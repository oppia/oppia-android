package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummaryDatabase
import org.oppia.app.model.ChapterSummaryDomain
import org.oppia.app.model.ChapterSummaryView
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.RevisionCardDatabase
import org.oppia.app.model.RevisionCardDomain
import org.oppia.app.model.SkillSummaryDatabase
import org.oppia.app.model.SkillSummaryDomain
import org.oppia.app.model.SkillSummaryView
import org.oppia.app.model.StorySummaryDatabase
import org.oppia.app.model.StorySummaryDomain
import org.oppia.app.model.StorySummaryView
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.SubtopicDatabase
import org.oppia.app.model.SubtopicDomain
import org.oppia.app.model.SubtopicView
import org.oppia.app.model.TopicDatabase
import org.oppia.app.model.TopicDomain
import org.oppia.app.model.TopicSummaryListView
import org.oppia.app.model.TopicSummaryView
import org.oppia.app.model.TopicView
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

val TOPIC_JSON_FILE_ASSOCIATIONS = mapOf(
  FRACTIONS_TOPIC_ID to listOf(
    "fractions_exploration0.json",
    "fractions_exploration1.json",
    "fractions_questions.json",
    "fractions_skills.json",
    "fractions_stories.json",
    "fractions_subtopics.json",
    "fractions_topic.json"
  ),
  RATIOS_TOPIC_ID to listOf(
    "ratios_exploration0.json",
    "ratios_exploration1.json",
    "ratios_exploration2.json",
    "ratios_exploration3.json",
    "ratios_questions.json",
    "ratios_skills.json",
    "ratios_stories.json",
    "ratios_topic.json"
  )
)

private const val SUBTOPIC_TITLE = "What is Fraction?"

private const val ADD_TOPIC_TRANSFORMED_PROVIDER_ID = "add_topic_transformed_id"
private const val ADD_SUBTOPIC_LIST_TRANSFORMED_PROVIDER_ID = "add_subtopic_list_transformed_id"
private const val ADD_STORY_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID = "add_story_list_transformed_id"
private const val ADD_SKILL_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID = "add_skill_list_transformed_id"
private const val ADD_CHAPTER_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID =
  "add_chapter_list_transformed_id"
private const val GET_ALL_TOPICS_TRANSFORMED_PROVIDER_ID = "get_all_topics_transformed_id"
private const val GET_TOPIC_LIST_TRANSFORMED_PROVIDER_ID = "get_topic_list_transformed_id"
private const val GET_TOPIC_TRANSFORMED_PROVIDER_ID = "get_topic_transformed_id"
private const val COMBINE_TOPIC_AND_CHAPTER_SUMMARY = "combine_topic_and_chapter_summary"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class TopicRepository @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val logger: Logger
) {

  /** Indicates that the given chapter already exists. */
  class ChapterSummaryAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given chapter is not found. */
  class ChapterSummaryNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given revision card content already exists. */
  class RevisionCardAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given revision card content is not found. */
  class RevisionCardNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given skill summary already exists. */
  class SkillSummaryAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given skill summary is not found. */
  class SkillSummaryNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given story summary already exists. */
  class StorySummaryAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given story summary is not found. */
  class StorySummaryNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given subtopic already exists. */
  class SubtopicAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given subtopic is not found. */
  class SubtopicNotFoundException(msg: String) : Exception(msg)

  /** Indicates that the given topic already exists. */
  class TopicAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given topic is not found. */
  class TopicNotFoundException(msg: String) : Exception(msg)

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * CHAPTER_SUMMARY_ALREADY_FOUND, the [ChapterSummaryAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class ChapterSummaryActionStatus {
    SUCCESS,
    CHAPTER_SUMMARY_ALREADY_EXISTS,
    CHAPTER_SUMMARY_NOT_FOUND
  }

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * REVISION_CARD_ALREADY_FOUND, the [RevisionCardAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class RevisionCardActionStatus {
    SUCCESS,
    REVISION_CARD_ALREADY_EXISTS,
    REVISION_CARD_NOT_FOUND
  }

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * SKILL_SUMMARY_ALREADY_FOUND, the [SkillSummaryAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class SkillSummaryActionStatus {
    SUCCESS,
    SKILL_SUMMARY_ALREADY_EXISTS,
    SKILL_SUMMARY_NOT_FOUND
  }

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * STORY_SUMMARY_ALREADY_FOUND, the [StorySummaryAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class StorySummaryActionStatus {
    SUCCESS,
    STORY_SUMMARY_ALREADY_EXISTS,
    STORY_SUMMARY_NOT_FOUND
  }

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * SUBTOPIC_ALREADY_FOUND, the [SubtopicAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class SubtopicActionStatus {
    SUCCESS,
    SUBTOPIC_ALREADY_EXISTS,
    SUBTOPIC_NOT_FOUND
  }

  /**
   * These statuses correspond to the exceptions above such that if the deferred contains
   * TOPIC_ALREADY_FOUND, the [TopicAlreadyExistsException] will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class TopicActionStatus {
    SUCCESS,
    TOPIC_ALREADY_EXISTS,
    TOPIC_NOT_FOUND
  }

  private val chapterSummaryDataStore =
    cacheStoreFactory.create(
      "chapter_summary_database",
      ChapterSummaryDatabase.getDefaultInstance()
    )

  private val revisionCardDataStore =
    cacheStoreFactory.create(
      "revision_card_database",
      RevisionCardDatabase.getDefaultInstance()
    )

  private val skillSummaryDataStore =
    cacheStoreFactory.create(
      "skill_summary_database",
      SkillSummaryDatabase.getDefaultInstance()
    )

  private val storySummaryDataStore =
    cacheStoreFactory.create(
      "story_summary_database",
      StorySummaryDatabase.getDefaultInstance()
    )

  private val subtopicDataStore =
    cacheStoreFactory.create("subtopic_database", SubtopicDatabase.getDefaultInstance())

  private val topicDataStore =
    cacheStoreFactory.create("topic_database", TopicDatabase.getDefaultInstance())

  // TODO(#272): Remove init block when storeDataAsync is fixed
  init {
    chapterSummaryDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache chapterSummaryDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
    revisionCardDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache revisionCardDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
    skillSummaryDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache skillSummaryDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
    storySummaryDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache storySummaryDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
    subtopicDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache subtopicDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
    topicDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache topicDataStore ahead of LiveData conversion for TopicRepository.",
          it
        )
      }
    }
  }

  fun initialiseAllTopics() {
    TOPIC_JSON_FILE_ASSOCIATIONS.keys.forEach { topicId ->
      addTopic(retrieveTopicDomainFromJson(topicId))
      addSubTopicList(retrieveSubtopicDomainListFromJson(topicId))
      addSkillSummaryList(retrieveSkillSummaryDomainListFromJson(topicId))
      addStorySummaryList(retrieveStorySummaryDomainListFromJson(topicId))
      addChapterSummaryList(retrieveChapterSummaryDomainListFromJson(topicId))
      addRevisionCardList(retrieveRevisionCardDomainListFromJson(FRACTIONS_SUBTOPIC_ID_1))
    }
  }

  fun getSkillSummaryDataProvider(skillId: String): DataProvider<SkillSummaryView> {
    return dataProviders.transformAsync<SkillSummaryDatabase, SkillSummaryView>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      skillSummaryDataStore
    ) {
      val skillSummaryDomain = it.skillSummaryDatabaseMap[skillId]
      val skillSummaryView = convertSkillSummaryDomainToSkillSummaryView(skillSummaryDomain)
      AsyncResult.success(skillSummaryView)
    }
  }

  fun getSubtopicDataProvider(subtopicId: String): DataProvider<SubtopicView> {
    return dataProviders.transformAsync<SubtopicDatabase, SubtopicView>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      subtopicDataStore
    ) {
      val subtopicDomain = it.subtopicDatabaseMap[subtopicId]
      val subtopicView = convertSubtopicDomainToSubtopicView(subtopicDomain)
      AsyncResult.success(subtopicView)
    }
  }

  fun getSubtopicListDataProvider(topicId: String): DataProvider<List<SubtopicView>> {
    return dataProviders.transformAsync<SubtopicDatabase, List<SubtopicView>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      subtopicDataStore
    ) {
      val subtopicViewList = ArrayList<SubtopicView>()
      val subtopicDomainList =
        it.subtopicDatabaseMap.values.filter { subtopicDomain -> subtopicDomain.topicId == topicId }
      subtopicDomainList.forEach { subtopicDomain ->
        subtopicViewList.add(convertSubtopicDomainToSubtopicView(subtopicDomain))
      }
      AsyncResult.success(subtopicViewList)
    }
  }

  fun getStorySummaryDataProvider(storyId: String): DataProvider<StorySummaryView> {
    val storySummaryDataProvider = retrieveStorySummaryDataProvider(storyId)
    val chapterSummaryListDataProvider = retrieveChapterSummaryListForAStoryDataProvider(storyId)
    return dataProviders.combine(
      COMBINE_TOPIC_AND_CHAPTER_SUMMARY,
      storySummaryDataProvider,
      chapterSummaryListDataProvider,
      ::combineStorySummaryAndChapterSummaryList
    )
  }

  fun getStorySummaryListDataProvider(topicId: String): DataProvider<List<StorySummaryView>> {
    val storySummaryListDataProvider = retrieveStorySummaryListForATopicDataProvider(topicId)
    val chapterSummaryListDataProvider = retrieveChapterSummaryListForATopicDataProvider(topicId)
    return dataProviders.combine(
      COMBINE_TOPIC_AND_CHAPTER_SUMMARY,
      storySummaryListDataProvider,
      chapterSummaryListDataProvider,
      ::combineStorySummaryListAndChapterSummaryList
    )
  }

  fun getTopicDataProvider(topicId: String): DataProvider<TopicView> {
    return dataProviders.transformAsync<TopicDatabase, TopicView>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      topicDataStore
    ) {
      val topicDomain = it.topicDatabaseMap[topicId]
      val topicView = convertTopicDomainToTopicView(topicDomain)
      AsyncResult.success(topicView)
    }
  }

  /** Returns a [TopicSummaryListView] [DataProvider]. */
  fun getTopicSummaryListDataProvider(): DataProvider<TopicSummaryListView> {
    val topicListDataProvider = retrieveTopicListDataProvider()
    val chapterSummaryListDataProvider = retrieveChapterSummaryListDataProvider()
    return dataProviders.combine(
      COMBINE_TOPIC_AND_CHAPTER_SUMMARY,
      topicListDataProvider,
      chapterSummaryListDataProvider,
      ::combineTopicListAndChapterSummaryList
    )
  }

  private fun retrieveStorySummaryListForATopicDataProvider(topicId: String): DataProvider<List<StorySummaryDomain>> {
    return dataProviders.transformAsync<StorySummaryDatabase, List<StorySummaryDomain>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      storySummaryDataStore
    ) {
      val storySummaryDomainList =
        it.storySummaryDatabaseMap.values.filter { storySummaryDomain -> storySummaryDomain.topicId == topicId }
      AsyncResult.success(storySummaryDomainList)
    }
  }

  private fun retrieveStorySummaryDataProvider(storyId: String): DataProvider<StorySummaryDomain> {
    return dataProviders.transformAsync<StorySummaryDatabase, StorySummaryDomain>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      storySummaryDataStore
    ) {
      val storySummary = it.storySummaryDatabaseMap[storyId]
      AsyncResult.success(storySummary ?: StorySummaryDomain.getDefaultInstance())
    }
  }

  private fun retrieveChapterSummaryListForAStoryDataProvider(storyId: String): DataProvider<List<ChapterSummaryDomain>> {
    return dataProviders.transformAsync<ChapterSummaryDatabase, List<ChapterSummaryDomain>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      chapterSummaryDataStore
    ) {
      val chapterSummaryList = it.chapterSummaryDatabaseMap.values.filter { chapterSummaryDomain ->
        chapterSummaryDomain.storyId == storyId
      }
      AsyncResult.success(chapterSummaryList.toList())
    }
  }

  private fun retrieveTopicListDataProvider(): DataProvider<List<TopicDomain>> {
    return dataProviders.transformAsync<TopicDatabase, List<TopicDomain>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      topicDataStore
    ) {
      AsyncResult.success(it.topicDatabaseMap.values.toList())
    }
  }

  private fun retrieveChapterSummaryListDataProvider(): DataProvider<List<ChapterSummaryDomain>> {
    return dataProviders.transformAsync<ChapterSummaryDatabase, List<ChapterSummaryDomain>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      chapterSummaryDataStore
    ) {
      AsyncResult.success(it.chapterSummaryDatabaseMap.values.toList())
    }
  }

  private fun retrieveChapterSummaryListForATopicDataProvider(topicId: String): DataProvider<List<ChapterSummaryDomain>> {
    return dataProviders.transformAsync<ChapterSummaryDatabase, List<ChapterSummaryDomain>>(
      GET_TOPIC_TRANSFORMED_PROVIDER_ID,
      chapterSummaryDataStore
    ) {
      val chapterSummaryDomainList =
        it.chapterSummaryDatabaseMap.values.filter { chapterSummaryDomain ->
          chapterSummaryDomain.topicId == topicId
        }
      AsyncResult.success(chapterSummaryDomainList)
    }
  }

  private fun combineStorySummaryListAndChapterSummaryList(
    storySummaryDomainList: List<StorySummaryDomain>,
    chapterSummaryDomainList: List<ChapterSummaryDomain>
  ): List<StorySummaryView> {
    val storySummaryViewList = ArrayList<StorySummaryView>()
    storySummaryDomainList.forEach { storySummaryDomain ->
      val currentChapterSummaryDomainList =
        chapterSummaryDomainList.filter { chapterSummaryDomain -> chapterSummaryDomain.storyId == storySummaryDomain.storyId }
      val storySummaryView = combineStorySummaryAndChapterSummaryList(
        storySummaryDomain,
        currentChapterSummaryDomainList
      )
      storySummaryViewList.add(storySummaryView)
    }
    return storySummaryViewList
  }

  private fun combineStorySummaryAndChapterSummaryList(
    storySummaryDomain: StorySummaryDomain,
    chapterSummaryDomainList: List<ChapterSummaryDomain>
  ): StorySummaryView {
    val chapterSummaryViewList = ArrayList<ChapterSummaryView>()
    chapterSummaryDomainList.forEach { chapterSummaryDomain ->
      val chapterSummaryView = convertChapterSummaryDomainToChapterSummaryView(chapterSummaryDomain)
      chapterSummaryViewList.add(chapterSummaryView)
    }
    val storySummaryViewBuilder =
      convertStorySummaryDomainToStorySummaryView(storySummaryDomain).toBuilder()
    storySummaryViewBuilder.addAllChapter(chapterSummaryViewList)
    return storySummaryViewBuilder.build()
  }

  private fun combineTopicListAndChapterSummaryList(
    topicDomainList: List<TopicDomain>,
    chapterSummaryDomainList: List<ChapterSummaryDomain>
  ): TopicSummaryListView {
    val topicSummaryListView = TopicSummaryListView.newBuilder()
    topicDomainList.forEach { topicDomain ->
      val topicSummaryView = convertTopicDomainToTopicSummaryView(topicDomain)
      val chapterCount =
        chapterSummaryDomainList.filter { chapterSummaryDomain ->
          chapterSummaryDomain.topicId == topicDomain.topicId
        }.size
      topicSummaryListView.addTopicSummary(
        topicSummaryView.toBuilder().setTotalChapterCount(
          chapterCount
        ).build()
      )
    }
    return topicSummaryListView.build()
  }

  /**
   * Adds a topic to offline storage.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addTopic(topicDomain: TopicDomain): DataProvider<Any?> {
    val deferred = topicDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val topicDatabaseBuilder = it.toBuilder().putTopicDatabase(topicDomain.topicId, topicDomain)
      Pair(topicDatabaseBuilder.build(), TopicActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(ADD_TOPIC_TRANSFORMED_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResultForTopic(
        topicDomain.topicId,
        deferred
      )
    }
  }

  /**
   * Adds a list of subtopic to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addSubTopicList(subtopicDomainList: List<SubtopicDomain>): DataProvider<Any?> {
    val deferred =
      subtopicDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val subtopicDatabaseBuilder = it.toBuilder()
        subtopicDomainList.forEach { subtopicDomain ->
          subtopicDatabaseBuilder.putSubtopicDatabase(
            subtopicDomain.subtopicId,
            subtopicDomain
          )
        }
        Pair(subtopicDatabaseBuilder.build(), SubtopicActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(ADD_SUBTOPIC_LIST_TRANSFORMED_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResultForSubtopic(deferred)
    }
  }

  /**
   * Adds a list of skills to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addSkillSummaryList(skillSummaryDomainList: List<SkillSummaryDomain>): DataProvider<Any?> {
    val deferred =
      skillSummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val skillSummaryDatabaseBuilder = it.toBuilder()
        skillSummaryDomainList.forEach { skillSummaryDomain ->
          skillSummaryDatabaseBuilder.putSkillSummaryDatabase(
            skillSummaryDomain.skillId,
            skillSummaryDomain
          )
        }
        Pair(skillSummaryDatabaseBuilder.build(), SkillSummaryActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(
      ADD_SKILL_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResultForSkillSummary(deferred)
    }
  }

  /**
   * Adds a list of stories to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addStorySummaryList(storySummaryDomainList: List<StorySummaryDomain>): DataProvider<Any?> {
    val deferred =
      storySummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val storySummaryDatabaseBuilder = it.toBuilder()
        storySummaryDomainList.forEach { storySummaryDomain ->
          storySummaryDatabaseBuilder.putStorySummaryDatabase(
            storySummaryDomain.storyId,
            storySummaryDomain
          )
        }
        Pair(storySummaryDatabaseBuilder.build(), StorySummaryActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(
      ADD_STORY_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResultForStorySummary(deferred)
    }
  }

  /**
   * Adds a list of stories to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addChapterSummaryList(chapterSummaryDomainList: List<ChapterSummaryDomain>): DataProvider<Any?> {
    val deferred =
      chapterSummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val chapterSummaryDatabaseBuilder = it.toBuilder()
        chapterSummaryDomainList.forEach { chapterSummaryDomain ->
          chapterSummaryDatabaseBuilder.putChapterSummaryDatabase(
            chapterSummaryDomain.explorationId,
            chapterSummaryDomain
          )
        }
        Pair(chapterSummaryDatabaseBuilder.build(), ChapterSummaryActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(
      ADD_CHAPTER_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResultForChapterSummary(deferred)
    }
  }

  /**
   * Adds a list of stories to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addRevisionCardList(revisionCardDomainList: List<RevisionCardDomain>): DataProvider<Any?> {
    val deferred =
      revisionCardDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val revisionCardDatabaseBuilder = it.toBuilder()
        revisionCardDomainList.forEach { revisionCardDomain ->
          revisionCardDatabaseBuilder.putSubtopicContentDatabase(
            revisionCardDomain.subtopicTitle,
            revisionCardDomain
          )
        }
        Pair(revisionCardDatabaseBuilder.build(), RevisionCardActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(
      ADD_CHAPTER_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResultForRevisionCard(deferred)
    }
  }

  private fun convertChapterSummaryDomainToChapterSummaryView(
    chapterSummaryDomain: ChapterSummaryDomain
  ): ChapterSummaryView {
    return ChapterSummaryView.newBuilder()
      .setExplorationId(chapterSummaryDomain.explorationId)
      .setName(chapterSummaryDomain.name)
      .setSummary(chapterSummaryDomain.summary)
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(chapterSummaryDomain.chapterThumbnail)
      .build()
  }

  private fun convertStorySummaryDomainToStorySummaryView(
    storySummaryDomain: StorySummaryDomain
  ): StorySummaryView {
    return StorySummaryView.newBuilder()
      .setStoryId(storySummaryDomain.storyId)
      .setStoryName(storySummaryDomain.storyName)
      .setStoryThumbnail(storySummaryDomain.storyThumbnail)
      .build()
  }

  private fun convertTopicDomainToTopicSummaryView(topicDomain: TopicDomain): TopicSummaryView {
    return TopicSummaryView.newBuilder()
      .setTopicId(topicDomain.topicId)
      .setName(topicDomain.name)
      .setVersion(1)
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicDomain.topicId))
      .build()
  }

  private fun convertSkillSummaryDomainToSkillSummaryView(skillSummaryDomain: SkillSummaryDomain?): SkillSummaryView {
    return if (skillSummaryDomain == null) {
      SkillSummaryView.getDefaultInstance()
    } else {
      SkillSummaryView.newBuilder()
        .setSkillId(skillSummaryDomain.skillId)
        .setDescription(skillSummaryDomain.description)
        .setSkillThumbnail(skillSummaryDomain.skillThumbnail)
        .setThumbnailUrl(skillSummaryDomain.thumbnailUrl)
        .build()
    }
  }

  private fun convertSubtopicDomainToSubtopicView(subtopicDomain: SubtopicDomain?): SubtopicView {
    return if (subtopicDomain == null) {
      SubtopicView.getDefaultInstance()
    } else {
      SubtopicView.newBuilder()
        .setSubtopicId(subtopicDomain.subtopicId)
        .setTitle(subtopicDomain.title)
        .addAllSkillIds(subtopicDomain.skillIdsList)
        .setSubtopicThumbnail(subtopicDomain.subtopicThumbnail)
        .setThumbnailUrl(subtopicDomain.thumbnailUrl)
        .build()
    }
  }

  private fun convertTopicDomainToTopicView(topicDomain: TopicDomain?): TopicView {
    return if (topicDomain == null) {
      TopicView.getDefaultInstance()
    } else {
      TopicView.newBuilder()
        .setTopicId(topicDomain.topicId)
        .setName(topicDomain.name)
        .setDescription(topicDomain.description)
        .setTopicThumbnail(topicDomain.topicThumbnail)
        .setDiskSizeBytes(topicDomain.diskSizeBytes)
        .build()
    }
  }

  private suspend fun getDeferredResultForChapterSummary(deferred: Deferred<ChapterSummaryActionStatus>): AsyncResult<Any?> {
    return when (deferred.await()) {
      ChapterSummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      ChapterSummaryActionStatus.CHAPTER_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        ChapterSummaryAlreadyExistsException("Chapter summary list is already present.")
      )
      ChapterSummaryActionStatus.CHAPTER_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        ChapterSummaryNotFoundException("Chapter summary list not found.")
      )
    }
  }

  private suspend fun getDeferredResultForRevisionCard(deferred: Deferred<RevisionCardActionStatus>): AsyncResult<Any?> {
    return when (deferred.await()) {
      RevisionCardActionStatus.SUCCESS -> AsyncResult.success(null)
      RevisionCardActionStatus.REVISION_CARD_ALREADY_EXISTS -> AsyncResult.failed(
        RevisionCardAlreadyExistsException("Revision card is already present.")
      )
      RevisionCardActionStatus.REVISION_CARD_NOT_FOUND -> AsyncResult.failed(
        RevisionCardNotFoundException("Revision card not found.")
      )
    }
  }

  private suspend fun getDeferredResultForSkillSummary(deferred: Deferred<SkillSummaryActionStatus>): AsyncResult<Any?> {
    return when (deferred.await()) {
      SkillSummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      SkillSummaryActionStatus.SKILL_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        SkillSummaryAlreadyExistsException("Skill summary list is already present.")
      )
      SkillSummaryActionStatus.SKILL_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        SkillSummaryNotFoundException("Skill summary list not found.")
      )
    }
  }

  private suspend fun getDeferredResultForStorySummary(deferred: Deferred<StorySummaryActionStatus>): AsyncResult<Any?> {
    return when (deferred.await()) {
      StorySummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      StorySummaryActionStatus.STORY_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        StorySummaryAlreadyExistsException("Story summary list is already present.")
      )
      StorySummaryActionStatus.STORY_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        StorySummaryNotFoundException("Story summary list not found.")
      )
    }
  }

  private suspend fun getDeferredResultForSubtopic(deferred: Deferred<SubtopicActionStatus>): AsyncResult<Any?> {
    return when (deferred.await()) {
      SubtopicActionStatus.SUCCESS -> AsyncResult.success(null)
      SubtopicActionStatus.SUBTOPIC_ALREADY_EXISTS -> AsyncResult.failed(
        SubtopicAlreadyExistsException("Subtopic list is already present.")
      )
      SubtopicActionStatus.SUBTOPIC_NOT_FOUND -> AsyncResult.failed(
        SubtopicNotFoundException("Subtopic list not found.")
      )
    }
  }

  private suspend fun getDeferredResultForTopic(
    topicId: String,
    deferred: Deferred<TopicActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      TopicActionStatus.SUCCESS -> AsyncResult.success(null)
      TopicActionStatus.TOPIC_ALREADY_EXISTS -> AsyncResult.failed(
        TopicAlreadyExistsException("Topic for topicId $topicId is already present.")
      )
      TopicActionStatus.TOPIC_NOT_FOUND -> AsyncResult.failed(
        TopicNotFoundException("Topic for topicId $topicId not found.")
      )
    }
  }

  private fun retrieveTopicDomainFromJson(topicId: String): TopicDomain {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createTopicDomainFromJson("fractions_topic.json")
      RATIOS_TOPIC_ID -> createTopicDomainFromJson("ratios_topic.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  private fun retrieveSubtopicDomainListFromJson(topicId: String): List<SubtopicDomain> {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createSubtopicDomainListFromJson("fractions_topic.json")
      RATIOS_TOPIC_ID -> createSubtopicDomainListFromJson("ratios_topic.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  private fun retrieveSkillSummaryDomainListFromJson(topicId: String): List<SkillSummaryDomain> {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createSkillsFromJson("fractions_skills.json")
      RATIOS_TOPIC_ID -> createSkillsFromJson("ratios_skills.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  private fun retrieveStorySummaryDomainListFromJson(topicId: String): List<StorySummaryDomain> {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createStoriesFromJson(topicId, "fractions_stories.json")
      RATIOS_TOPIC_ID -> createStoriesFromJson(topicId, "ratios_stories.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  private fun retrieveChapterSummaryDomainListFromJson(topicId: String): List<ChapterSummaryDomain> {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> {
        val storyData =
          jsonAssetRetriever.loadJsonFromAsset("fractions_stories.json")?.getJSONArray("story_list")!!
        createChaptersFromJson(topicId, storyData)
      }
      RATIOS_TOPIC_ID -> {
        val storyData =
          jsonAssetRetriever.loadJsonFromAsset("ratios_stories.json")?.getJSONArray("story_list")!!
        createChaptersFromJson(topicId, storyData)
      }
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  private fun retrieveRevisionCardDomainListFromJson(topicId: String): List<RevisionCardDomain> {
    val revisionCardDomainList = ArrayList<RevisionCardDomain>()
    when (topicId) {
      FRACTIONS_TOPIC_ID -> {
        revisionCardDomainList.add(retrieveRevisionCardDomainFromJson(FRACTIONS_SUBTOPIC_ID_1))
        revisionCardDomainList.add(retrieveRevisionCardDomainFromJson(FRACTIONS_SUBTOPIC_ID_2))
        revisionCardDomainList.add(retrieveRevisionCardDomainFromJson(FRACTIONS_SUBTOPIC_ID_3))
      }
    }
    return revisionCardDomainList
  }

  private fun retrieveRevisionCardDomainFromJson(subtopicId: String): RevisionCardDomain {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      FRACTIONS_SUBTOPIC_ID_2 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      FRACTIONS_SUBTOPIC_ID_3 -> createSubtopicFromJson(
        "fractions_subtopics.json"
      )
      else -> throw IllegalArgumentException("Invalid subtopicId: $subtopicId")
    }
  }

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicDomainFromJson(topicFileName: String): TopicDomain {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val topicId = topicData.getString("id")
    return TopicDomain.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("name"))
      .setDescription(topicData.getString("description"))
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
      .setDiskSizeBytes(computeTopicSizeBytes(TOPIC_FILE_ASSOCIATIONS.getValue(topicId)))
      .build()
  }

  /**
   * Creates subtopic list from its json representation. The json file is expected to have
   * a key called 'subtopics' that holds the subtopic data.
   */
  private fun createSubtopicDomainListFromJson(topicFileName: String): List<SubtopicDomain> {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val topicId = topicData.getString("id")
    return createSubtopicDomainListFromJsonArray(topicId, topicData.optJSONArray("subtopics"))
  }

  /** Creates a sub-topic from its json representation. */
  private fun createSubtopicFromJson(subtopicFileName: String): RevisionCardDomain {
    val subtopicData =
      jsonAssetRetriever.loadJsonFromAsset(subtopicFileName)?.getJSONObject("page_contents")!!
    val subtopicTitle =
      jsonAssetRetriever.loadJsonFromAsset(subtopicFileName)?.getString("subtopic_title")!!
    return RevisionCardDomain.newBuilder()
      .setSubtopicTitle(subtopicTitle)
      .setPageContents(
        SubtitledHtml.newBuilder()
          .setHtml(subtopicData.getJSONObject("subtitled_html").getString("html"))
          .setContentId(subtopicData.getJSONObject("subtitled_html").getString("content_id")).build()
      )
      .build()
  }

  /**
   * Creates the subtopic list of a topic from its json representation. The json file is expected to have
   * a key called 'subtopic' that contains an array of skill Ids,subtopic_id and title.
   */
  private fun createSubtopicDomainListFromJsonArray(
    topicId: String,
    subtopicJsonArray: JSONArray?
  ): List<SubtopicDomain> {
    val subtopicDomainList = mutableListOf<SubtopicDomain>()
    for (i in 0 until subtopicJsonArray!!.length()) {
      val skillIdList = ArrayList<String>()

      val currentSubtopicJsonObject = subtopicJsonArray.optJSONObject(i)
      val skillJsonArray = currentSubtopicJsonObject.optJSONArray("skill_ids")

      for (j in 0 until skillJsonArray.length()) {
        skillIdList.add(skillJsonArray.optString(j))
      }
      val subtopicDomain = SubtopicDomain.newBuilder()
        .setTopicId(topicId)
        .setSubtopicId(currentSubtopicJsonObject.optString("id"))
        .setTitle(currentSubtopicJsonObject.optString("title"))
        .setSubtopicThumbnail(createSubtopicThumbnail(currentSubtopicJsonObject.optString("id")))
        .addAllSkillIds(skillIdList).build()
      subtopicDomainList.add(subtopicDomain)
    }
    return subtopicDomainList
  }

  private fun computeTopicSizeBytes(constituentFiles: List<String>): Long {
    // TODO(#169): Compute this based on protos & the combined topic package.
    // TODO(#386): Incorporate audio & image files in this computation.
    return constituentFiles.map(jsonAssetRetriever::getAssetSize).map(Int::toLong)
      .reduceRight(Long::plus)
  }

  /**
   * Creates a list of skill for topic from its json representation. The json file is expected to have
   * a key called 'skill_list' that contains an array of skill objects, each with the key 'skill'.
   */
  private fun createSkillsFromJson(fileName: String): List<SkillSummaryDomain> {
    val skillList = mutableListOf<SkillSummaryDomain>()
    val skillData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("skill_list")!!
    for (i in 0 until skillData.length()) {
      skillList.add(createSkillFromJson(skillData.getJSONObject(i).getJSONObject("skill")))
    }
    return skillList
  }

  private fun createSkillFromJson(skillData: JSONObject): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(skillData.getString("id"))
      .setDescription(skillData.getString("description"))
      .setSkillThumbnail(createSkillThumbnail(skillData.getString("id")))
      .build()
  }

  /**
   * Creates a list of [StorySummary]s for topic from its json representation. The json file is expected to have
   * a key called 'story_list' that contains an array of story objects, each with the key 'story'.
   */
  private fun createStoriesFromJson(topicId: String, fileName: String): List<StorySummaryDomain> {
    val storySummaryDomainList = mutableListOf<StorySummaryDomain>()
    val storySummaryData =
      jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    for (i in 0 until storySummaryData.length()) {
      storySummaryDomainList.add(
        createStoryFromJson(
          topicId,
          storySummaryData.getJSONObject(i).getJSONObject("story")
        )
      )
    }
    return storySummaryDomainList
  }

  /** Creates a list of [StorySummaryDomain]s for topic given its json representation and the index of the story in json. */
  private fun createStoryFromJson(topicId: String, storyData: JSONObject): StorySummaryDomain {
    val storyId = storyData.getString("id")
    return StorySummaryDomain.newBuilder()
      .setTopicId(topicId)
      .setStoryId(storyId)
      .setStoryName(storyData.getString("title"))
      .setStoryThumbnail(STORY_THUMBNAILS.getValue(storyId))
      .build()
  }

  private fun createChaptersFromJson(
    topicId: String,
    storiesData: JSONArray
  ): List<ChapterSummaryDomain> {
    val chapterSummaryDomainList = mutableListOf<ChapterSummaryDomain>()
    for (i in 0 until storiesData.length()) {
      val storyData = storiesData.getJSONObject(i).getJSONObject("story")
      val chapterData = storyData.getJSONObject("story_contents").getJSONArray("nodes")
      for (j in 0 until chapterData.length()) {
        val chapter = chapterData.getJSONObject(i)
        val explorationId = chapter.getString("exploration_id")
        chapterSummaryDomainList.add(
          ChapterSummaryDomain.newBuilder()
            .setTopicId(topicId)
            .setStoryId(storyData.getString("id"))
            .setExplorationId(explorationId)
            .setName(chapter.getString("title"))
            .setChapterPlayState(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
            .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
            .build()
        )
      }

    }
    return chapterSummaryDomainList
  }

  private fun createSkillThumbnail(skillId: String): LessonThumbnail {
    return when (skillId) {
      FRACTIONS_SKILL_ID_0 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
      FRACTIONS_SKILL_ID_1 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.WRITING_FRACTIONS)
        .build()
      FRACTIONS_SKILL_ID_2 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS_AND_IMPROPER_FRACTIONS)
        .build()
      RATIOS_SKILL_ID_0 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.DERIVE_A_RATIO)
        .build()
      else -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
        .build()
    }
  }

  private fun createSubtopicThumbnail(subtopicId: String): LessonThumbnail {
    return when (subtopicId) {
      FRACTIONS_SUBTOPIC_ID_1 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.WHAT_IS_A_FRACTION)
        .build()
      FRACTIONS_SUBTOPIC_ID_2 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.FRACTION_OF_A_GROUP)
        .build()
      FRACTIONS_SUBTOPIC_ID_3 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.MIXED_NUMBERS)
        .build()
      FRACTIONS_SUBTOPIC_ID_4 -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_FRACTIONS)
        .build()
      else -> LessonThumbnail.newBuilder()
        .setThumbnailGraphic(LessonThumbnailGraphic.THE_NUMBER_LINE)
        .build()
    }
  }
}