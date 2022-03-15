package org.oppia.android.util.logging.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventBundleCreator
import org.oppia.android.util.logging.EventLogger
import org.oppia.android.util.logging.SyncStatusManager
import java.util.Locale
import javax.inject.Singleton

private const val NETWORK_USER_PROPERTY = "NETWORK"
private const val COUNTRY_USER_PROPERTY = "COUNTRY"

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val eventBundleCreator: EventBundleCreator,
  private val syncStatusManager: SyncStatusManager,
  context: Context
) : EventLogger {
  private var bundle = Bundle()
  private val connectivityManager: ConnectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  /** Logs an event to Firebase Analytics with [NETWORK_USER_PROPERTY] and [COUNTRY_USER_PROPERTY]. */
  override fun logEvent(eventLog: EventLog) {
    bundle = eventBundleCreator.createEventBundle(eventLog)
    firebaseAnalytics.logEvent(eventLog.context.activityContextCase.name, bundle)
    // TODO(#3792): Remove this usage of Locale.
    firebaseAnalytics.setUserProperty(COUNTRY_USER_PROPERTY, Locale.getDefault().displayCountry)
    firebaseAnalytics.setUserProperty(
      NETWORK_USER_PROPERTY, connectivityManager.activeNetworkInfo.typeName
    )
    syncStatusManager.setSyncStatus(SyncStatusManager.SyncStatus.DATA_UPLOADED)
  }

  override fun logCachedEvent(eventLog: EventLog) {
    bundle = eventBundleCreator.createEventBundle(eventLog)
    firebaseAnalytics.logEvent(eventLog.context.activityContextCase.name, bundle)
    // TODO(#3792): Remove this usage of Locale.
    firebaseAnalytics.setUserProperty(COUNTRY_USER_PROPERTY, Locale.getDefault().displayCountry)
    firebaseAnalytics.setUserProperty(
      NETWORK_USER_PROPERTY, connectivityManager.activeNetworkInfo.typeName
    )
  }
}
