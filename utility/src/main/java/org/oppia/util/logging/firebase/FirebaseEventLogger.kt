package org.oppia.util.logging.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import java.util.*
import javax.inject.Singleton

const val NETWORK_USER_PROPERTY = "NETWORK"
const val COUNTRY_USER_PROPERTY = "COUNTRY"

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val eventBundleCreator: EventBundleCreator,
  private val context: Context
) : EventLogger {
  private var bundle = Bundle()
  private val connectivityManager: ConnectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  /** Logs an event to Firebase Analytics with [NETWORK_USER_PROPERTY] and [COUNTRY_USER_PROPERTY]. */
  override fun logEvent(eventLog: EventLog) {
    bundle = eventBundleCreator.createEventBundle(eventLog)
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
    firebaseAnalytics.setUserProperty(COUNTRY_USER_PROPERTY, Locale.getDefault().displayCountry)
    firebaseAnalytics.setUserProperty(
      NETWORK_USER_PROPERTY, connectivityManager.activeNetworkInfo.typeName
    )
  }
}
