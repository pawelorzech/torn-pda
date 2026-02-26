# Test Coverage Analysis — Torn PDA

> Analysis date: 2026-02-26

## Executive Summary

Torn PDA is a large Flutter/Dart mobile application (~238,000 lines of Dart across 393 files) with a Firebase Cloud Functions backend (~20 TypeScript files) and native Android Kotlin code (~2,700 lines across 20 files). **Test coverage is critically low**: the entire codebase has only **3 test files** (491 lines of test code), all of which cover a single Android-native feature (Live Updates). There are **zero Dart/Flutter tests** and **zero Cloud Functions tests**.

| Layer | Source Files | Lines of Code | Test Files | Coverage |
|---|---|---|---|---|
| Dart/Flutter (lib/) | 393 | ~238,000 | 0 | **0%** |
| Cloud Functions (TS) | 20 | ~3,500 | 0 | **0%** |
| Android Native (Kotlin/Java) | 20 | ~2,700 | 3 | ~15% (Live Updates only) |
| **Total** | **433** | **~244,200** | **3** | **~0.2%** |

There is no CI/CD pipeline, no coverage tracking tools, and no test scripts configured.

---

## Current Test Inventory

### What Exists

All 3 tests are JUnit 4 Kotlin unit tests in `android/app/src/test/java/com/manuito/tornpda/liveupdates/`:

| Test File | Lines | Tests | What It Covers |
|---|---|---|---|
| `LiveUpdateEligibilityEvaluatorTest.kt` | 109 | 4 | API level, notification permissions, battery optimization, device capability |
| `LiveUpdateChannelBridgeTest.kt` | 207 | 5 | Flutter method channel bridge (start/end/capabilities/events/unknown) |
| `DefaultLiveUpdateManagerTest.kt` | 175 | 4 | Session management, eligibility checks, timeout cleanup |

These tests are well-structured with proper fakes/mocks and good assertion patterns. They serve as a solid example for how to write tests for the rest of the codebase.

### What's Missing

- **No `test/` directory** at the Flutter project root
- **No test dependencies** in `pubspec.yaml` (no `flutter_test`, `mockito`, `bloc_test`, etc.)
- **No test script** in cloud_functions `package.json` (despite `firebase-functions-test` being a dev dependency)
- **No CI/CD** (no GitHub Actions, no GitLab CI, no Jenkinsfile)
- **No coverage tools** (no JaCoCo, no lcov, no Codecov)

---

## Recommended Test Improvements (Prioritized)

### Tier 1: Pure Functions & Utilities (Highest ROI — Easy to test, high value)

These are pure or nearly-pure functions with no framework dependencies. They can be tested with simple `flutter_test` unit tests — no mocking needed.

#### 1.1 `lib/utils/stats_calculator.dart` (74 lines)
- **What it does**: Calculates estimated battle stats from level, crimes, networth, and rank using lookup tables
- **Why test it**: Core game mechanic. Uses 3 lookup tables (8 level triggers, 6 crime triggers, 5 networth triggers) and 26 hardcoded rank mappings. Formula: `rankIndex - levelIndex - crimeIndex - networthIndex - 1` maps to one of 7 stat buckets
- **Test cases**: Each stat bucket boundary, edge cases (minimum/maximum values), unknown ranks, null inputs, out-of-range indices

#### 1.2 `lib/utils/time_formatter.dart` (308 lines)
- **What it does**: Formats timestamps with timezone support (Local Time vs Torn City Time/UTC), relative day calculations (today/tomorrow/in X days/yesterday), elapsed time formatting
- **Why test it**: 7 formatting methods, 10+ conditional branches. Used throughout the app for travel timers, cooldowns, event timelines. Complex day-difference calculation with timezone-aware date stripping
- **Test cases**: 24h vs 12h format, LT vs TCT zone, day boundary crossings, same-day/tomorrow/multi-day/yesterday, elapsed time (seconds/minutes/hours/days), month/day formatting

#### 1.3 `lib/utils/number_formatter.dart` (27 lines)
- **What it does**: Formats large numbers to human-readable strings (1,500,000 → "1.5M")
- **Why test it**: 4 boundary conditions (B/M/K/raw). Handles negative numbers. Used for all financial displays
- **Test cases**: Billions, millions, thousands, small numbers, negative values, zero, exact boundary values (999 vs 1000, 999999 vs 1000000)

#### 1.4 `lib/utils/country_check.dart` (65 lines)
- **What it does**: Determines player country from status strings ("Abroad", "Traveling", "Hospital")
- **Why test it**: String parsing with 11 country-specific hospital patterns. Returns "error" on exceptions. Critical for travel feature
- **Test cases**: Each country for Abroad/Traveling/Hospital states, "Returning to Torn", null inputs, unexpected status strings, empty descriptions

#### 1.5 `lib/utils/travel/travel_times.dart` (149 lines)
- **What it does**: Maps country + ticket type to travel time in minutes. 4 ticket types x 11 countries = 44 hardcoded values
- **Why test it**: Pure lookup function. Any typo in the values could cause incorrect travel notifications
- **Test cases**: Every country/ticket combination (44 cases), "Torn" destination (should return 0), string-based country name lookup, unknown countries

#### 1.6 `lib/utils/timestamp_ago.dart` (27 lines)
- **What it does**: Converts Unix timestamp to relative time string ("3 days ago", "2 weeks ago")
- **Why test it**: 5 conditional branches for seconds/days/weeks/months/years. Boundary-sensitive (7 days → weeks, 30 days → months)
- **Test cases**: Each time bucket, boundary values (6 vs 7 days, 29 vs 30 days, 364 vs 365 days), future timestamps, zero/negative

#### 1.7 `lib/utils/color_json.dart` (36 lines)
- **What it does**: Parses Color objects from multiple JSON formats (int, "Color(0xff...)", "#AABBCC", "ff7575")
- **Why test it**: 4 distinct input formats with potential parsing failures. Falls back to black on error
- **Test cases**: Each format, malformed strings, null, already a Color object, edge hex values

#### 1.8 `lib/utils/profile/events_timeline_fixes.dart` (196 lines)
- **What it does**: Fixes malformed HTML href attributes from the Torn API, processes event messages, strips unsupported HTML tags
- **Why test it**: Complex regex patterns, string manipulation with real-world malformed data. The `fixHrefAttributes` function handles duplicated `www.torn.com` prefixes
- **Test cases**: Double-prefixed hrefs, quoted/unquoted attributes, event message corrections (5 known patterns), HTML tag stripping (preserving `<a>` and `<b>`)

#### 1.9 `lib/utils/html_parser.dart` (15 lines)
- **What it does**: Strips HTML tags from strings
- **Test cases**: Various HTML inputs, nested tags, empty strings, already-clean strings

---

### Tier 2: API Error Handling & Data Models (Medium effort, high value)

#### 2.1 `lib/providers/api/api_utils.dart` — ApiError class (112 lines)
- **What it does**: Maps 20 Torn API error codes to human-readable messages (0=no connection, 2=incorrect key, 5=rate limited, 8=IP banned, etc.)
- **Why test it**: 20 switch cases. User-facing error messages must be accurate. Includes PDA-specific codes (999, 100, 101)
- **Test cases**: Every error code (0-18 + 999/100/101), unknown codes, default fallback behavior

#### 2.2 `lib/providers/api/api_utils.dart` — ApiCallRequest class
- **What it does**: Ensures exactly one of V1 or V2 API selection is provided (assertion)
- **Test cases**: V1-only, V2-only, both (should fail), neither (should fail)

#### 2.3 Model serialization (lib/models/)
- **Key models to test**:
  - `own_profile_basic.dart` (497 lines) — Core user profile with nested married/faction/status data
  - `own_profile_misc.dart` (1,060 lines) — Battle stats, work stats, money, skills, bazaar items
  - `bars_model.dart` (513 lines) — Energy/nerve/happy/life bars with chain status
  - `target_model.dart` (457 lines) — Attack target with status, faction, last action
  - `friend_model.dart` (410 lines) — Friend data with profile info
  - `appwidget_api_model.dart` (514 lines) — Home screen widget data
- **Why test them**: These are the data contracts between the API and the app. JSON parsing errors silently corrupt data
- **Test approach**: Create JSON fixtures from real API responses, verify round-trip serialization (fromJson → toJson → fromJson)

---

### Tier 3: State Management & Business Logic (Higher effort, critical impact)

#### 3.1 `lib/providers/targets_provider.dart` (1,259 lines)
- **What to test**:
  - **Respect calculation** (lines ~177-232): `fairFight * 0.25 * (log(level) + 1)` with attack history parsing
  - **20-type sorting comparator** (lines ~705-767): Level, respect, FF, name, life, hospital, status, color, notes, bounty, time added — each ascending and descending
  - **Range filtering** (lines ~769-807): Level/life/fair-fight/hospital-time range filtering with hospital time calculation from epoch
  - **Bounty amount parsing**: Regex extraction of currency from strings (`$1,234.00` → 1234)
  - **Import/merge logic** (lines ~934-1019): Paginated API fetching with deduplication
- **Why test it**: Core targeting system. Sorting bugs could make players miss important targets

#### 3.2 `lib/providers/chain_status_controller.dart` (2,064 lines)
- **What to test**: The DEFCON state machine (9 states: cooldown, green1-2, orange1-2, red1-2, panic, off, apiFail). State transitions based on chain count, time remaining, and API health
- **Why test it**: Chain watching is a flagship feature. Incorrect alerts could cause players to lose chains

#### 3.3 `lib/providers/war_controller.dart` (2,227 lines)
- **What to test**: Target sorting, faction member filtering, war status calculations
- **Why test it**: Faction wars are high-stakes. Sorting/filtering bugs affect war coordination

#### 3.4 `lib/providers/player_notes_controller.dart` (231 lines)
- **What to test**: CRUD operations, color validation (sentinel 'z' for none), automatic cleanup when note+color are empty, migration from null colors
- **Why test it**: Moderate complexity with clear state transitions. Good candidate for controller testing

#### 3.5 `lib/providers/api/api_caller.dart` (587 lines)
- **What to test**: Rate limiting logic (95 calls/100s), queue management, concurrent request throttling (base 6 → backoff to 1), error accumulation tracking
- **Why test it**: Protects against Torn API bans. Rate limiting bugs could lock users out of the API

---

### Tier 4: Cloud Functions (Separate test stack — TypeScript/Jest)

#### 4.1 `cloud_functions/functions/src/torn_api.ts` (API utilities)
- **What to test**: `getUsersStat()`, `getUsersRefills()`, `checkUserIdKey()` — API call wrappers
- **Setup needed**: Add a `test` script to package.json, configure Jest or Vitest, mock Firebase Admin SDK

#### 4.2 `cloud_functions/functions/src/notification.ts` (35.9 KB — largest file)
- **What to test**: Each notification type's condition checking (energy full, nerve full, hospital status, drug cooldowns, message detection)
- **Why test it**: Push notification logic affects 40k+ users. False positives/negatives are highly visible

#### 4.3 `cloud_functions/functions/src/helpers.ts`
- **What to test**: Shared utility functions
- **Why test it**: Used across multiple functions; bugs propagate widely

---

### Tier 5: Expand Android Native Tests

The existing 3 Kotlin tests cover `LiveUpdateEligibilityEvaluator`, `LiveUpdateChannelBridge`, and `DefaultLiveUpdateManager`. The remaining untested Kotlin files:

| File | Lines | Testing Priority |
|---|---|---|
| `LiveUpdatePayload.kt` | 74 | Medium — data mapping |
| `LiveUpdateSessionRegistry.kt` | 68 | Medium — session state management |
| `LiveUpdateCapabilityMonitor.kt` | 69 | Medium — capability change detection |
| `LiveUpdateCapabilityStore.kt` | 52 | Low — simple SharedPreferences wrapper |
| `LiveUpdateTapIntentFactory.kt` | 35 | Low — intent creation |
| `AndroidLiveUpdateAdapter.kt` | 395 | High — complex notification management |

---

## Infrastructure Recommendations

### 1. Set Up Flutter Test Infrastructure
```yaml
# Add to pubspec.yaml under dev_dependencies:
dev_dependencies:
  flutter_test:
    sdk: flutter
  mockito: ^5.4.0
  build_runner: ^2.4.0  # already present
```

Create `test/` directory with subdirectories mirroring `lib/`:
```
test/
  utils/
    stats_calculator_test.dart
    time_formatter_test.dart
    number_formatter_test.dart
    country_check_test.dart
    travel/
      travel_times_test.dart
    ...
  providers/
    api/
      api_utils_test.dart
    targets_provider_test.dart
    ...
  models/
    profile/
      own_profile_basic_test.dart
    ...
```

### 2. Set Up Cloud Functions Test Infrastructure
```json
// Add to cloud_functions/functions/package.json scripts:
"scripts": {
  "test": "jest --coverage",
  ...
}
```

### 3. Add CI/CD Pipeline
A GitHub Actions workflow running `flutter test` and Android unit tests on every PR would prevent regressions as the codebase grows.

### 4. Add Coverage Tracking
Configure `flutter test --coverage` with lcov to track progress toward coverage goals.

---

## Suggested Implementation Order

| Phase | Scope | Estimated Tests | Impact |
|---|---|---|---|
| **Phase 1** | Tier 1 pure functions (9 files) | ~80-120 tests | Covers core calculations, formatting, and parsing |
| **Phase 2** | Tier 2 API errors + key models (8 files) | ~50-80 tests | Covers API error handling and data contracts |
| **Phase 3** | Tier 3 provider logic (5 controllers) | ~40-60 tests | Covers business-critical state management |
| **Phase 4** | Tier 4 Cloud Functions (3 files) | ~30-50 tests | Covers backend notification logic |
| **Phase 5** | Tier 5 remaining Android native | ~15-25 tests | Completes native layer coverage |

**Phase 1 alone would dramatically improve confidence** in the most testable and highest-risk code paths, with minimal setup effort.

---

## Key Files Reference

### Pure Functions (Test First)
| File | Lines | Description |
|---|---|---|
| `lib/utils/stats_calculator.dart` | 74 | Battle stats estimation |
| `lib/utils/time_formatter.dart` | 308 | Time/date formatting |
| `lib/utils/number_formatter.dart` | 27 | Large number formatting |
| `lib/utils/country_check.dart` | 65 | Country detection from status |
| `lib/utils/travel/travel_times.dart` | 149 | Travel time lookups |
| `lib/utils/timestamp_ago.dart` | 27 | Relative time strings |
| `lib/utils/color_json.dart` | 36 | Color format parsing |
| `lib/utils/profile/events_timeline_fixes.dart` | 196 | HTML/event message fixing |
| `lib/utils/html_parser.dart` | 15 | HTML tag stripping |

### Critical Business Logic (Test Next)
| File | Lines | Description |
|---|---|---|
| `lib/providers/api/api_utils.dart` | 163 | API error code mapping |
| `lib/providers/targets_provider.dart` | 1,259 | Target sorting/filtering/calculations |
| `lib/providers/chain_status_controller.dart` | 2,064 | Chain watcher state machine |
| `lib/providers/war_controller.dart` | 2,227 | War management logic |
| `lib/providers/api/api_caller.dart` | 587 | API rate limiting queue |
| `lib/providers/player_notes_controller.dart` | 231 | Player notes CRUD |
