package org.oppia.android.domain.oppialogger

import android.content.Context
import android.provider.Settings
import com.google.firebase.installations.FirebaseInstallations
import org.oppia.android.domain.util.getSecureString
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.system.UUIDWrapper
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.system.UserIdGenerator
import java.io.File
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_ID_DATA_PROVIDER_ID = "LoggingIdentifierController.session_id"
private const val FIREBASE_ID_DATA_PROVIDER_ID = "LoggingIdentifierController.firebase_id"

/** Controller that handles logging identifiers related operations. */
@Singleton
class LoggingIdentifierController @Inject constructor(
  private val context: Context,
  private val dataProviders: DataProviders,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  @ApplicationIdSeed private val applicationIdSeed: Long,
  private val userIdGenerator: UserIdGenerator,
  private val machineLocale: OppiaLocale.MachineLocale
) {
  private val learnerIdRandom by lazy { Random(applicationIdSeed) }

  // TODO(#4249): Replace this with a StateFlow & the DataProvider with a StateFlow-converted one.
  private var sessionId = AtomicReference(computeSessionId())

  /**
   * Creates and returns a unique identifier which will be used to identify the current learner.
   *
   * Each call to this function will return a unique learner ID, so it's up to the caller to ensure
   * long-lived IDs are properly persisted.
   */
  fun createLearnerId(): String = machineLocale.run {
    "%08x".formatForMachines(learnerIdRandom.nextInt())
  }

  /**
   * Returns a data provider that provides a one-time string which acts as a unique device ID for
   * the current installation environment.
   *
   * Note that the returned ID is *not* guaranteed to be unique across devices, or even across app
   * installations. It's an approximation for a hardware-based unique ID which is expected to be
   * suitable for short-term user studies.
   */
  fun getDeviceId(): DataProvider<String> = retrieveApproximatedUniqueDeviceId()

  /**
   * Returns an in-memory data provider pointing to a class variable of [sessionId].
   *
   * This ID is unique to each session. A session starts when an exploration begins.
   */
  fun getSessionId(): DataProvider<String> {
    return dataProviders.createInMemoryDataProvider(SESSION_ID_DATA_PROVIDER_ID) {
      return@createInMemoryDataProvider sessionId.get()
    }
  }

  /**
   * Regenerates [sessionId] and notifies the data provider.
   *
   * The [sessionId] is generally updated when:
   *  1. An exploration is started/resumed/started-over.
   *  2. Inactivity duration exceeds 30 mins.
   */
  fun updateSessionId() {
    sessionId.set(computeSessionId())
    asyncDataSubscriptionManager.notifyChangeAsync(SESSION_ID_DATA_PROVIDER_ID)
  }

  private fun computeSessionId(): String = userIdGenerator.generateRandomUserId()

  private fun retrieveApproximatedUniqueDeviceId(): DataProvider<String> {
    // TODO(#4249): Replace this with a StateFlow, too, for simplicity.
    // Per https://developer.android.com/training/articles/user-data-ids and
    // https://stackoverflow.com/a/2785493/3689782 there's no reliable way to compute a device ID.
    // The following is an unreliable approximation that's more or less tied to the installation of
    // the app, though *an* ID is at least guaranteed to be returned.
    val fidResult = AtomicReference(AsyncResult.pending<String>())
    FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
      val fid = if (task.isSuccessful) task.result else null
      val deviceId = fid ?: retrieveSecureAndroidId() ?: computeInstallTimeBasedId()
      val hashedId = machineLocale.run {
        MessageDigest.getInstance("SHA-1")
          .digest(deviceId.toByteArray())
          .joinToString("") { "%02x".formatForMachines(it) }
          .substring(startIndex = 0, endIndex = 12)
      }
      fidResult.set(AsyncResult.success(hashedId))
      asyncDataSubscriptionManager.notifyChangeAsync(FIREBASE_ID_DATA_PROVIDER_ID)
    }
    return dataProviders.createInMemoryDataProviderAsync(FIREBASE_ID_DATA_PROVIDER_ID) {
      fidResult.get()
    }
  }

  private fun retrieveSecureAndroidId(): String? =
    context.getSecureString(Settings.Secure.ANDROID_ID)

  private fun computeInstallTimeBasedId(): String {
    // Use the package's install time as a (poor) proxy for a device ID since it's at least
    // guaranteed to be present.
    val appInfo = context.packageManager.getApplicationInfo("org.oppia.android", /* flags= */ 0)
    return machineLocale.run { "%02x".formatForMachines(File(appInfo.sourceDir).lastModified()) }
  }
}
