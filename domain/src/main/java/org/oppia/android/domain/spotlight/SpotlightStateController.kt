package org.oppia.android.domain.spotlight

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.OnboardingSpotlightCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileSpotlightCheckpoint
import org.oppia.android.app.model.SpotlightCheckpointDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync

private const val SPOTLIGHT_STATE_DATA_PROVIDER_ID = "spotlight_state_data_provider_id"
private const val CACHE_NAME = "spotlight_checkpoint_database"
private const val RECORD_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID =
  "record_spotlight_checkpoint_provider_id"
private const val RETRIEVE_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID =
  "retrieve_spotlight_checkpoint_provider_id"

@Singleton
class SpotlightStateController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val dataProviders: DataProviders,
) {

  class SpotlightStateNotFoundException(message: String) : Exception(message)

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<SpotlightCheckpointDatabase>>()

  private fun recordSpotlightStateAsync(
    profileId: ProfileId,
    checkpoint: Any,
  ): Deferred<Any> {
    return retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val spotlightCheckpointDatabaseBuilder = it.toBuilder()

      val newCheckpoint : Any = when (checkpoint) {
        is OnboardingSpotlightCheckpoint -> {
          spotlightCheckpointDatabaseBuilder.setOnboardingSpotlightCheckpoint(checkpoint)
        }
        is ProfileSpotlightCheckpoint -> {
          spotlightCheckpointDatabaseBuilder.setProfileSpotlightCheckpoint(checkpoint)
        }
        else -> {
          // throw exception
        }
      }

      val spotlightCheckpointDatabase = spotlightCheckpointDatabaseBuilder.build()

      Pair(spotlightCheckpointDatabase, newCheckpoint)
    }
  }

  fun recordSpotlightCheckpoint(
    profileId: ProfileId,
    checkpoint: Any,
  ): DataProvider<Any?> {
    val deferred = recordSpotlightStateAsync(
      profileId,
      checkpoint
    )
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync AsyncResult.Success(deferred.await())
    }
  }

  fun retrieveSpotlightCheckpoint(
    profileId: ProfileId,
    spotlightActivity: SpotlightActivity,
  ): DataProvider<Any> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
      ) {


        val checkpoint = when (spotlightActivity) {
           SpotlightActivity.ONBOARDING_ACTIVITY -> {
            it.onboardingSpotlightCheckpoint
          }
           SpotlightActivity.PROFILE_ACTIVITY -> {
            it.profileSpotlightCheckpoint
          }
        }

        if (checkpoint != null){
          AsyncResult.Success(checkpoint)
        }else {
          AsyncResult.Failure(SpotlightStateNotFoundException("State not found "))
        }

      }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<SpotlightCheckpointDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          SpotlightCheckpointDatabase.getDefaultInstance(),
          profileId
        )
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeCacheAsync().invokeOnCompletion { throwable ->
      throwable?.let {
        oppiaLogger.e(
          "SpotlightCheckpointController",
          "Failed to prime cache ahead of data retrieval for SpotlightCheckpointController.",
          it
        )
      }
    }
    return cacheStore
  }
}

enum class SpotlightActivity {
  ONBOARDING_ACTIVITY,
  PROFILE_ACTIVITY
}