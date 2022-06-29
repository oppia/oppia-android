package org.oppia.android.domain.spotlight

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.SpotlightStateDatabase
import org.oppia.android.app.model.SpotlightViewState

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
  class SpotlightFeatureUnrecognizedException(message: String) : Exception(message)

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<SpotlightStateDatabase>>()

  private fun recordSpotlightStateAsync(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
    viewState: SpotlightViewState
  ): Deferred<Any> {
    return retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val spotlightStateDatabaseBuilder = it.toBuilder()

      val spotlight = when (feature) {
        Spotlight.FeatureCase.ONBOARDING_NEXT_BUTTON -> {
          spotlightStateDatabaseBuilder.setOnboardingNextButton(viewState)
        }
        Spotlight.FeatureCase.TOPIC_LESSON_TAB -> {
          spotlightStateDatabaseBuilder.setTopicLessonTab(viewState)
        }
        else -> {
          throw SpotlightFeatureUnrecognizedException("spotlight feature is not one of the recognized types")
        }
      }
      val spotlightStateDatabase = spotlightStateDatabaseBuilder.build()
      Pair(spotlightStateDatabase, spotlight)
    }
  }

  fun recordSpotlightCheckpoint(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
    viewState: SpotlightViewState
  ): DataProvider<Any?> {
    val deferred = recordSpotlightStateAsync(
      profileId,
      feature,
      viewState
    )
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync AsyncResult.Success(deferred.await())
    }
  }

  fun retrieveSpotlightViewState(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
  ): DataProvider<Any> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
      ) {

        val viewState = when (feature) {
          Spotlight.FeatureCase.ONBOARDING_NEXT_BUTTON -> {
            it.onboardingNextButton
          }
          Spotlight.FeatureCase.TOPIC_LESSON_TAB -> {
            it.topicLessonTab
          }
          else -> {
            throw SpotlightFeatureUnrecognizedException("spotlight activity is not one of the recognized types")
          }
        }

        if (viewState != null) {
          AsyncResult.Success(viewState)
        } else {
          AsyncResult.Failure(SpotlightStateNotFoundException("State not found "))
        }
      }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<SpotlightStateDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          SpotlightStateDatabase.getDefaultInstance(),
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
