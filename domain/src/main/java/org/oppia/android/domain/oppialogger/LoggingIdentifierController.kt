package org.oppia.android.domain.oppialogger

import android.content.Context
import android.net.wifi.WifiManager
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.system.UUIDWrapper
import java.security.MessageDigest
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_ID_DATA_PROVIDER_ID = "session_id_data_provider_id"

/** Controller that handles logging identifiers related operations. */
@Singleton
class LoggingIdentifierController @Inject constructor(
  context: Context,
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  @DeviceIdSeed private val deviceIdSeed: Long,
  private val uuidWrapper: UUIDWrapper
) {

  private val wifiManager =
    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

  /**
   * Returns a string which acts as the [deviceId] for the current device.
   * This ID will remain common across all profiles on the current device.
   */
  val deviceId by lazy {
    computeDeviceId()
  }

  private var sessionId: String = computeSessionId()

  /** Creates and returns a unique identifier which will be used to identify the current learner. */
  fun createLearnerId(): String = String.format("%08x", Random(deviceIdSeed).nextInt())

  private fun computeSessionId(): String = uuidWrapper.randomUUIDString()

  /**
   * Returns an in-memory data provider pointing to a class variable of [sessionId].
   * This ID is unique to each session. A session starts when an exploration begins.
   */
  fun getSessionId(): DataProvider<String> {
    return dataProviders.createInMemoryDataProvider(SESSION_ID_DATA_PROVIDER_ID) {
      return@createInMemoryDataProvider sessionId
    }
  }

  /** Regenerates [sessionId] and notifies the data provider.
   * The [sessionId] is updated when:
   *  1. An exploration is started/resumed/started-over.
   *  2. Inactivity duration exceeds 30 mins.
   */
  fun updateSessionId() {
    sessionId = computeSessionId()
    asyncDataSubscriptionManager.notifyChangeAsync(SESSION_ID_DATA_PROVIDER_ID)
  }

  private fun computeDeviceId(): String {
    return MessageDigest.getInstance("SHA-1")
      .digest(wifiManager.connectionInfo.macAddress.toByteArray())
      .joinToString("") { "%02x".format(it) }.substring(startIndex = 0, endIndex = 12)
  }
}