package org.oppia.android.domain.oppialogger

import java.security.MessageDigest
import java.util.Random
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.DeviceContextDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale

private const val SESSION_ID_DATA_PROVIDER_ID = "LoggingIdentifierController.session_id"
private const val INSTALLATION_ID_DATA_PROVIDER_ID = "LoggingIdentifierController.installation_id"

/** Controller that handles logging identifiers related operations. */
@Singleton
class LoggingIdentifierController @Inject constructor(
  private val dataProviders: DataProviders,
  @ApplicationIdSeed private val applicationIdSeed: Long,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val persistentCacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger
) {
  private val learnerIdRandom by lazy { Random(applicationIdSeed) }

  // TODO(#4249): Replace this with a StateFlow & the DataProvider with a StateFlow-converted one.
  private var sessionId = AtomicReference(computeSessionId())

  // TODO: finish this.
  private val deviceId =
    UUID.nameUUIDFromBytes(ByteArray(16).also { learnerIdRandom.nextBytes(it) }).toString()
  private val sessionId by lazy { MutableStateFlow(computeSessionId()) }
  private val sessionIdDataProvider by lazy {
    dataProviders.run { sessionId.convertToAutomaticDataProvider(SESSION_ID_DATA_PROVIDER_ID) }
  }
  private val deviceIdStore by lazy {
    persistentCacheStoreFactory.create(
      cacheName = "device_context_database", DeviceContextDatabase.getDefaultInstance()
    ).also {
      it.primeInMemoryAndDiskCacheAsync { database ->
        val deviceId = computeDeviceId()
        database.toBuilder().apply {
          installationId = deviceId
        }.build()
      }.invokeOnCompletion { failure ->
        if (failure != null) {
          oppiaLogger.e(
            "LoggingIdentifierController", "Failed to initialize the installation ID", failure
          )
        }
      }
    }
  }

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
   * Returns a data provider that provides a one-time string which acts as a unique installation ID
   * for the current app installation context.
   *
   * Note that the returned ID is *not* guaranteed to be unique across devices, or even across app
   * installations. It's an approximation for a hardware-based unique ID which is expected to be
   * suitable for short-term user studies.
   */
  fun getInstallationId(): DataProvider<String> {
    return deviceIdStore.transform(
      INSTALLATION_ID_DATA_PROVIDER_ID, DeviceContextDatabase::getInstallationId
    )
  }

  suspend fun fetchInstallationId(): String? =
    deviceIdStore.readDataAsync().await().installationId.takeUnless(String::isEmpty)

  /**
   * Returns an in-memory data provider pointing to a class variable of [sessionId].
   *
   * This ID is unique to each session. A session starts when an exploration begins.
   */
  fun getSessionId(): DataProvider<String> = sessionIdDataProvider

  fun getSessionIdFlow(): StateFlow<String> = sessionId

  /**
   * Regenerates [sessionId] and notifies the data provider.
   *
   * The [sessionId] is generally updated when:
   *  1. An exploration is started/resumed/started-over.
   *  2. Inactivity duration exceeds 30 mins.
   */
  fun updateSessionId() {
    sessionId.value = computeSessionId()
  }

  private fun computeSessionId(): String = learnerIdRandom.randomUuid().toString()

  private fun computeDeviceId(): String {
    return machineLocale.run {
      MessageDigest.getInstance("SHA-1")
        .digest(learnerIdRandom.randomUuid().toString().toByteArray())
        .joinToString("") { "%02x".formatForMachines(it) }
        .substring(startIndex = 0, endIndex = 12)
    }
  }

  /**
   * Returns a new [UUID] using random values sourced from this [Random] that's very similar to the
   * one created by default in [UUID.randomUUID].
   */
  private fun Random.randomUuid(): UUID =
    UUID.nameUUIDFromBytes(ByteArray(16).also { this@randomUuid.nextBytes(it) })
}
