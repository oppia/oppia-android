package org.oppia.android.domain.spotlight

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.Spotlight.FeatureCase.FEATURE_NOT_SET
import org.oppia.android.app.model.Spotlight.FeatureCase.FIRST_CHAPTER
import org.oppia.android.app.model.Spotlight.FeatureCase.LESSONS_BACK_BUTTON
import org.oppia.android.app.model.Spotlight.FeatureCase.ONBOARDING_NEXT_BUTTON
import org.oppia.android.app.model.Spotlight.FeatureCase.PROMOTED_STORIES
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_LESSON_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.TOPIC_REVISION_TAB
import org.oppia.android.app.model.Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON
import org.oppia.android.app.model.Spotlight.FeatureCase.VOICEOVER_PLAY_ICON
import org.oppia.android.app.model.SpotlightStateDatabase
import org.oppia.android.app.model.SpotlightViewState
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync

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
  class SpotlightFeatureNotFoundException(message: String) : IllegalArgumentException(message)

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<SpotlightStateDatabase>>()

  /**
   * Marks the [SpotlightViewState] of a spotlit feature for a given profile as seen.
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
   * @param profileId the ID of the profile that will be viewing the spotlight
   * @param feature the spotlight feature to be spotlit
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
          ONBOARDING_NEXT_BUTTON -> spotlightStateRetrieverHelper(it.onboardingNextButton)
          TOPIC_LESSON_TAB -> spotlightStateRetrieverHelper(it.topicLessonTab)
          TOPIC_REVISION_TAB -> spotlightStateRetrieverHelper(it.topicRevisionTab)
          FIRST_CHAPTER -> spotlightStateRetrieverHelper(it.firstChapter)
          PROMOTED_STORIES -> spotlightStateRetrieverHelper(it.promotedStories)
          LESSONS_BACK_BUTTON -> spotlightStateRetrieverHelper(it.lessonsBackButton)
          VOICEOVER_PLAY_ICON -> spotlightStateRetrieverHelper(it.voiceoverPlayIcon)
          VOICEOVER_LANGUAGE_ICON -> spotlightStateRetrieverHelper(it.voiceoverLanguageIcon)
          FEATURE_NOT_SET -> {
            return@transformAsync AsyncResult.Failure(
              SpotlightFeatureNotFoundException("Spotlight feature was not found")
            )
          }
        }
        AsyncResult.Success(viewState)
      }
  }

  private fun spotlightStateRetrieverHelper(spotlightViewState: SpotlightViewState): SpotlightViewState {
    return if (spotlightViewState == SpotlightViewState.SPOTLIGHT_VIEW_STATE_UNSPECIFIED) {
      SpotlightViewState.SPOTLIGHT_NOT_SEEN
    } else spotlightViewState
  }

  private fun recordSpotlightStateAsync(
    profileId: ProfileId,
    feature: Spotlight.FeatureCase,
    viewState: SpotlightViewState
  ): Deferred<Any> {
    return retrieveCacheStore(profileId).storeDataAsync(
      updateInMemoryCache = true
    ) { spotlightStateDatabase ->
      spotlightStateDatabase.toBuilder().run {
        when (feature) {
          ONBOARDING_NEXT_BUTTON -> this.setOnboardingNextButton(viewState)
          TOPIC_LESSON_TAB -> this.setTopicLessonTab(viewState)
          TOPIC_REVISION_TAB -> this.setTopicRevisionTab(viewState)
          FIRST_CHAPTER -> this.setFirstChapter(viewState)
          PROMOTED_STORIES -> this.setPromotedStories(viewState)
          LESSONS_BACK_BUTTON -> this.setLessonsBackButton(viewState)
          VOICEOVER_PLAY_ICON -> this.setVoiceoverPlayIcon(viewState)
          VOICEOVER_LANGUAGE_ICON -> this.setVoiceoverLanguageIcon(viewState)
          FEATURE_NOT_SET -> {
            throw SpotlightFeatureNotFoundException("Spotlight feature was not found")
          }
        }
      }.build()
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<SpotlightStateDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore = cacheStoreMap.getOrPut(profileId) {
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          SpotlightStateDatabase.getDefaultInstance(),
          profileId
        )
      }
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeInMemoryCacheAsync().invokeOnCompletion { throwable ->
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
