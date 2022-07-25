package org.oppia.android.domain.spotlight

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.SpotlightStateDatabase
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_NAME = "spotlight_checkpoint_database"
private const val RECORD_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID =
  "record_spotlight_checkpoint_provider_id"
private const val RETRIEVE_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID =
  "retrieve_spotlight_checkpoint_provider_id"

/** Handles saving and retrieving feature spotlight states. */
@Singleton
class SpotlightStateController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
  private val dataProviders: DataProviders,
) {
  // thrown when spotlight feature is not set while retrieving or marking spotlight view states
  class SpotlightFeatureNotFoundException(message: String) : Exception(message)

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<SpotlightStateDatabase>>()

  /**
   * Marks the [SpotlightViewState] of a spotlit feature for a given profile as seen
   *
   * @param profileId the ID of the profile viewing the spotlight
   * @param feature the spotlight feature who's view state is to be recorded
   * @throws SpotlightFeatureNotFoundException when feature is not set correctly
   */
  fun markSpotlightViewed(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
  ): DataProvider<Any?> {
    val deferred = recordSpotlightStateAsync(
      profileId,
      feature,
      SpotlightViewState.SPOTLIGHT_SEEN
    )
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_SPOTLIGHT_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync AsyncResult.Success(deferred.await())
    }
  }

  /**
   * Retrieves the current [SpotlightViewState] of a spotlit feature for a given profile.
   *
   * @param profileId the ID of the profile that will be viewing the spotlight
   * @param the feature to be spotlit
   *
   * @return DataProvider containing the current [SpotlightViewState] corresponding to the specified [feature]
   */
  fun retrieveSpotlightViewState(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
  ): DataProvider<SpotlightViewState> {
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
          Spotlight.FeatureCase.TOPIC_REVISION_TAB -> {
            it.topicRevisionTab
          }
          Spotlight.FeatureCase.FIRST_CHAPTER -> {
            it.firstChapter
          }
          Spotlight.FeatureCase.PROMOTED_STORIES -> {
            it.promotedStories
          }
          Spotlight.FeatureCase.LESSONS_BACK_BUTTON -> {
            it.lessonsBackButton
          }
          Spotlight.FeatureCase.VOICEOVER_PLAY_ICON -> {
            it.voiceoverPlayIcon
          }
          Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON -> {
            it.voiceoverLanguageIcon
          }
          Spotlight.FeatureCase.FEATURE_NOT_SET -> {
            return@transformAsync AsyncResult.Failure(
              SpotlightFeatureNotFoundException("Spotlight feature was not found")
            )
          }
        }
        AsyncResult.Success(viewState)
      }
  }

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
        Spotlight.FeatureCase.TOPIC_REVISION_TAB -> {
          spotlightStateDatabaseBuilder.setTopicRevisionTab(viewState)
        }
        Spotlight.FeatureCase.FIRST_CHAPTER -> {
          spotlightStateDatabaseBuilder.setFirstChapter(viewState)
        }
        Spotlight.FeatureCase.PROMOTED_STORIES -> {
          spotlightStateDatabaseBuilder.setPromotedStories(viewState)
        }
        Spotlight.FeatureCase.LESSONS_BACK_BUTTON -> {
          spotlightStateDatabaseBuilder.setLessonsBackButton(viewState)
        }
        Spotlight.FeatureCase.VOICEOVER_PLAY_ICON -> {
          spotlightStateDatabaseBuilder.setVoiceoverPlayIcon(viewState)
        }
        Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON -> {
          spotlightStateDatabaseBuilder.setVoiceoverLanguageIcon(viewState)
        }
        Spotlight.FeatureCase.FEATURE_NOT_SET -> {
          throw SpotlightFeatureNotFoundException("Spotlight feature was not found")
        }
      }
      val spotlightStateDatabase = spotlightStateDatabaseBuilder.build()
      Pair(spotlightStateDatabase, spotlight)
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
