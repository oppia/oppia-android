package org.oppia.android.domain.oppialogger

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.DeviceContextDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.locale.OppiaLocale
import java.security.MessageDigest
import java.util.Random
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
  // Use a base random to compute the per-ID random IDs in order so that test behaviors are
  // consistent and deterministic when fixing the application ID seed.
  private val baseRandom = Random(applicationIdSeed)
  private val installationRandomSeed = baseRandom.nextLong()
  private val sessionRandomSeed = baseRandom.nextLong()
  private val learnerRandomSeed = baseRandom.nextLong()
  private val installationIdRandom by lazy { Random(installationRandomSeed) }
  private val sessionIdRandom by lazy { Random(sessionRandomSeed) }
  private val learnerIdRandom by lazy { Random(learnerRandomSeed) }

  private val sessionId by lazy { MutableStateFlow(computeSessionId()) }
  private val sessionIdDataProvider by lazy {
    dataProviders.run { sessionId.convertToAutomaticDataProvider(SESSION_ID_DATA_PROVIDER_ID) }
  }
  private val installationIdStore by lazy {
    persistentCacheStoreFactory.create(
      cacheName = "device_context_database", DeviceContextDatabase.getDefaultInstance()
    ).also {
      it.primeInMemoryAndDiskCacheAsync(
        updateMode = UpdateMode.UPDATE_IF_NEW_CACHE,
        publishMode = PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
      ) { database ->
        database.toBuilder().apply { installationId = computeInstallationId() }.build()
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
    return installationIdStore.transform(
      INSTALLATION_ID_DATA_PROVIDER_ID, DeviceContextDatabase::getInstallationId
    )
  }

  /**
   * Returns the most recently known installation ID per [getInstallationId].
   *
   * Since checking for the latest installation ID is inherently asynchronous, this operation should
   * only be called from a background coroutine.
   */
  suspend fun fetchInstallationId(): String? =
    installationIdStore.readDataAsync().await().installationId.takeUnless(String::isEmpty)

  /**
   * Returns an in-memory data provider pointing to a class variable of [sessionId].
   *
   * This ID is unique to each session. A session starts when a learner logs in.
   */
  fun getSessionId(): DataProvider<String> = sessionIdDataProvider

  /**
   * Returns the [StateFlow] backing the current session ID indicated by [getSessionId].
   *
   * Where the [DataProvider] returned by [getSessionId] can be composed by domain controllers or
   * observed by the UI layer, the [StateFlow] returned by this method can be observed in background
   * contexts.
   */
  fun getSessionIdFlow(): StateFlow<String> = sessionId

  /**
   * Regenerates [sessionId] and notifies the data provider.
   *
   * The [sessionId] is generally updated when:
   *  1. A learner logs into their profile.
   *  2. Inactivity duration exceeds the maximum time limit for an active session.
   */
  fun updateSessionId() {
    sessionId.value = computeSessionId()
  }

  private fun computeSessionId(): String = sessionIdRandom.randomUuid().toString()

  private fun computeInstallationId(): String {
    return machineLocale.run {
      MessageDigest.getInstance("SHA-1")
        .digest(installationIdRandom.randomUuid().toString().toByteArray())
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
