package org.oppia.android.util.extensions

import android.content.Context
import android.widget.Toast

// Extension functions for Context that act as getters for PackageManager.

/** Returns the current app version name. */
fun Context.getVersionName(): String {
  return this.packageManager.getPackageInfo(this.packageName, /* flags= */ 0).versionName
}

/** Returns the current app version code. */
fun Context.getVersionCode(): Int {
  return this.packageManager.getPackageInfo(this.packageName, /* flags= */ 0).versionCode
}

/** Returns the time at which the app was last updated. */
fun Context.getLastUpdateTime(): Long {
  return this.packageManager.getPackageInfo(this.packageName, /* flags= */ 0).lastUpdateTime
}
