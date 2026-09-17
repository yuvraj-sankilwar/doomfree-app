package com.doomfree.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.doomfree.launcher.data.AppGroup
import com.doomfree.launcher.data.QuickActionSlot
import com.doomfree.launcher.ui.screens.allapps.AllAppsScreen
import com.doomfree.launcher.ui.screens.focus.FocusScreen
import com.doomfree.launcher.ui.screens.frequent.FrequentAppsScreen
import com.doomfree.launcher.ui.screens.home.HomeScreen
import com.doomfree.launcher.ui.screens.settings.AppGroupManagerScreen
import com.doomfree.launcher.ui.screens.settings.AppPickerScreen
import com.doomfree.launcher.ui.screens.settings.SettingsScreen
import com.doomfree.launcher.ui.screens.theme.ThemePickerScreen
import com.doomfree.launcher.ui.screens.todo.TodoScreen
import com.doomfree.launcher.ui.theme.DoomfreeTheme
import kotlinx.coroutines.delay

private object Routes {
    const val HOME_CAROUSEL = "home_carousel"
    const val FOCUS = "focus"
    const val SETTINGS = "settings"
    const val THEME_PICKER = "theme_picker"
    const val MANAGE_FREQUENT = "manage_frequent"
    const val MANAGE_ESSENTIALS = "manage_essentials"
    const val MANAGE_DISTRACTIONS = "manage_distractions"
    const val PICK_QUICK_ACTION = "pick_quick_action/{slot}"
    fun pickQuickAction(slot: QuickActionSlot) = "pick_quick_action/${slot.name}"
}

/** Carousel page order — To-do/Focus sits left of Home, All Apps is pushed two swipes right past
 * Frequent Apps on purpose: the least-curated entry point should cost a little more friction. */
private const val PAGE_COUNT = 4
private const val PAGE_TODO = 0
private const val PAGE_HOME = 1
private const val PAGE_FREQUENT = 2
private const val PAGE_ALL_APPS = 3

@Composable
fun DoomfreeApp(viewModel: MainViewModel) {
    val accent by viewModel.accentColor.collectAsState()
    val navController = rememberNavController()

    DoomfreeTheme(accent = accent) {
        // Fallback background behind the whole NavHost, for the sliver of transparent status/nav
        // bar area before any destination has laid out. Each screen paints its own background
        // (flat or gradient) fully edge-to-edge and insets only its *content* with
        // statusBarsPadding() — that split is what lets accent-colored gradients extend behind
        // the status bar instead of a flat color fighting with them at the seam.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME_CAROUSEL,
            ) {
            composable(Routes.HOME_CAROUSEL) {
                HomeCarousel(
                    viewModel = viewModel,
                    onOpenFocus = { navController.navigate(Routes.FOCUS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onConfigureQuickAction = { slot -> navController.navigate(Routes.pickQuickAction(slot)) },
                )
            }
            composable(Routes.FOCUS) {
                FocusScreen(
                    viewModel = viewModel,
                    onSessionStarted = { navController.popBackStack() },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenFrequentApps = { navController.navigate(Routes.MANAGE_FREQUENT) },
                    onOpenEssentials = { navController.navigate(Routes.MANAGE_ESSENTIALS) },
                    onOpenDistractions = { navController.navigate(Routes.MANAGE_DISTRACTIONS) },
                    onOpenTheme = { navController.navigate(Routes.THEME_PICKER) },
                )
            }
            composable(Routes.THEME_PICKER) {
                ThemePickerScreen(
                    viewModel = viewModel,
                    onConfigureQuickAction = { slot -> navController.navigate(Routes.pickQuickAction(slot)) },
                )
            }
            composable(Routes.MANAGE_FREQUENT) {
                val installedApps by viewModel.installedApps.collectAsState()
                AppGroupManagerScreen(
                    title = "Frequent Apps",
                    subtitle = "Shown in the 3×3 grid on the Frequent Apps panel",
                    selectedLabel = "Frequent",
                    installedApps = installedApps,
                    isSelected = { it.isFrequent },
                    onToggle = { viewModel.toggleFrequent(it.packageName, makeFrequent = !it.isFrequent) },
                    onReset = { viewModel.resetFrequent() },
                )
            }
            composable(Routes.MANAGE_ESSENTIALS) {
                val installedApps by viewModel.installedApps.collectAsState()
                AppGroupManagerScreen(
                    title = "Essentials",
                    subtitle = "These always launch, even during a Focus session",
                    selectedLabel = "Essentials",
                    installedApps = installedApps,
                    isSelected = { it.group == AppGroup.ESSENTIAL },
                    onToggle = {
                        val next = if (it.group == AppGroup.ESSENTIAL) AppGroup.GENERAL else AppGroup.ESSENTIAL
                        viewModel.setGroup(it.packageName, next)
                    },
                    onReset = { viewModel.resetGroup(AppGroup.ESSENTIAL) },
                )
            }
            composable(Routes.MANAGE_DISTRACTIONS) {
                val installedApps by viewModel.installedApps.collectAsState()
                AppGroupManagerScreen(
                    title = "Distractions",
                    subtitle = "Blocked while a Focus session is active",
                    selectedLabel = "Distractions",
                    installedApps = installedApps,
                    isSelected = { it.group == AppGroup.DISTRACTION },
                    onToggle = {
                        val next = if (it.group == AppGroup.DISTRACTION) AppGroup.GENERAL else AppGroup.DISTRACTION
                        viewModel.setGroup(it.packageName, next)
                    },
                    onReset = { viewModel.resetGroup(AppGroup.DISTRACTION) },
                )
            }
            composable(Routes.PICK_QUICK_ACTION) { backStackEntry ->
                val slotName = backStackEntry.arguments?.getString("slot")
                val slot = QuickActionSlot.entries.find { it.name == slotName } ?: QuickActionSlot.GESTURE
                val installedApps by viewModel.installedApps.collectAsState()
                AppPickerScreen(
                    title = slot.label,
                    subtitle = "Choose which app opens here",
                    installedApps = installedApps,
                    onPick = { app ->
                        viewModel.setQuickAction(slot, app.packageName)
                        navController.popBackStack()
                    },
                )
            }
        }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeCarousel(
    viewModel: MainViewModel,
    onOpenFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfigureQuickAction: (QuickActionSlot) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = PAGE_HOME) { PAGE_COUNT }
    val gesturePackage by viewModel.quickActionPackage(QuickActionSlot.GESTURE).collectAsState()
    val density = LocalDensity.current
    val view = LocalView.current

    var dotsVisible by remember { mutableStateOf(true) }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        dotsVisible = true
        if (!pagerState.isScrollInProgress) {
            delay(5000)
            dotsVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                PAGE_TODO -> TodoScreen(viewModel = viewModel, onOpenFocus = onOpenFocus)
                PAGE_HOME -> HomeScreen(
                    viewModel = viewModel,
                    onConfigureQuickAction = onConfigureQuickAction,
                )
                PAGE_FREQUENT -> FrequentAppsScreen(viewModel, onOpenSettings = onOpenSettings)
                PAGE_ALL_APPS -> AllAppsScreen(viewModel)
            }
        }

        AnimatedVisibility(
            visible = dotsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                repeat(PAGE_COUNT) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                CircleShape,
                            ),
                    )
                }
            }
        }

        // Swipe-up shortcut zone, sitting just above the bottom 10% of the screen — not at the
        // very edge, since that's literally the same touch area as the system's own
        // back/home gesture strip and the two would otherwise fight over every touch.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val zoneHeight = 48.dp
            val bottomMargin = maxHeight * 0.10f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomMargin)
                    .fillMaxWidth()
                    .height(zoneHeight)
                    .onGloballyPositioned { coordinates ->
                        if (android.os.Build.VERSION.SDK_INT >= 29) {
                            val bounds = coordinates.boundsInWindow()
                            view.systemGestureExclusionRects = listOf(
                                android.graphics.Rect(
                                    bounds.left.toInt(),
                                    bounds.top.toInt(),
                                    bounds.right.toInt(),
                                    bounds.bottom.toInt(),
                                ),
                            )
                        }
                    }
                    .pointerInput(gesturePackage) {
                        val triggerPx = with(density) { 24.dp.toPx() }
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalDy = 0f
                            verticalDrag(down.id) { change ->
                                totalDy += change.positionChange().y
                                change.consume()
                            }
                            if (-totalDy > triggerPx) {
                                gesturePackage?.let { viewModel.launchApp(it) }
                            }
                        }
                    },
            )
        }
    }
}
