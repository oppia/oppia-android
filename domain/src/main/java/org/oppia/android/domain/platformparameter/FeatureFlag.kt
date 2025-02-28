package org.oppia.android.domain.platformparameter

// NOTE: Injections will look something like this (assuming direct import of the ID):
//   @FeatureFlag(DOWNLOADS_SUPPORT) val isDownloadsSupportEnabled: Boolean

@Qualifier annotation class FeatureFlag(val id: FeatureFlagId)
