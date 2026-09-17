# Minimal Focus Launcher — Implementation Plan

Written for: a fresh Claude Code session tasked with building this Android launcher app from scratch, with no prior context on the design discussion that produced this plan.

Design reference (visual source of truth — match this, don't reinvent it):
https://claude.ai/artifact/LNEcfsaHdoSwgJjn8XcyiX
("Launcher — Final Flow" page: Home, All Apps, Frequent Apps, Focus, Theme picker, Blocked popup.)

## 1. Product summary

A distraction-reducing Android home-screen replacement (launcher). The core idea: **match effort to intent**. Things a user reaches for instinctively cost zero friction; things that hook them cost a little. This is achieved with a 3-panel home carousel plus a lightweight, always-on app-grouping system — not a heavyweight "screen time dashboard" app.

Explicitly **not** goals for v1 (do not scope-creep into these):
- No built-in screen-time analytics/insights dashboard — the user checks Android's own Digital Wellbeing for that.
- No dedicated "Focus Active" lock screen — enforcement is just an inline popup at the moment a blocked app is opened.
- No fleshed-out enforcement for the "General" app group yet (see §4) — just tag and store the category; behavior is deferred.
- No custom widgets, no multi-user/cloud sync, no theming beyond one accent-color swap.

## 2. Tech stack

- **Kotlin + Jetpack Compose** for all UI. This must be a native launcher — cross-platform frameworks (Flutter/RN) don't expose the Android APIs this needs (see below) without heavy platform-channel bridging, so don't consider them.
- **Room** for persistence: app groupings, todo items, session state.
- **DataStore (Preferences)** for small singleton settings: selected accent color key, default-launcher-onboarding-complete flag.
- **AccessibilityService** to detect foreground-app changes system-wide (required to intercept a blocked app opened from *outside* the launcher — notification tap, deep link, recents, etc. — not just from this app's own UI).
- **WindowManager overlay (`TYPE_APPLICATION_OVERLAY`)** to draw the "blocked" popup on top of whatever app just tried to open.
- **PackageManager / LauncherApps** to enumerate installed apps, icons, labels.
- **RoleManager** (`RoleManager.ROLE_HOME`) on API 29+, plus the classic `Intent.ACTION_MAIN` / `CATEGORY_HOME` `<intent-filter>` for all versions, to make this installable as the default launcher.
- **CountDownTimer / AlarmManager (exact alarm)** for the Focus session duration — use AlarmManager for the session end so it's reliable even if the process is killed; CountDownTimer only drives the in-app UI while visible.

## 3. Required permissions & onboarding

These need explicit user grants and a short onboarding flow before the app is usable:

1. **Default launcher role** — `RoleManager.createRequestRoleIntent(ROLE_HOME)` (API 29+) or the standard "always use this app" system prompt on older versions.
2. **Accessibility Service** — user must manually enable it in system settings; deep-link to `Settings.ACTION_ACCESSIBILITY_SETTINGS` and explain why (this is what makes blocking work outside the launcher itself).
3. **Draw over other apps** (`Settings.ACTION_MANAGE_OVERLAY_PERMISSION`) — needed for the blocked-app overlay popup.
4. **Usage Access** (`PACKAGE_USAGE_STATS`) — only if you end up needing per-app usage stats later; not required for the core group-check flow, so defer unless a feature needs it.

Build one short onboarding sequence (3 permission cards, one per grant, each explaining the "why" in one line) that runs once on first launch and is skippable per-step but not fully skippable (the app can't function as a launcher without at least step 1).

**Important honesty note to carry into the build:** on stock, non-managed Android, "100% blocked, no override" is a best-effort UX guarantee, not a cryptographic one — a user can always go disable the Accessibility Service in system settings to bypass it. Don't oversell this in copy or in your own reasoning; if the user wants a truly unbypassable block, that requires Device Owner / enterprise MDM mode, which is out of scope here. Make the bypass path *inconvenient* (it's a few taps into system settings), not impossible.

## 4. Data model (Room)

```
AppEntry
  packageName: String (PK)
  label: String
  group: enum { ESSENTIAL, DISTRACTION, GENERAL }   // default GENERAL for every newly-seen app
  isFrequent: Boolean                                // shown in the Frequent Apps panel
  frequentSortOrder: Int?

TodoItem
  id: Long (PK, autogen)
  text: String
  done: Boolean
  createdAt: Long

FocusSession
  id: Long (PK, autogen)
  startedAt: Long
  durationMs: Long
  endsAt: Long
  isActive: Boolean
```

Settings (DataStore, not Room): `accentColorKey: String` (one of the theme keys below), `onboardingComplete: Boolean`.

On first app-list sync (and whenever a newly-installed app is detected via `PACKAGE_ADDED` broadcast), insert an `AppEntry` with `group = GENERAL` by default. Never default anything to ESSENTIAL or DISTRACTION automatically — those are always an explicit user choice made on the Focus screen.

## 5. Screens (build these six, nothing else)

Match layout, spacing, type, and the color system from the design canvas. Default accent is `#ff7a3d` on a near-black warm background (`#141110`/`#18150f` card tones), fonts are JetBrains Mono (numerals/labels) + Space Grotesk (body) loaded as Google Fonts or bundled.

### 5.1 Home (center panel)
Glance-only — **no app icons here at all**. Shows: live clock (segmented-digit style), date, a battery tile, a screen-time tile (read from `UsageStatsManager` if usage-access is granted, else hide gracefully), and a small **To-Do widget** (3–5 items max, checkable, from `TodoItem`) in place of any quick-launch row. Bottom dot indicator shows position (center of 3).

### 5.2 All Apps (swipe left from Home)
Alphabetically sectioned, searchable list of every installed app **not** marked frequent. Search bar at top, section-letter headers, fade-out at the scroll edge. Tapping a row: look up the app's `AppEntry.group`; if a session `isActive` and `group == DISTRACTION`, show the Blocked popup (§5.5) instead of launching; otherwise launch normally (ESSENTIAL and GENERAL both launch freely for now — see §4/§ non-goals on deferred GENERAL enforcement).

### 5.3 Frequent Apps (swipe right from Home)
A 3×3 grid: the user's pinned frequent apps (tap to open, long-press to edit/reorder) **plus one fixed tile: "Focus"** (not a user app — always present, opens §5.4). A small gear icon top-right of this screen only opens the Theme picker (§5.6) — this is the *only* place theming is reachable; do not add a settings tab or icon anywhere else.

### 5.4 Focus (opened by tapping the Focus tile)
One combined screen, not several:
- **Essentials** chip list (+ add) — apps here always launch, no group-check needed at all in the enforcement code path.
- **Distractions** chip list (+ add) — apps here get blocked (§5.5) whenever a session is active.
- One line of static copy: "Everything else stays General — untouched for now."
- Duration picker (preset pills: 15m / 45m / 3h / Custom) + a live segmented-digit preview of the chosen duration.
- "Slide to start" control at the bottom — on completion, create a `FocusSession` row, schedule its end via `AlarmManager`, and return to Home.

Saved groupings persist across sessions (Room), so this screen reopens with prior choices already reflected — it's a management screen you revisit, not a one-time wizard.

### 5.5 Blocked popup (not a navigation destination — an overlay)
A small centered dialog card (not full-screen): app name + "is blocked" + "Marked as a Distraction — locked until your session ends at {time}" + a single "Got it" dismiss. No secondary action, no override button, ever. This is drawn either as an in-app dialog (when the tap happened inside this launcher's All Apps list) or as a `WindowManager` overlay (when the Accessibility Service caught the app being opened from elsewhere) — same visual component, two trigger paths.

### 5.6 Theme picker (opened only from the gear icon on Frequent Apps)
A single grid of 8 solid color swatches — Orange (default), Purple, Green, Teal, Pink, Red, Blue, Sky Blue. Tapping one writes `accentColorKey` to DataStore and every screen re-reads that single color token; there is nothing else on this screen and no other entry point to it.

## 6. Enforcement flow (the part that actually reduces usage)

1. `AppBlockAccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED` events and reads the foreground `packageName`.
2. On each change: if there's no active `FocusSession`, do nothing (fully passive). If a session is active, look up that package's `AppEntry.group`:
   - `ESSENTIAL` or unknown/not-yet-seen system package → do nothing, let it run.
   - `DISTRACTION` → immediately `performGlobalAction(GLOBAL_ACTION_BACK)` (or HOME) to back out of it, then show the overlay popup (§5.5).
   - `GENERAL` → do nothing for now (deferred, per non-goals).
3. When `FocusSession.endsAt` passes (via the scheduled `AlarmManager` callback), flip `isActive = false`. No special "session ended" screen is needed — the block simply stops applying.

## 7. Suggested build order

1. Project scaffold, manifest with `HOME`/`DEFAULT` intent-filter, RoleManager request flow.
2. Home screen with static/sample data, theme token wired end-to-end (prove the accent-swap works before building more screens around it).
3. App list loading (`LauncherApps`) → All Apps (search + sections) and Frequent Apps (grid, pin/reorder) with real installed apps.
4. Room schema + DAOs; To-Do widget on Home backed by real data.
5. Focus screen: chip management (essentials/distractions) + duration picker + slide-to-start → creates a real `FocusSession`.
6. Accessibility service + overlay popup wired to the session/group check (§6). This is the highest-risk, most fiddly part — build and test it in isolation before polishing anything else.
7. Permission onboarding sequence (§3), gating first run.
8. Theme picker screen + gear icon entry point.
9. Pass on edge cases: uninstalled apps disappearing from all lists, newly installed apps defaulting to GENERAL, device reboot re-arming any scheduled alarms.

## 8. Explicitly out of scope (revisit only if asked)

Insights/analytics screen, GENERAL-group enforcement mechanics, custom home-screen widgets beyond the built-in tiles, multi-device sync, Device-Owner/MDM-grade unbypassable locking.
