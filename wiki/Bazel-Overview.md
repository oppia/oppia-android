## Overview

TODO: Finish this page. Add details:

- Link to set-up guide
- Link to background
- Link to cheat sheet
- Link to Android Studio guide
- Link to Advanced topics
- Link to Advanced topics
- FAQs:
  - Unresolved symbols in Android Studio after switching branches
  - Fixing a failure in syncing in Android Studio
  - Keeping an eye on system RAM & CPU usage via System Monitor (or other tools), and what to do if resources spike too high
  - How to add dependencies (tips & tricks for using AS plugin), and how to deal with missing deps when building, include bits for strict deps since they require interpretation (for Java, Maven, and Kotlin). More specifically, how to deal with "remove android_sdk" dep if we end up keeping that change to rules_kotlin.
    - Need to depend on some sort of external lib -> how to change to //third_party-esque ref
      - downloaded.kt (direct dep) vs. @maven_{app,script}// dep
    - Need to depend on android_sdk -> usually means non-Android lib depending on Android lib (can't do that, or need to convert Android lib to be non-Android)
    - Need to depend on first-party lib (how to convert to correct target); converting local packages to local refs
    - How to find the target being discussed (extra _kt)
    - Moving transitive dep to main dep (for add_dep cases) for Maven
    - Dealing with NonExistentClass errors in generated code (particularly Dagger) -- use nearby generated code/names for context on the type it's expecting & make sure that's in the deps list.
    - Use new SuggestBuildFixes script (note some pitfalls: failures to build, incorrect results due to a dependency needing a change, not expanding non-wildcard targets, and needing multiple runs).
  - TODO: Determine others based on experimentation
