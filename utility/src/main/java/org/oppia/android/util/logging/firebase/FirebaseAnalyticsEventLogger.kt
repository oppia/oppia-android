package org.oppia.android.util.logging.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.EventBundleCreator
import org.oppia.android.util.logging.AnalyticsEventLogger
import org.oppia.android.util.networking.NetworkConnectionUtil
import java.util.Locale
import javax.inject.Inject

private const val NETWORK_USER_PROPERTY = "NETWORK"
private const val COUNTRY_USER_PROPERTY = "COUNTRY"

/** Logger for event logging to Firebase Analytics. */
class FirebaseAnalyticsEventLogger private constructor(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val eventBundleCreator: EventBundleCreator
) : AnalyticsEventLogger {
  /**
   * Logs an event to Firebase Analytics with [NETWORK_USER_PROPERTY] and [COUNTRY_USER_PROPERTY].
   */
  override fun logEvent(eventLog: EventLog) {
    Bundle().let {
      firebaseAnalytics.logEvent(eventBundleCreator.fillEventBundle(eventLog, it), it)
    }
    // TODO(#3792): Remove this usage of Locale.
    firebaseAnalytics.setUserProperty(COUNTRY_USER_PROPERTY, Locale.getDefault().displayCountry)
    firebaseAnalytics.setUserProperty(NETWORK_USER_PROPERTY, getNetworkStatus())
  }

  private fun getNetworkStatus(): String {
    return when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.LOCAL ->
        NetworkConnectionUtil.ProdConnectionStatus.LOCAL.logName
      NetworkConnectionUtil.ProdConnectionStatus.CELLULAR ->
        NetworkConnectionUtil.ProdConnectionStatus.CELLULAR.logName
      else -> NetworkConnectionUtil.ProdConnectionStatus.NONE.logName
    }
  }

  /** Application-scoped injectable factory for creating new [FirebaseAnalyticsEventLogger]s. */
  @SuppressLint("MissingPermission") // This is a false warning probably due to the IJwB plugin.
  class Factory @Inject constructor(
    private val context: Context,
    private val networkConnectionUtil: NetworkConnectionUtil,
    private val eventBundleCreator: EventBundleCreator
  ) {
    private val firebaseAnalytics by lazy {
      FirebaseAnalytics.getInstance(context.applicationContext)
    }

    /**
     * Returns a new [FirebaseAnalyticsEventLogger] for the current application context.
     *
     * Generally, only one of these needs to be created per application.
     */
    fun create(): AnalyticsEventLogger =
      FirebaseAnalyticsEventLogger(firebaseAnalytics, networkConnectionUtil, eventBundleCreator)
  }
}
