package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.FeatureFlagId
import javax.inject.Qualifier

// NOTE: Injections will look something like this (assuming direct import of the ID):
//   @FeatureFlag(DOWNLOADS_SUPPORT) val isDownloadsSupportEnabled: Boolean

@Qualifier
annotation class FeatureFlag(val id: FeatureFlagId)
