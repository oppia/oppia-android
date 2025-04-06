package org.oppia.android.util.logging

import org.oppia.android.util.platformparameter.APP_AND_OS_DEPRECATION
import org.oppia.android.util.platformparameter.DOWNLOADS_SUPPORT
import org.oppia.android.util.platformparameter.EDIT_ACCOUNTS_OPTIONS_UI
import org.oppia.android.util.platformparameter.ENABLE_MULTIPLE_CLASSROOMS
import org.oppia.android.util.platformparameter.ENABLE_NPS_SURVEY
import org.oppia.android.util.platformparameter.ENABLE_ONBOARDING_FLOW_V2
import org.oppia.android.util.platformparameter.ENABLE_PERFORMANCE_METRICS_COLLECTION
import org.oppia.android.util.platformparameter.EXTRA_TOPIC_TABS_UI
import org.oppia.android.util.platformparameter.FAST_LANGUAGE_SWITCHING_IN_LESSON
import org.oppia.android.util.platformparameter.INTERACTION_CONFIG_CHANGE_STATE_RETENTION
import org.oppia.android.util.platformparameter.LEARNER_STUDY_ANALYTICS
import org.oppia.android.util.platformparameter.LOGGING_LEARNER_STUDY_IDS
import org.oppia.android.util.platformparameter.SPOTLIGHT_UI

/**
 * Converter for feature flag names to integers for more compact logging to address Google
 * Analytics character limits. GA4 limits the characters permitted in a log event parameter value to
 * a maximum of 100 characters as of March 2025.
 *
 * See https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.html#logEvent(java.lang.String,android.os.Bundle)
 */
object FeatureFlagNameToIntegerNameConverter {
  /**
   * Converts a feature flag name to an integer.
   *
   * These numbers are supposed to be incremented indefinitely and the integer representation of
   * each feature flag name should not be reused even after the feature flag is no longer in use.
   *
   * @param flagName the string constant flag name to convert
   * @return an integer representation of the flag name
   */
  fun convertToInteger(flagName: String): Int {
    return when (flagName) {
      LEARNER_STUDY_ANALYTICS -> 1
      ENABLE_PERFORMANCE_METRICS_COLLECTION -> 2
      EDIT_ACCOUNTS_OPTIONS_UI -> 3
      SPOTLIGHT_UI -> 4
      EXTRA_TOPIC_TABS_UI -> 5
      INTERACTION_CONFIG_CHANGE_STATE_RETENTION -> 6
      DOWNLOADS_SUPPORT -> 7
      APP_AND_OS_DEPRECATION -> 8
      FAST_LANGUAGE_SWITCHING_IN_LESSON -> 9
      LOGGING_LEARNER_STUDY_IDS -> 10
      ENABLE_NPS_SURVEY -> 11
      ENABLE_ONBOARDING_FLOW_V2 -> 12
      ENABLE_MULTIPLE_CLASSROOMS -> 13
      else -> 0
    }
  }
}
