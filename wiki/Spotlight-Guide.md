Feature Spotlights

## Table of Contents

- [Introduction](#introduction)
- [Creating a new spotlight](#creating-a-new-spotlight)
  - [Registering a new feature](#registering-a-new-feature)
  - [Hooking up the spotlight](#hooking-up-the-spotlight)
- [Testing spotlights](#testing-spotlights)
- [Disabling the spotlights](#disabling-the-spotlights)
  - [In tests](#in-tests)
  - [In production](#in-production)

# Introduction

Spotlight is a visual tool that highlights and brings a user’s focus to an element on the screen. We use them to communicate the purpose of a specific screen element to the user, which they might otherwise miss. Spotlighting involves dimming the other areas on the screen and lighting the element to bring the user’s focus onto it, and explaining what/how that feature should be used. For example, this is a spotlight on the home screen spotlighting a story:

![image](https://user-images.githubusercontent.com/64526117/233807103-0bb76f92-8821-47cc-a8ac-8222e71214b4.png)

# Creating a new spotlight

The [SpotlightFragment](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightFragment.kt#L44) contains APIs to call spotlights for any in-app screen element. To create a spotlight, call one of these functions:
- [requestSpotlight](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightManager.kt#L26)
- [requestSpotlightWithDelayedLayout](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightManager.kt#L13)

Both these functions expect a parameter, [SpotlightTarget](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightTarget.kt#L14). The spotlight target is a holder for the necessary information to show a spotlight. 

The spotlight target binds these fields together:
- anchor: The view that should be spotlit.
- hint: The helpful text that should appear along the spotlight to describe the element being spotlit.
- shape: The preferred shape of the spotlight highlight. Can be either a circle or a rounded rectangle, based on whichever shape best fits the area being highlighted.
- feature: The specific app feature that the spotlight is tied to. It’s used to track whether this specific spotlight has been seen.

In order to start spotlighting an element in the UI, three high-level things need to be done:
- The feature’s spotlight needs to be defined.
- The new spotlight needs to be hooked up for persistent storage.
- The spotlight needs to be hooked up to be shown in the UI.

## Registering a new feature

In the file [spotlight.proto](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/model/src/main/proto/spotlight.proto#L1), two things need to be added.

A new feature (in the [Spotlight](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/model/src/main/proto/spotlight.proto#L10) proto):
```proto
message Spotlight {
  // Determines the UI element being spotlit.
  oneof feature {
    // Corresponds to the onboarding screen's next button.
    SpotlightViewState onboarding_next_button = 1;

    // Corresponds to the topic fragment's lessons tab.
    SpotlightViewState topic_lesson_tab = 2;

    // Corresponds to the topic fragment's revision tab.
    SpotlightViewState topic_revision_tab = 3;

    // Add and describe your new spotlit feature here.
    SpotlightViewState your_feature_name = <next_available_index>;
}
```

And somewhere to store it (in [SpotlightStateDatabase](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/model/src/main/proto/spotlight.proto#L53)):
```proto
message SpotlightStateDatabase {
  // Corresponds to the onboarding screen's next button.
  SpotlightViewState onboarding_next_button = 1;

  // Corresponds to the topic fragment's lessons tab.
  SpotlightViewState topic_lesson_tab = 2;

  // Corresponds to the topic fragment's revision tab.
  SpotlightViewState topic_revision_tab = 3;

  // Similarly, add storage for your new feature here.
  SpotlightViewState your_feature_name = <next_available_index>;
}
```

The new spotlight feature also needs to be added to the [SpotlightStateController](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/domain/src/main/java/org/oppia/android/domain/spotlight/SpotlightStateController.kt#L3). In the [retrieveSpotlightViewState](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/domain/src/main/java/org/oppia/android/domain/spotlight/SpotlightStateController.kt#L80) and the [recordSpotlightStateAsync](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/domain/src/main/java/org/oppia/android/domain/spotlight/SpotlightStateController.kt#L110) methods, add the feature to the switch case as so:
```kotlin
fun retrieveSpotlightViewState(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
  ): DataProvider<SpotlightViewState> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
      ) {
        val viewState = when (feature) {
          ONBOARDING_NEXT_BUTTON -> it.onboardingNextButton
          TOPIC_LESSON_TAB -> it.topicLessonTab
          TOPIC_REVISION_TAB -> it.topicRevisionTab
          YOUR_FEATURE_NAME -> it.yourFeatureName
```

Similarly for the [recordSpotlightStateAsync](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/domain/src/main/java/org/oppia/android/domain/spotlight/SpotlightStateController.kt#L110) method.

## Hooking up the spotlight
In order to request a spotlight in the UI, use [SpotlightManager](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightManager.kt#L4):

```kotlin
checkNotNull(getSpotlightManager()).requestSpotlight(
  SpotlightTarget(
    binding.yourFeatureName,
    R.string.your_feature_string_string_resource,
    SpotlightShape.RoundedRectangle,
    Spotlight.FeatureCase.YOUR_FEATURE_NAME
  )
)

private fun getSpotlightManager(): SpotlightManager? {
  return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
    SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
  ) as? SpotlightManager
}
```

For views that are laid out late in the lifecycle (such as recycler views loaded after a data provider call, view pagers, or elements that show up after an enter animation), use [requestSpotlightWithDelayedLayout](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/app/src/main/java/org/oppia/android/app/spotlight/SpotlightManager.kt#L13) instead.

# Testing spotlights
Spotlights are tested by checking if the expected hint is shown on the screen or not.

```kotlin
  @Test
  fun testPromotedStorySpotlight_setToShowOnSecondLogin_notSeenBefore_checkSpotlightShown() {
    logIntoUserTwice()
    launch<HomeActivity>(createHomeActivityIntent(internalProfileId1)).use {
      testCoroutineDispatchers.runCurrent()
      onView(withText(R.string.promoted_story_spotlight_hint))
        .check(matches(isDisplayed()))
      // Or, use check(doesNotExist()) to verify it is not being shown.
    }
  }
```

In order to bypass a spotlight without any UI interaction, the ``SpotlightStateController`` should be used to mark the spotlights as seen. A helper function can be introduced and used to mark one or more spotlights as seen. 
	
```kotlin
  private fun markAllSpotlightsSeen() {
    val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_LESSON_TAB)  
    spotlightStateController.markSpotlightViewed(profileId, TOPIC_REVISION_TAB) 
    spotlightStateController.markSpotlightViewed(profileId, YOUR_FEATURE_NAME)
    testCoroutineDispatchers.runCurrent()
  }
```

## Disabling the spotlights
If the spotlights need to be disabled, the platform parameter value should be set:

### In tests
If the spotlights need to be disabled in the tests, use the ``TestPlatformParameterModule`` to turn off the [enableSpotlightUi](https://github.com/oppia/oppia-android/blob/d2c37dc547f3e5d12dfe62fa97b9b16fbf0fed6e/utility/src/main/java/org/oppia/android/util/platformparameter/PlatformParameterConstants.kt#L218) platform parameter.

```kotlin
TestPlatformParameterModule.forceEnableSpotlightUi(false)
```
###  In production
In order to do it in production, turn the value of ``ENABLE_SPOTLIGHT_UI_DEFAULT_VALUE`` to ``false``.
