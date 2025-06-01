package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import org.oppia.android.app.model.FeatureFlagDefinition
import org.oppia.android.app.model.PlatformParameterDefinition
import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.model.RemoteFeatureFlag
import org.oppia.android.app.model.RemotePlatformParameter
import org.oppia.android.app.model.RemotePlatformParameterAndFeatureFlagDatabase
import org.oppia.android.app.model.SyncStatus
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * Production implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
class PlatformParameterControllerProdImpl(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val configRetriever: PlatformParameterConfigRetriever,
  private val processState: PlatformParameterProcessState,
  private val oppiaLogger: OppiaLogger,
  private val backgroundCoroutineDispatcher: CoroutineDispatcher,
) : PlatformParameterController {
  private val databaseStore by lazy {
    cacheStoreFactory.create(
      DATABASE_NAME, RemotePlatformParameterAndFeatureFlagDatabase.getDefaultInstance()
    )
  }
  // Note that the 'by lazy' here guarantees thread-safe and singleton initialization.
  private val initializationDeferred by lazy { loadParametersInternalAsync() }
  private val areParametersLoadedFlow by lazy { MutableStateFlow(false) }

  init {
    // Ensure that parameters and flags are fully loaded ahead of a call to retrieveData() since
    // loadParameters() guarantees exactly one non-pending result. Note that this will also
    // guarantee that the file is created on-disk (which is fine--it will just initially be empty).
    databaseStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { failure ->
      if (failure != null) {
        oppiaLogger.e(
          "PlatformParameterController", "Failed to load platform params/feature flags.", failure
        )
      }
    }
  }

  override fun loadParametersAsync() = initializationDeferred

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return dataProviders.run {
      areParametersLoadedFlow.convertToAutomaticDataProvider(
        GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID
      )
    }
  }

  override fun downloadRemoteParameters(): DataProvider<Any?> {
    check(areParametersLoadedFlow.value) {
      "Can only remotely download parameters after they have been loaded."
    }
    return dataProviders.createInMemoryDataProviderAsync(DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID) {
      // TODO(#5725): Finish implementing forcing remote parameter downloads.

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  suspend fun loadRemotePlatformParameters(): List<RemotePlatformParameter> {
    return databaseStore.readDataAsync().await().remotePlatformParameterList
  }

  suspend fun loadRemoteFeatureFlags(): List<RemoteFeatureFlag> {
    return databaseStore.readDataAsync().await().remoteFeatureFlagList
  }

  fun loadSupportedPlatformParameters(): List<PlatformParameterDefinition> {
    return configRetriever.loadSupportedPlatformParameters().platformParameterDefinitionList
  }

  fun loadSupportedFeatureFlags(): List<FeatureFlagDefinition> {
    return configRetriever.loadSupportedFeatureFlags().featureFlagDefinitionList
  }

  private fun loadParametersInternalAsync(): Deferred<Any?> {
    return CoroutineScope(backgroundCoroutineDispatcher).async {
      val params = loadAllParameterStates()

      // Synchronize injectable parameter and flag state.
      val platformParams = params.filterIsInstance<ParameterState.PlatformParameter>()
      val featureFlags = params.filterIsInstance<ParameterState.FeatureFlag>()
      val platStatesById = platformParams.associate { it.definition.id to it.computeCurrentState() }
      val flagStatesById = featureFlags.associate { it.definition.id to it.computeCurrentState() }
      val statusesById = featureFlags.associate { it.definition.id to it.computeCurrentStatus() }
      processState.initializePlatformParameters(platStatesById)
      processState.initializeFeatureFlags(flagStatesById)
      processState.initializeFeatureFlagSyncStatuses(statusesById)

      // Let observers know that parameters have been initialized.
      areParametersLoadedFlow.value = true

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
    }
  }

  private suspend fun loadAllParameterStates(): List<ParameterState> {
    val remoteParamById = loadRemotePlatformParameters().associateBy { it.id }
    val remoteFlagById = loadRemoteFeatureFlags().associateBy { it.id }
    return loadSupportedPlatformParameters().map { paramDefinition ->
      ParameterState.PlatformParameter(paramDefinition, remoteParamById[paramDefinition.id])
    } + loadSupportedFeatureFlags().map { flagDefinition ->
      ParameterState.FeatureFlag(flagDefinition, remoteFlagById[flagDefinition.id])
    }
  }

  // TODO(#5835): Remove this factory once the hack for initializing parameters in tests is gone.
  class Factory @Inject constructor(
    private val cacheStoreFactory: PersistentCacheStore.Factory,
    private val dataProviders: DataProviders,
    private val configRetriever: PlatformParameterConfigRetriever,
    private val oppiaLogger: OppiaLogger,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  ) {
    fun create(processState: PlatformParameterProcessState): PlatformParameterControllerProdImpl {
      return PlatformParameterControllerProdImpl(
        cacheStoreFactory, dataProviders, configRetriever, processState, oppiaLogger,
        backgroundCoroutineDispatcher
      )
    }
  }

  private sealed class ParameterState {
    abstract val remoteServerName: String

    abstract fun updateFromServer(value: PlatformParameterValue)

    data class PlatformParameter(
      val definition: PlatformParameterDefinition,
      val remote: RemotePlatformParameter?
    ) : ParameterState() {
      private var latestSyncFromServer: PlatformParameterValue? = null

      override val remoteServerName = definition.remoteServerName

      override fun updateFromServer(value: PlatformParameterValue) {
        latestSyncFromServer = value
      }

      // Always compute based on the remote value, if any, and fall back to the definition.
      fun computeCurrentState(): PlatformParameterValue =
        remote?.remoteValue ?: definition.defaultValue

      fun serialize(): RemotePlatformParameter {
        // Ensure there's always a record of the parameter.
        val remoteValue = latestSyncFromServer
        val remote = this.remote ?: computeRemote()
        return if (remoteValue != null) {
          remote.toBuilder().apply {
            this.remoteValue = remoteValue
            this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        } else remote // Nothing changes.
      }

      private fun computeRemote() = RemotePlatformParameter.newBuilder().apply {
        this.id = definition.id
        this.syncStatus = SyncStatus.NOT_SYNCED_FROM_SERVER
      }.build()
    }

    data class FeatureFlag(
      val definition: FeatureFlagDefinition,
      val remote: RemoteFeatureFlag?
    ) : ParameterState() {
      private var latestSyncFromServer: Boolean? = null

      override val remoteServerName = definition.remoteServerName

      override fun updateFromServer(value: PlatformParameterValue) {
        check(value.valueTypeCase == PlatformParameterValue.ValueTypeCase.BOOLEAN) {
          "Expected feature flag corresponding to ${definition.id} (remote: $remoteServerName) to" +
            " be a boolean, but received from server: $value"
        }
        latestSyncFromServer = value.boolean
      }

      // Always compute based on the remote value, if any, and fall back to the definition.
      fun computeCurrentState(): Boolean = remote?.remoteIsEnabled ?: definition.defaultIsEnabled

      fun computeCurrentStatus(): SyncStatus =
        remote?.syncStatus ?: SyncStatus.NOT_SYNCED_FROM_SERVER

      fun serialize(): RemoteFeatureFlag {
        // Ensure there's always a record of the parameter.
        val remoteValue = latestSyncFromServer
        val remote = this.remote ?: computeRemote()
        return if (remoteValue != null) {
          remote.toBuilder().apply {
            this.remoteIsEnabled = remoteValue
            this.syncStatus = SyncStatus.SYNCED_FROM_SERVER
          }.build()
        } else remote // Nothing changes.
      }

      private fun computeRemote() = RemoteFeatureFlag.newBuilder().apply {
        this.id = definition.id
        this.syncStatus = SyncStatus.NOT_SYNCED_FROM_SERVER
      }.build()
    }
  }

  private companion object {
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val DATABASE_NAME = "platform_parameter_and_feature_flag_database"
  }
}
