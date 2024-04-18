## Table of Contents

- [Why do we need feature flags?](#why-do-we-need-feature-flags)
- [When to use feature flags?](#when-to-use-feature-flags)
- [How to use feature flags?](#how-to-use-feature-flags)
  - [Development stage](#development-stage)
  - [Pre-launch testing stage](#pre-launch-testing-stage)
- [Post-launch testing](#post-launch-testing)

When developing a new feature, you might want to limit the scope of the feature so that it's only enabled when certain criteria are met (e.g. only enabled in dev environment). In these cases, you can use the feature gating system to gate the enabling of the features with a feature flag.

## Why do we need feature flags?

Feature flags make the development process much easier by letting developers safely introduce new features to users. They also give us the ability to quickly turn off features if they cause issues after being launched.

Feature flags are handy for hiding features that are still in the development or testing phase. This way, developers can gradually add the feature to the code and only activate it when it's fully ready for everyone to use. If something goes wrong with the feature, feature flags make it easy to turn it off without undoing all the changes. This is especially helpful for big or complex features.

Moreover, feature flags help separate the release of the feature from the release of the app itself. This means we can update the app's behavior, when the new feature is ready to launch, with fewer chances of things going wrong.


## When to use feature flags?

Feature flags should be used when you are working on a feature whose scale makes it hard to implement all at once, or for more experimental features that are likely to cause breakages. Essentially, feature flags are a must for features that are not yet ready for production/fully-tested at the time that they're merged into develop.


## How to use feature flags?

Suppose you are working on a large scale user-facing feature that will take 1 or more PRs to fully implement. In such a case, please use feature flags to gate your feature appropriately by carefully following the steps listed below:


### Development Stage

1. First, create a PR on Web following [these instructions](https://github.com/oppia/oppia/wiki/Launching-new-features#follow-the-steps-below-to-add-a-new-feature-flag) that introduces the feature flag, so that it can be used by Android. The name of the feature flag must start with `android_` and the feature flag should be in DEV stage.

1. While waiting for that PR to be merged, create a PR on Android following the instructions in [Platform Parameters & Feature Flags](https://github.com/oppia/oppia-android/wiki/Platform-Parameters-&-Feature-Flags) that introduces your feature and adds a new feature flag that is meant to be used with this new feature. The feature flag should be placed in the DEV stage and disabled by default, so that it cannot accidentally be turned on in environments for which it is not yet ready (like the alpha/beta/GA release channels). Every single user-facing aspect of the feature -- whether frontend or backend -- must be gated behind the feature flag (both in the first PR, and all the following ones as well). This is to ensure that the feature is not visible to the user until it is fully implemented and ready for production.

1. The first PR above must be merged before any subsequent Android PRs are merged. This is to ensure that the feature flag is available in the codebase for it to be used in the following PRs.


### Pre-launch testing stage**

Testing is crucial to ensure that the app's core functionalities function properly. This early identification of major bugs and stability issues saves time and resources in the long run. We use the testing (pre-alpha) version of the app for this.

1. When your feature is fully implemented and ready for testing, submit a follow-up PR on Web to move your feature to the TEST stage.

1. Once that PR is merged, fill in [this form](https://forms.gle/rUJaHJSpRGemtGDp6) to ask the Web server admins to update the feature flag's stage to TEST on the Web server. (This will make the feature available in both the testing and alpha channels of the Android app.)

1. While waiting for the above two requests to be completed, fill in [this form](https://docs.google.com/forms/d/e/1FAIpQLSdFlDXwXzZuCqCsGACzchZCUdahqwL1bqgRQYMf5zNn1SUqxQ/viewform) to request a new feature test. This will send a new request to the Android release team.

1. Work with the Android release team to organize a test of your feature. This will entail the following:
  - The release team will deploy a new testing version of the app with the feature flag for your feature set to True. (This is currently done through a compile-time flag, but will later switch to using the Web feature flag defined in step 1 above.)
  - The release team will send instructions to the feature testers for how to test the feature. Feature testers will use this to conduct the testing, by filling in the [feature review template](https://docs.google.com/document/d/1Uj5XFzDjthBI0ze-sgYZxD8qs8nTxsZso4Rd--DDffs/edit?usp=sharing).

1. As the feature owner, you should analyze and address any feedback received from testers. This includes bug reports, suggestions for improvement, and overall impressions of the feature.
  - If the feature testing surfaces blocking issues that need to be fixed prior to the feature’s release, you must work on fixing the highlighted issues before proceeding further. (Any issue that the feature owner, PM, or Android team lead considers blocking should be regarded as such.) You can request a re-test once all the testing feedback is addressed.

1. If the testing results are positive and the feature is approved by the QA team (i.e. it’s been fully tested, and all issues filed have been fixed + verified), your feature is ready for launch! Create a PR on the Web codebase that moves the feature flag to the PROD stage, allowing it to be enabled/disabled in production and have its min-version set (by the release coordinator(s)). **NOTE: When opening this PR, include a link to the testing doc or other proof that the feature has been approved for release.** While this PR is open, confirm (through discussion) with the Android QA team that the new CUJs for this feature have been added to the overall CUJs used for testing Android releases in general.
  - Once this PR is merged, send a ["job run request"](https://forms.gle/rUJaHJSpRGemtGDp6) to the release coordinators to turn on the feature in production by adding a rule in the /release-coordinator page.
  - We recommend filling in [this form](https://goo.gl/forms/sNBWrW03fS6dBWEp1) to announce your feature to the public once it's launched!


## Post-launch testing

We need to do some cleanup after the feature is successfully running in production:

1. Once the feature is confirmed to be functioning as intended in production (at least 10% of the 7DA user base is on a version of the app that has the feature enabled) by the product team, please do the following, in order:
  - Make sure that the feature is ready to be made permanent. To do this, confirm with the PMs that no users have reported issues with it, and that no regressions have been detected via Android Play Store / Firebase analytics or general user feedback.
  - The PMs should also fill in this [post-launch review template](https://docs.google.com/document/d/1DifFAe3oRzjmVPh2fEllfAky4n0QMAXVQc3Y580qkr8/edit).

1. Once you have confirmation that the feature can be made permanent, create a PR in the Android codebase to remove all remaining references to the feature flag from the codebase (for example, in all the `if` blocks you created to gate the feature).

1. Finally, merge one last Web PR to move the feature flag to the "deprecated" stage (one of the stages listed in `core/domain/platform_feature_list.py`, meant for flags that are no longer in use).
