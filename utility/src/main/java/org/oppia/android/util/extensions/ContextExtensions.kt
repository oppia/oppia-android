package org.oppia.android.util.extensions

import android.content.Context

// Extension functions for Context that act as getters for PackageManager.

/** Returns the current app version name. */
fun Context.getVersionName(): String {
  // TODO(#3616): Migrate to the proper SDK 28+ APIs.
  @Suppress("DEPRECATION") // The code is correct for targeted versions of Android.
  return this.packageManager.getPackageInfo(this.packageName, /* flags = */ 0).versionName
}

/** Returns the current app version code. */
fun Context.getVersionCode(): Int {
  // TODO(#3616): Migrate to the proper SDK 28+ APIs.
  @Suppress("DEPRECATION") // The code is correct for targeted versions of Android.
  return this.packageManager.getPackageInfo(this.packageName, /* flags = */ 0).versionCode
}

/** Returns the time at which the app was last updated. */
fun Context.getLastUpdateTime(): Long {
  // TODO(#3616): Migrate to the proper SDK 28+ APIs.
  @Suppress("DEPRECATION") // The code is correct for targeted versions of Android.
  return this.packageManager.getPackageInfo(this.packageName, /* flags = */ 0).lastUpdateTime
}
