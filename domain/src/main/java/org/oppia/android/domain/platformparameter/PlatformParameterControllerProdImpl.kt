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

  /**
   * A [MutableStateFlow] that indicates whether all platform parameters and feature flags have
   * been successfully loaded and initialized.
   */
  val parametersAreLoadedFlow by lazy { MutableStateFlow(false) }

  init {
    // Ensure that parameters and flags are fully loaded ahead of a call to retrieveData() since
    // loadRemotePlatformParameters() needs to guarantee that the existing parameters are loaded
    // despite any later calls to downloadRemoteParameters(). Note that this will also guarantee
    // that the file is created on-disk (which is fine--it will just initially be empty).
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
      parametersAreLoadedFlow.convertToAutomaticDataProvider(
        GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID
      )
    }
  }

  override fun downloadRemoteParameters(): DataProvider<Unit> {
    check(parametersAreLoadedFlow.value) {
      "Can only remotely download parameters after they have been loaded."
    }
    return dataProviders.createInMemoryDataProviderAsync(DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID) {
      // TODO(#5835): Finish implementing forcing remote parameter downloads.

      // Erase the data provider's value so that callers cannot inadvertently depend on the actual
      // list of parameters available.
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
    }
  }

  /**
   * Synchronously loads and returns the known remotely downloaded platform parameters.
   *
   * The returned value should be fixed even if [downloadRemoteParameters] is called.
   *
   * This should only ever be called by package-specific implementations of
   * [PlatformParameterController].
   */
  suspend fun loadRemotePlatformParameters(): List<RemotePlatformParameter> {
    return databaseStore.readDataAsync().await().remotePlatformParameterList
  }

  /**
   * Synchronously loads and returns the known remotely downloaded feature flags.
   *
   * The returned value should be fixed even if [downloadRemoteParameters] is called.
   *
   * This should only ever be called by package-specific implementations of
   * [PlatformParameterController].
   */
  suspend fun loadRemoteFeatureFlags(): List<RemoteFeatureFlag> {
    return databaseStore.readDataAsync().await().remoteFeatureFlagList
  }

  /**
   * Returns the list of [PlatformParameterDefinition]s configured for this build of the app.
   *
   * This should only ever be called by package-specific implementations of
   * [PlatformParameterController].
   */
  fun loadSupportedPlatformParameters(): List<PlatformParameterDefinition> {
    return configRetriever.loadSupportedPlatformParameters().platformParameterDefinitionList
  }

  /**
   * Returns the list of [FeatureFlagDefinition]s configured for this build of the app.
   *
   * This should only ever be called by package-specific implementations of
   * [PlatformParameterController].
   */
  fun loadSupportedFeatureFlags(): List<FeatureFlagDefinition> {
    return configRetriever.loadSupportedFeatureFlags().featureFlagDefinitionList
  }

  private fun loadParametersInternalAsync(): Deferred<Unit> {
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
      parametersAreLoadedFlow.value = true

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

  /** An application-scoped factory for building new [PlatformParameterControllerProdImpl]s. */
  // TODO(#5835): Remove this factory once the hack for initializing parameters in tests is gone.
  class Factory @Inject constructor(
    private val cacheStoreFactory: PersistentCacheStore.Factory,
    private val dataProviders: DataProviders,
    private val configRetriever: PlatformParameterConfigRetriever,
    private val oppiaLogger: OppiaLogger,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
  ) {
    /**
     * Returns a new [PlatformParameterControllerProdImpl] for the specified [processState].
     *
     * This method should only ever be called once since there should only ever be one instance of
     * [PlatformParameterController] for the lifetime of an Oppia Android application process.
     */
    fun create(processState: PlatformParameterProcessState): PlatformParameterControllerProdImpl {
      return PlatformParameterControllerProdImpl(
        cacheStoreFactory, dataProviders, configRetriever, processState, oppiaLogger,
        backgroundCoroutineDispatcher
      )
    }
  }

  /**
   * An abstract representation of either a platform parameter or feature flag, incorporating both
   * the default and remote versions.
   */
  private sealed class ParameterState {
    /**
     * A representation of a single platform parameter incorporating both its local definition state
     * and its remotely downloaded state, if any.
     *
     * @property definition the definition of the parameter
     * @property remote the remotely downloaded version of the parameter, or null if none
     */
    data class PlatformParameter(
      val definition: PlatformParameterDefinition,
      val remote: RemotePlatformParameter?
    ) : ParameterState() {
      /**
       * Returns the current value of the parameter, first prioritizing its remotely downloaded
       * value (if any) and falling back to its default value.
       */
      fun computeCurrentState(): PlatformParameterValue =
        remote?.remoteValue ?: definition.defaultValue
    }

    /**
     * A representation of a single feature flag incorporating both its local definition state and
     * its remotely downloaded state, if any.
     *
     * @property definition the definition of the flag
     * @property remote the remotely downloaded version of the flag, or null if none
     */
    data class FeatureFlag(
      val definition: FeatureFlagDefinition,
      val remote: RemoteFeatureFlag?
    ) : ParameterState() {
      /**
       * Returns the current enabled state of the flag, first prioritizing its remotely downloaded
       * value (if any) and falling back to its default value.
       */
      fun computeCurrentState(): Boolean = remote?.remoteIsEnabled ?: definition.defaultIsEnabled

      /**
       * Returns the [SyncStatus] of this flag depending on whether it has been successfully synced
       * remotely from the Oppia web server.
       */
      fun computeCurrentStatus(): SyncStatus =
        remote?.syncStatus ?: SyncStatus.NOT_SYNCED_FROM_SERVER
    }
  }

  private companion object {
    private const val GET_PARAMETER_INITIALIZATION_STATUS_PROVIDER_ID =
      "get_parameter_initialization_status"
    private const val DOWNLOAD_REMOTE_PARAMETERS_PROVIDER_ID = "download_remote_parameters"
    private const val DATABASE_NAME = "platform_parameter_and_feature_flag_database"
  }
}
