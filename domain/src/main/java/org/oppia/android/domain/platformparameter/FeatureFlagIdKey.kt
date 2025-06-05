package org.oppia.android.domain.platformparameter

import dagger.MapKey
import org.oppia.android.app.model.FeatureFlagId

/**
 * Key for [FeatureFlags] to enable keying on [FeatureFlagId] for an injectable map of feature
 * flags.
 */
@MapKey annotation class FeatureFlagIdKey(val id: FeatureFlagId)
