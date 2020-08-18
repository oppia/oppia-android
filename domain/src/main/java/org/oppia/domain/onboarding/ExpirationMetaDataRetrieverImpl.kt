package org.oppia.domain.onboarding

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import javax.inject.Inject

/**
 * Implementation of [ExpirationMetaDataRetriever] that uses Android's package manager to retrieve
 * the values from the application's manifest.
 */
class ExpirationMetaDataRetrieverImpl @Inject constructor(
  private val context: Context
) : ExpirationMetaDataRetriever {
  override fun getMetaData(): Bundle? {
    return context.packageManager.getApplicationInfo(
      context.packageName, PackageManager.GET_META_DATA
    ).metaData
  }
}
