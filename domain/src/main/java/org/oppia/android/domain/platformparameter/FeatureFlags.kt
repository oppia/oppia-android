package org.oppia.android.domain.platformparameter

import javax.inject.Qualifier

/**
 * Qualifier for an application-injectable map of feature flags keyed on
 * [org.oppia.android.app.model.FeatureFlagId].
 */
@Qualifier annotation class FeatureFlags
