package org.oppia.android.domain.platformparameter

import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.oppia.android.app.model.EphemeralFeatureFlag
import org.oppia.android.app.model.EphemeralPlatformParameter
import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.LocalOverridePlatformParameterDatabase
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.model.OverriddenPlatformParameter
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterId
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.backends.gae.api.PlatformParameterDebugService
import org.oppia.android.data.backends.gae.model.GaeFeatureFlag
import org.oppia.android.data.backends.gae.model.GaePlatformParameter
import org.oppia.android.data.backends.gae.model.GaePlatformParameterValue
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.extensions.safeForEach
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.properties.CustomPropertyRetriever
import org.oppia.android.util.threading.BackgroundDispatcher
import retrofit2.HttpException
import retrofit2.await
import javax.inject.Inject

/**
 * Debug implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
class PlatformParameterControllerDebugImpl @Inject constructor(
  private val platformParameterControllerProdImpl: PlatformParameterControllerProdImpl,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  private val processState: PlatformParameterProcessState,
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val platformParameterDebugService: PlatformParameterDebugService,
  private val customPropertyRetriever: CustomPropertyRetriever,
  private val exceptionLogger: ExceptionLogger,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) : PlatformParameterController {

  private val databaseStore by lazy {
    cacheStoreFactory.create(
      DATABASE_NAME, LocalOverridePlatformParameterDatabase.getDefaultInstance()
    )
  }

  // Note that the 'by lazy' here guarantees thread-safe and singleton initialization.
  private val initializationDeferred by lazy { loadParametersInternalAsync() }
  private val parametersAreLoadedFlow by lazy { MutableStateFlow(false) }
  private val downloadRemoteParametersProvider by lazy { downloadRemoteParametersInternal() }
  private var ongoingDownloadTask: Deferred<Unit>? = null

  init {
    // Ensure that parameters and flags are fully loaded ahead of a call to retrieveData() since
    // loadRemotePlatformParameters() needs to guarantee that the existing parameters are loaded
    // despite any later calls to downloadRemoteParameters(). Note that this will also guarantee
    // that the file is created on-disk (which is fine--it will just initially be empty).
    databaseStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { failure ->
      failure?.let {
        oppiaLogger.e(
          "PlatformParameterController", "Failed to load platform params/feature flags.", failure
        )
      }
    }
  }

  override fun loadParametersAsync() = initializationDeferred

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return dataProviders.run {
      parametersAreLoadedFlow.convertToAutomaticDataProvider(
        GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID
      )
    }
  }

  override fun downloadRemoteParameters() = downloadRemoteParametersProvider

  /** Cancels any ongoing remote parameter download. */
  fun cancelRemoteParameterDownload(): Boolean {
    ongoingDownloadTask?.cancel()
    ongoingDownloadTask = null
    oppiaLogger.d("PlatformParameterController", "Cancelled ongoing remote parameter download")
    return true
  }

  /** Loads the locally overridden feature flags from the database. */
  suspend fun loadLocalOverriddenFeatureFlags(): List<OverriddenFeatureFlag> {
    return databaseStore.readDataAsync().await().overriddenFeatureFlagList
  }

  /** Loads the locally overridden platform parameters from the database. */
  suspend fun loadLocalOverriddenPlatformParameters(): List<OverriddenPlatformParameter> {
    return databaseStore.readDataAsync().await().overriddenPlatformParameterList
  }

  /**
   * Returns a [DataProvider] that loads the current values of all supported
   * feature flags as a list of [EphemeralFeatureFlag].
   *
   * For each flag, uses a remote override if available; otherwise falls
   * back to its default value, with the appropriate [SyncStatus].
   */
  fun loadEphemeralFeatureFlags(): DataProvider<List<EphemeralFeatureFlag>> {
    return dataProviders.createInMemoryDataProviderAsync(
      LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID
    ) {
      val defaultFlags = platformParameterControllerProdImpl.loadSupportedFeatureFlags()
      val remoteFlags = platformParameterControllerProdImpl.loadRemoteFeatureFlags()
      val remoteFlagById = remoteFlags.associateBy { it.id }

      val localFlags = loadLocalOverriddenFeatureFlags()
      val localFlagById = localFlags.associateBy { it.id }

      val ephemeralFlags = defaultFlags.map { flagDefinition ->
        val localFlag = localFlagById[flagDefinition.id]
        val remoteFlag = remoteFlagById[flagDefinition.id]

        val currentValue = localFlag?.overriddenValue
          ?: remoteFlag?.remoteIsEnabled
          ?: flagDefinition.defaultIsEnabled

        val nonOverriddenValue = remoteFlag?.remoteIsEnabled
          ?: flagDefinition.defaultIsEnabled

        val nonOverriddenSyncStatus = remoteFlag?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        val syncStatus = localFlag?.let { SyncStatus.LOCAL_OVERRIDE }
          ?: remoteFlag?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralFeatureFlag.newBuilder().apply {
          this.id = flagDefinition.id
          this.currentValue = currentValue
          this.nonOverriddenValue = nonOverriddenValue
          this.nonOverriddenSyncStatus = nonOverriddenSyncStatus
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralFlags)
    }
  }

  /**
   * Returns a [DataProvider] that loads the current values of all supported
   * platform parameters as a list of [EphemeralPlatformParameter].
   *
   * Each parameter uses a remote override if available, otherwise falls
   * back to its default value with the appropriate [SyncStatus].
   */
  fun loadEphemeralPlatformParameters(): DataProvider<List<EphemeralPlatformParameter>> {
    return dataProviders.createInMemoryDataProviderAsync(
      LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID
    ) {
      val defaultParameters = platformParameterControllerProdImpl.loadSupportedPlatformParameters()
      val remoteParameters = platformParameterControllerProdImpl.loadRemotePlatformParameters()
      val remoteParamById = remoteParameters.associateBy { it.id }

      val localParameters = loadLocalOverriddenPlatformParameters()
      val localParamsById = localParameters.associateBy { it.id }

      val ephemeralParameters = defaultParameters.map { paramDefinition ->
        val remoteParam = remoteParamById[paramDefinition.id]
        val localParam = localParamsById[paramDefinition.id]

        val currentValue = localParam?.overriddenValue
          ?: remoteParam?.remoteValue
          ?: paramDefinition.defaultValue

        val nonOverriddenValue = remoteParam?.remoteValue
          ?: paramDefinition.defaultValue

        val nonOverriddenSyncStatus = remoteParam?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        val syncStatus = localParam?.let { SyncStatus.LOCAL_OVERRIDE }
          ?: remoteParam?.syncStatus
          ?: SyncStatus.NOT_SYNCED_FROM_SERVER

        EphemeralPlatformParameter.newBuilder().apply {
          this.id = paramDefinition.id
          this.currentValue = currentValue
          this.nonOverriddenValue = nonOverriddenValue
          this.nonOverriddenSyncStatus = nonOverriddenSyncStatus
          this.syncStatus = syncStatus
        }.build()
      }

      return@createInMemoryDataProviderAsync AsyncResult.Success(ephemeralParameters)
    }
  }

  private fun loadParametersInternalAsync(): Deferred<Unit> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      val ephemeralPlatformParametersResult =
        loadEphemeralPlatformParameters().retrieveData()
      val ephemeralFeatureFlagsResult = loadEphemeralFeatureFlags().retrieveData()
      val platStatesById = when (ephemeralPlatformParametersResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral platform parameters:",
            ephemeralPlatformParametersResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralPlatformParametersResult.value.associate { it.id to it.currentValue }
        is AsyncResult.Pending -> emptyMap()
      }

      val flagStatesById = when (ephemeralFeatureFlagsResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral feature flags:",
            ephemeralFeatureFlagsResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralFeatureFlagsResult.value.associate { it.id to it.currentValue }
        else -> emptyMap()
      }

      val statusesById = when (ephemeralFeatureFlagsResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "PlatformParameterControllerDebugImpl",
            "Failed to load ephemeral feature flag sync statuses:",
            ephemeralFeatureFlagsResult.error
          )
          emptyMap()
        }
        is AsyncResult.Success ->
          ephemeralFeatureFlagsResult.value.associate { it.id to it.syncStatus }
        else -> emptyMap()
      }

      processState.initializePlatformParameters(platStatesById)
      processState.initializeFeatureFlags(flagStatesById)
      processState.initializeFeatureFlagSyncStatuses(statusesById)

      // Let observers know that parameters have been initialized.
      parametersAreLoadedFlow.value = true
    }
  }

  private fun downloadRemoteParametersInternal(): DataProvider<Unit> {
    return dataProviders.createInMemoryDataProviderAsync(DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID) {
      if (ongoingDownloadTask == null) {
        oppiaLogger.d("PlatformParameterController", "Force downloading remote parameters")
        ongoingDownloadTask = CoroutineScope(backgroundCoroutineDispatcher).async {
          // TODO: Much of this can be moved to the prod impl and exposed here. In fact, only the
          //  service needs to be able to vary.
          val currentParams = platformParameterControllerProdImpl.loadRemotePlatformParameters()
          val currentFlags = platformParameterControllerProdImpl.loadRemoteFeatureFlags()
          val paramDefs = platformParameterControllerProdImpl.loadSupportedPlatformParameters()
          val flagDefs = platformParameterControllerProdImpl.loadSupportedFeatureFlags()
          val gaeParams = fetchRemotePlatformParametersListAsync().await()
          val gaeFlags = fetchRemoteFeatureFlagsListAsync().await()
          val incomingParams = convertAndFilterPlatformParameters(gaeParams, paramDefs)
          val incomingFlags = convertAndFilterFeatureFlags(gaeFlags, flagDefs)
          if (gaeParams.size != incomingParams.size) {
            val extraCount = gaeParams.size - incomingParams.size
            oppiaLogger.w("PlatformParameterController", "Received $extraCount extra parameter(s).")
          }
          if (gaeFlags.size != incomingFlags.size) {
            val extraCount = gaeFlags.size - incomingFlags.size
            oppiaLogger.w("PlatformParameterController", "Received $extraCount extra flag(s).")
          }
          val newRemoteParams = computeNewPlatformParametersList(currentParams, incomingParams)
          val newRemoteFlags = computeNewFeatureFlagsList(currentFlags, incomingFlags)
          platformParameterControllerProdImpl.databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
            oldDatabase.toBuilder()
              .clearRemotePlatformParameter()
              .clearRemoteFeatureFlag()
              .addAllRemotePlatformParameter(newRemoteParams)
              .addAllRemoteFeatureFlag(newRemoteFlags)
              .build()
          }
          oppiaLogger.d("PlatformParameterController", "Finished download attempt.")
        }
      }
      return@createInMemoryDataProviderAsync try {
        ongoingDownloadTask?.await()
        AsyncResult.Success(Unit)
      } catch (e: UnknownHostException) {
        // UnknownHostException happens particularly when the device loses connectivity or there's a
        // different DNS issue preventing oppia.org from being resolved.
        exceptionLogger.logException(e)
        oppiaLogger.e(
          "PlatformParameterController",
          "Failed to download parameters due to DNS or connectivity issue."
        )
        AsyncResult.Failure(e)
      } catch (e: HttpException) {
        // Something failed when trying to access one of the Oppia endpoints.
        exceptionLogger.logException(e)
        oppiaLogger.e(
          "PlatformParameterController",
          "Encountered HTTP issue when accessing URL: ${e.response()?.raw()?.request?.url?.toUrl()}"
        )
        AsyncResult.Failure(e)
      }
    }
  }

  private fun fetchRemotePlatformParametersListAsync(): Deferred<List<GaePlatformParameter>> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      withContext(Dispatchers.IO) {
        val androidMinVersionForUpdate = customPropertyRetriever.getInt(
          "android_min_version_code_for_recommending_app_update"
        )
        val androidMinVersion = customPropertyRetriever.getInt("android_min_supported_version_code")
        val minSdkVersion = customPropertyRetriever.getInt("android_min_supported_api_level")
        oppiaLogger.d(
          "PlatformParameterController",
          "System overrides:" +
            " android_min_version_code_for_recommending_app_update=$androidMinVersionForUpdate," +
            " android_min_supported_version_code=$androidMinVersion," +
            " android_min_supported_api_level=$minSdkVersion"
        )
        platformParameterDebugService.getPlatformParameters(
          overrideAndroidMinVersionCodeForRecommendingAppUpdate = androidMinVersionForUpdate,
          overrideAndroidMinSupportedVersionCode = androidMinVersion,
          overrideAndroidMinSupportApiLevel = minSdkVersion
        ).await()
      }
    }
  }

  private fun fetchRemoteFeatureFlagsListAsync(): Deferred<List<GaeFeatureFlag>> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      withContext(Dispatchers.IO) {
        val enableFastSwitch =
          customPropertyRetriever.getBoolean("android_enable_fast_language_switching_in_lesson")
        oppiaLogger.d(
          "PlatformParameterController",
          "System overrides: android_enable_fast_language_switching_in_lesson=$enableFastSwitch"
        )
        platformParameterDebugService.getFeatureFlags(
          overrideEnableEnableFastLanguageSwitchingInLesson = enableFastSwitch
        ).await()
      }
    }
  }

  private fun convertAndFilterPlatformParameters(
    gaeParameters: List<GaePlatformParameter>,
    supportedParameters: List<PlatformParameterDefinition>
  ): List<RemotePlatformParameter> {
    return gaeParameters.mapNotNull { convertAndFilterPlatformParameter(it, supportedParameters) }
  }

  // TODO: Optimize definitions by compiling a map by remote server name.
  private fun convertAndFilterPlatformParameter(
    gaeParameter: GaePlatformParameter,
    supportedParameters: List<PlatformParameterDefinition>
  ): RemotePlatformParameter? {
    val parameterId = supportedParameters.find { it.remoteServerName == gaeParameter.name }?.id ?: return null
    val paramValue = gaeParameter.value.convertToProto() ?: return null
    return RemotePlatformParameter.newBuilder().apply {
      this.id = parameterId
      this.remoteValue = paramValue
      this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
    }.build()
  }

  private fun GaePlatformParameterValue.convertToProto(): PlatformParameterValue? {
    return when (this) {
      is GaePlatformParameterValue.BooleanValue -> PlatformParameterValue.newBuilder().setBoolean(value).build()
      is GaePlatformParameterValue.IntValue -> PlatformParameterValue.newBuilder().setInteger(value).build()
      is GaePlatformParameterValue.StringValue -> PlatformParameterValue.newBuilder().setString(value).build()
      GaePlatformParameterValue.UnsupportedValue -> return null // Unsupported value.
    }
  }

  private fun convertAndFilterFeatureFlags(
    gaeFlags: List<GaeFeatureFlag>,
    supportedFlags: List<FeatureFlagDefinition>
  ): List<RemoteFeatureFlag> {
    return gaeFlags.mapNotNull { convertAndFilterFeatureFlag(it, supportedFlags) }
  }

  private fun convertAndFilterFeatureFlag(
    gaeFlag: GaeFeatureFlag,
    supportedFlags: List<FeatureFlagDefinition>
  ): RemoteFeatureFlag? {
    val flagId = supportedFlags.find { it.remoteServerName == gaeFlag.name }?.id ?: return null
    return RemoteFeatureFlag.newBuilder().apply {
      this.id = flagId
      this.remoteIsEnabled = gaeFlag.isEnabled
      this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
    }.build()
  }

  private fun computeNewPlatformParametersList(
    currentRemote: List<RemotePlatformParameter>,
    incomingRemote: List<RemotePlatformParameter>
  ): List<RemotePlatformParameter> {
    val currentById = currentRemote.associateBy { it.id }
    val incomingById = incomingRemote.associateBy { it.id }
    return (currentById + incomingById).values.toList()
  }

  private fun computeNewFeatureFlagsList(
    currentRemote: List<RemoteFeatureFlag>,
    incomingRemote: List<RemoteFeatureFlag>
  ): List<RemoteFeatureFlag> {
    val currentById = currentRemote.associateBy { it.id }
    val incomingById = incomingRemote.associateBy { it.id }
    return (currentById + incomingById).values.toList()
  }

  /**
   * Updates the local override database with the provided list of overridden feature flags.
   *
   * @param overriddenFlags the list of [OverriddenFeatureFlag]s to store as local overrides.
   * @return a [DataProvider] representing the result of the update operation.
   */
  fun updateOverriddenFeatureFlags(
    overriddenFlags: List<OverriddenFeatureFlag>
  ): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_OVERRIDDEN_FEATURE_FLAGS_PROVIDER_ID
    ) {
      databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
        val existingOverrides = oldDatabase.overriddenFeatureFlagList.associateBy { it.id }
        val latestValues = existingOverrides.toMutableMap().apply {
          overriddenFlags.safeForEach { override ->
            this[override.id] = override
          }
        }
        oldDatabase.toBuilder()
          .clearOverriddenFeatureFlag()
          .addAllOverriddenFeatureFlag(latestValues.values)
          .build()
      }.await()

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /**
   * Updates the local override database with the provided list of overridden platform parameters.
   *
   * @param overriddenParams the list of [OverriddenPlatformParameter]s to store as local overrides.
   * @return a [DataProvider] representing the result of the update operation.
   */
  fun updateOverriddenPlatformParameters(
    overriddenParams: List<OverriddenPlatformParameter>
  ): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_OVERRIDDEN_PLATFORM_PARAMETERS_PROVIDER_ID
    ) {
      databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
        val existingOverrides = oldDatabase.overriddenPlatformParameterList.associateBy { it.id }
        val latestValues = existingOverrides.toMutableMap().apply {

          overriddenParams.safeForEach { override ->
            this[override.id] = override
          }
        }
        oldDatabase.toBuilder()
          .clearOverriddenPlatformParameter()
          .addAllOverriddenPlatformParameter(latestValues.values)
          .build()
      }.await()

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /**
   * Resets the locally overridden feature flags corresponding to the specified [resetIds].
   *
   * This removes any locally overridden value for the specified feature flags from the local
   * override database.
   *
   * @param resetIds the IDs of the feature flags to reset
   * @return a [DataProvider] that completes when the overrides are removed.
   */
  fun resetFeatureFlags(resetIds: List<FeatureFlagId>): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(
      RESET_OVERRIDDEN_FEATURE_FLAG_PROVIDER_ID
    ) {
      databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
        val updatedOverrides = oldDatabase.overriddenFeatureFlagList
          .filterNot { it.id in resetIds }

        oldDatabase.toBuilder()
          .clearOverriddenFeatureFlag()
          .addAllOverriddenFeatureFlag(updatedOverrides)
          .build()
      }.await()

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /**
   * Resets the locally overridden platform parameters corresponding to the specified [resetIds].
   *
   * This removes any locally overridden value for the specified platform parameters from the local
   * override database.
   *
   * @param resetIds the IDs of the platform parameters to reset
   * @return a [DataProvider] that completes when the overrides are removed.
   */
  fun resetPlatformParameters(resetIds: List<PlatformParameterId>): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(
      RESET_OVERRIDDEN_PLATFORM_PARAMETER_PROVIDER_ID
    ) {
      databaseStore.storeDataAsync(updateInMemoryCache = true) { oldDatabase ->
        val updatedOverrides = oldDatabase.overriddenPlatformParameterList
          .filterNot { it.id in resetIds }

        oldDatabase.toBuilder()
          .clearOverriddenPlatformParameter()
          .addAllOverriddenPlatformParameter(updatedOverrides)
          .build()
      }.await()

      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  private companion object {
    private const val LOAD_EPHEMERAL_PLATFORM_PARAMETERS_PROVIDER_ID =
      "load_ephemeral_platform_parameters"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val LOAD_EPHEMERAL_FEATURE_FLAGS_PROVIDER_ID = "load_ephemeral_feature_flags"
    private const val UPDATE_OVERRIDDEN_FEATURE_FLAGS_PROVIDER_ID =
      "update_overridden_feature_flags"
    private const val UPDATE_OVERRIDDEN_PLATFORM_PARAMETERS_PROVIDER_ID =
      "update_overridden_platform_parameters"
    private const val RESET_OVERRIDDEN_PLATFORM_PARAMETER_PROVIDER_ID =
      "reset_overridden_platform_parameter"
    private const val RESET_OVERRIDDEN_FEATURE_FLAG_PROVIDER_ID =
      "reset_overridden_feature_flag"
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
    private const val DATABASE_NAME =
      "local_overridden_platform_parameter_and_feature_flag_database"
  }
}
