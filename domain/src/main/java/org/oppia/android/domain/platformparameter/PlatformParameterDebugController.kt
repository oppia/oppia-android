package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.util.data.DataProvider

/** Debug-only controller for accessing resolved platform parameter and feature flag values. */
interface PlatformParameterDebugController {

  /** Returns a merged list of platform parameters by resolving values. */
  fun loadEphemeralPlatformParameters(): DataProvider<List<EphemeralPlatformParameter>>

  /** Returns a merged list of feature flags by resolving values. */
  fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>>
}
