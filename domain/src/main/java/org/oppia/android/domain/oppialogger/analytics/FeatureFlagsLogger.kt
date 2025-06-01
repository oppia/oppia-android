package org.oppia.android.domain.oppialogger.analytics

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.FeatureFlagItemContext
import org.oppia.android.app.model.EventLog.FeatureFlagListContext
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.domain.platformparameter.FeatureFlags
import org.oppia.android.util.platformparameter.PlatformParameterValue

/**
 * Convenience logger for feature flags.
 *
 * This logger is meant to be used for feature flag-related logging on every app launch. It is
 * primarily used within the ApplicationLifecycleObserver to log the status of feature flags in a
 * given app session.
 */
@Singleton
class FeatureFlagsLogger @Inject constructor(
  private val analyticsController: AnalyticsController,
  @FeatureFlags
  private val featureFlagsProvider: Provider<Map<FeatureFlagId, PlatformParameterValue<Boolean>>>
) {
  private var featureFlagOverride: Map<FeatureFlagId, PlatformParameterValue<Boolean>>? = null
  private val featureFlagItemMap: Map<FeatureFlagId, PlatformParameterValue<Boolean>>
    get() = featureFlagOverride ?: featureFlagsProvider.get()

  /**
   * This method can be used to override the featureFlagItemMap and sets its value to the given map.
   *
   * @param featureFlagItemMap denotes the map of feature flag names to their corresponding
   * [PlatformParameterValue]s
   */
  fun setFeatureFlagItemMap(
    featureFlagItemMap: Map<FeatureFlagId, PlatformParameterValue<Boolean>>) {
    featureFlagOverride = featureFlagItemMap
  }

  /**
   * This method logs the name, enabled status and sync status of all feature flags to Firebase.
   *
   * This method should not be called until platform parameters and feature flags have been fully
   * loaded (i.e. not too early in the application lifecycle).
   *
   * @param appSessionId denotes the id of the current appInForeground session
   */
  fun logAllFeatureFlags(appSessionId: String) {
    val featureFlagItemList = mutableListOf<FeatureFlagItemContext>()
    for (flag in featureFlagItemMap) {
      featureFlagItemList.add(
        createFeatureFlagItemContext(flag)
      )
    }

    // TODO(#5341): Set the UUID value for this context
    val featureFlagContext = FeatureFlagListContext.newBuilder()
      .setAppSessionId(appSessionId)
      .addAllFeatureFlags(featureFlagItemList)
      .build()

    analyticsController.logLowPriorityEvent(
      EventLog.Context.newBuilder()
        .setFeatureFlagListContext(featureFlagContext)
        .build(),
      profileId = null
    )
  }

  /**
   * Creates an [EventLog] context for the feature flags to be logged.
   *
   * @param flagDetails denotes the key-value pair of the feature flag name and its corresponding
   * [PlatformParameterValue]
   * @return an [EventLog.Context] for the feature flags to be logged
   */
  private fun createFeatureFlagItemContext(
    flagDetails: Map.Entry<FeatureFlagId, PlatformParameterValue<Boolean>>,
  ): FeatureFlagItemContext {
    return FeatureFlagItemContext.newBuilder().apply {
      id = flagDetails.key
      isEnabled = flagDetails.value.value
      this.syncStatus = flagDetails.value.syncStatus
    }.build()
  }
}
