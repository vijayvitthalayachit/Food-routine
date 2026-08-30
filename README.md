# Food Routine

An Android app that plans three meals a day around the Hindu calendar,
manages your recipes (saved from Instagram/Facebook or created by hand),
and tells you exactly what to buy, soak and sprout - and when.

## Features

### Hindu-calendar-aware meal planning
- Computes the **tithi** for every day astronomically (Sun/Moon ecliptic
  longitudes, sunrise-rule tithi assignment) - no internet needed.
- **Fasting days get no meal plan**:
  - **Ekadashi** (both pakshas, tithi at local sunrise),
  - **Janmashtami** (Krishna Ashtami of amanta Shravana, midnight rule),
  - **Grahana** (built-in solar/lunar eclipse table for 2025-2028),
  - **Custom fasts** you add yourself (Settings tab).
- Generates a **distinct meal plan for up to 15 tithi days**, from
  **Pratipada to Purnima/Amavasya**, skipping all fasting days. Recipes are
  not repeated while the pool lasts; small pools fall back to
  least-recently-used reuse (never twice on the same day).

### Recipes
- **Share from Instagram reels, Facebook videos, YouTube or any website**:
  share the link to *Food Routine* and it opens a pre-filled recipe editor
  (the source platform is auto-detected).
- **Custom recipe editor** with ingredients, quantities, servings,
  instructions and meal-type tags (breakfast/lunch/dinner).
- Ingredient **nutrition auto-fill** from a built-in catalog of ~65 common
  Indian ingredients (per-100 g calories, protein, carbs, fiber).
- **Calories, protein, carbs and fiber** are calculated per recipe, per
  serving, and for the whole day of the plan.

### Groceries, soaking and sprouting
Derived automatically from the active plan:
- Regular ingredient → **buy 1 day before** cooking.
- Soak-overnight ingredient (chana, rajma, sabudana...) → **buy 2 days
  before**, reminder to **soak the previous night**.
- Sprouted ingredient (whole moong, matki...) → **buy 3 days before**,
  **soak two nights before**, **drain & sprout/ferment the previous night**.
- A daily 6 pm notification lists what to buy and what to soak/sprout
  tonight. The Grocery tab shows the full dated timeline; same-day
  purchases of the same ingredient are merged.

## Project layout

| Module | Contents |
|--------|----------|
| `core` | Pure Kotlin (no Android): astronomy (`astro`), tithi/lunar-month panchang + fasting rules (`panchang`), meal-plan generator (`planner`), grocery/soak/sprout scheduler (`grocery`), nutrition catalog & calculator (`nutrition`). Fully unit-tested. |
| `app`  | Android app: Jetpack Compose UI, Room persistence, share-intent receiver, WorkManager daily reminder. |

`settings.gradle.kts` includes `:app` only when an Android SDK is present,
so `./gradlew :core:test` works on any machine with a JDK.

## Building

```bash
# Core logic + tests (JDK 17+ only)
./gradlew :core:test

# Debug APK (requires the Android SDK)
./gradlew :app:assembleDebug
```

CI (`.github/workflows/android-build.yml`) runs the tests and uploads the
debug APK as an artifact on every push.

- **minSdk 26** (Android 8.0), targetSdk 35.
- Kotlin 2.0, Jetpack Compose (Material 3), Room, WorkManager.

## Accuracy notes

Tithi timings are computed from a truncated ELP/Meeus lunar theory
(elongation good to a few arc-minutes, boundaries to within ~2 minutes of
time) with an approximate Lahiri ayanamsa for lunar-month naming. Near a
tithi boundary the result can differ by a day from a printed panchang, and
observance rules vary by tradition - always confirm important vrat dates
with your panchang. Eclipse (Grahana) dates beyond 2028 should be added as
custom fasting days.
