package org.oppia.app.utility

import android.content.Context
import android.content.pm.PackageManager
import javax.inject.Inject

/** Extension functions for PackageManager. */
@Inject
lateinit var context: Context

fun PackageManager.getVersionName(): String {
  return getPackageInfo(context.packageName, /* flags=*/0).versionName
}

fun PackageManager.getLastUpdateTime(): Long {
  return context.packageManager.getPackageInfo(
    context.packageName,
    /* flags= */ 0
  ).lastUpdateTime
}
