package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ChapterSummaryDatabase
import org.oppia.app.model.ChapterSummaryDomain
import org.oppia.app.model.ChapterSummaryView
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.RevisionCard
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.SkillSummaryDatabase
import org.oppia.app.model.SkillSummaryDomain
import org.oppia.app.model.SkillSummaryView
import org.oppia.app.model.StorySummary
import org.oppia.app.model.StorySummaryDatabase
import org.oppia.app.model.StorySummaryDomain
import org.oppia.app.model.StorySummaryView
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Subtopic
import org.oppia.app.model.SubtopicDatabase
import org.oppia.app.model.SubtopicDomain
import org.oppia.app.model.SubtopicView
import org.oppia.app.model.TopicBackend
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
      val topicBackend = retrieveTopicFromJSON(topicId)
      addTopic(topicBackend)
      addSubTopicList(topicBackend)
      addSkillSummary(topicBackend)
      addStorySummary(topicBackend)
      addChapterSummary(topicBackend)
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
  private fun addTopic(topicBackend: TopicBackend): DataProvider<Any?> {
    val deferred = topicDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val topicDomain = convertTopicBackendToTopicDomain(topicBackend)
      val topicDatabaseBuilder = it.toBuilder().putTopicDatabase(topicBackend.topicId, topicDomain)
      Pair(topicDatabaseBuilder.build(), TopicActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(ADD_TOPIC_TRANSFORMED_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResultForTopic(
        topicBackend.topicId,
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
  private fun addSubTopicList(topicBackend: TopicBackend): DataProvider<Any?> {
    val deferred =
      subtopicDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val subtopicListDomainList = convertTopicBackendToSubtopicListDomain(topicBackend)
        val subtopicDatabaseBuilder = it.toBuilder()
        subtopicListDomainList.forEach { subtopicDomain ->
          subtopicDatabaseBuilder.putSubtopicDatabase(
            subtopicDomain.subtopicId,
            subtopicDomain
          )
        }
        Pair(subtopicDatabaseBuilder.build(), SubtopicActionStatus.SUCCESS)
      }
    return dataProviders.createInMemoryDataProviderAsync(ADD_SUBTOPIC_LIST_TRANSFORMED_PROVIDER_ID) {
      return@createInMemoryDataProviderAsync getDeferredResultForSubtopic(
        topicBackend.topicId,
        deferred
      )
    }
  }

  /**
   * Adds a list of skills to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addSkillSummary(topicBackend: TopicBackend): DataProvider<Any?> {
    val deferred =
      skillSummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val skillSummaryDomainList = convertTopicBackendToSkillSummaryDomain(topicBackend)
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
      return@createInMemoryDataProviderAsync getDeferredResultForSkillSummary(
        topicBackend.topicId,
        deferred
      )
    }
  }

  /**
   * Adds a list of stories to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addStorySummary(topicBackend: TopicBackend): DataProvider<Any?> {
    val deferred =
      storySummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val storySummaryDomainList = convertTopicBackendToStorySummaryDomain(topicBackend)
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
      return@createInMemoryDataProviderAsync getDeferredResultForStorySummary(
        topicBackend.topicId,
        deferred
      )
    }
  }

  /**
   * Adds a list of stories to offline storage linked to a particular topic.
   *
   * @param topicBackend TopicBackend which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  private fun addChapterSummary(topicBackend: TopicBackend): DataProvider<Any?> {
    val deferred =
      chapterSummaryDataStore.storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
        val chapterSummaryDomainList = convertTopicToChapterSummaryList(topicBackend)
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
      return@createInMemoryDataProviderAsync getDeferredResultForChapterSummary(
        topicBackend.topicId,
        deferred
      )
    }
  }

  private fun convertTopicBackendToTopicDomain(topicBackend: TopicBackend): TopicDomain {
    return TopicDomain.newBuilder()
      .setTopicId(topicBackend.topicId)
      .setName(topicBackend.name)
      .setDescription(topicBackend.description)
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicBackend.topicId))
      .setDiskSizeBytes(topicBackend.diskSizeBytes)
      .build()
  }

  private fun convertTopicBackendToSkillSummaryDomain(topicBackend: TopicBackend): List<SkillSummaryDomain> {
    val skillSummaryDomainList = ArrayList<SkillSummaryDomain>()
    topicBackend.skillList.forEach { skillSummary ->
      val skillSummaryDomain = convertSkillSummaryToSkillSummaryDomain(skillSummary)
      skillSummaryDomainList.add(skillSummaryDomain)
    }
    return skillSummaryDomainList
  }

  private fun convertTopicToChapterSummaryList(topicBackend: TopicBackend): List<ChapterSummaryDomain> {
    val chapterSummaryList = ArrayList<ChapterSummaryDomain>()
    topicBackend.storyList.forEach { storySummary ->
      storySummary.chapterList.forEach { chapterSummary ->
        val chapterSummaryDomain = convertChapterSummaryToChapterSummaryDomain(
          topicBackend.topicId,
          storySummary.storyId,
          chapterSummary
        )
        chapterSummaryList.add(chapterSummaryDomain)
      }
    }
    return chapterSummaryList
  }

  private fun convertChapterSummaryToChapterSummaryDomain(
    topicId: String,
    storyId: String,
    chapterSummary: ChapterSummary
  ): ChapterSummaryDomain {
    return ChapterSummaryDomain.newBuilder()
      .setTopicId(topicId)
      .setStoryId(storyId)
      .setExplorationId(chapterSummary.explorationId)
      .setName(chapterSummary.name)
      .setSummary(chapterSummary.summary)
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(chapterSummary.chapterThumbnail)
      .build()
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

  private fun convertSkillSummaryToSkillSummaryDomain(skillSummary: SkillSummary): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(skillSummary.skillId)
      .setDescription(skillSummary.description)
      .setThumbnailUrl(skillSummary.thumbnailUrl)
      .setSkillThumbnail(skillSummary.skillThumbnail)
      .build()
  }

  private fun convertTopicBackendToStorySummaryDomain(topicBackend: TopicBackend): List<StorySummaryDomain> {
    val storySummaryDomainList = ArrayList<StorySummaryDomain>()
    topicBackend.storyList.forEach { storySummary ->
      val storySummaryDomain =
        convertStorySummaryToStorySummaryDomain(topicBackend.topicId, storySummary)
      storySummaryDomainList.add(storySummaryDomain)
    }
    return storySummaryDomainList
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

  private fun convertStorySummaryToStorySummaryDomain(
    topicId: String,
    storySummary: StorySummary
  ): StorySummaryDomain {
    return StorySummaryDomain.newBuilder()
      .setTopicId(topicId)
      .setStoryId(storySummary.storyId)
      .setStoryName(storySummary.storyName)
      .setStoryThumbnail(storySummary.storyThumbnail)
      .build()
  }

  private fun convertTopicBackendToSubtopicListDomain(topicBackend: TopicBackend): List<SubtopicDomain> {
    val subtopicDomainList = ArrayList<SubtopicDomain>()
    topicBackend.subtopicList.forEach { subtopic ->
      val subtopicDomain = convertSubtopicToSubtopicDomain(topicBackend.topicId, subtopic)
      subtopicDomainList.add(subtopicDomain)
    }
    return subtopicDomainList
  }

  private fun convertSubtopicToSubtopicDomain(topicId: String, subtopic: Subtopic): SubtopicDomain {
    return SubtopicDomain.newBuilder()
      .setTopicId(topicId)
      .setSubtopicId(subtopic.subtopicId)
      .setTitle(subtopic.title)
      .setSubtopicThumbnail(subtopic.subtopicThumbnail)
      .setThumbnailUrl(subtopic.thumbnailUrl)
      .addAllSkillIds(subtopic.skillIdsList)
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

  private suspend fun getDeferredResultForChapterSummary(
    topicId: String,
    deferred: Deferred<ChapterSummaryActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      ChapterSummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      ChapterSummaryActionStatus.CHAPTER_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        ChapterSummaryAlreadyExistsException("Chapter summary list for topic $topicId is already present.")
      )
      ChapterSummaryActionStatus.CHAPTER_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        ChapterSummaryNotFoundException("Chapter summary list for topic $topicId not found.")
      )
    }
  }

  private suspend fun getDeferredResultForSkillSummary(
    topicId: String,
    deferred: Deferred<SkillSummaryActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      SkillSummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      SkillSummaryActionStatus.SKILL_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        SkillSummaryAlreadyExistsException("Skill summary list for topic $topicId is already present.")
      )
      SkillSummaryActionStatus.SKILL_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        SkillSummaryNotFoundException("Skill summary list for topic $topicId not found.")
      )
    }
  }

  private suspend fun getDeferredResultForStorySummary(
    topicId: String,
    deferred: Deferred<StorySummaryActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      StorySummaryActionStatus.SUCCESS -> AsyncResult.success(null)
      StorySummaryActionStatus.STORY_SUMMARY_ALREADY_EXISTS -> AsyncResult.failed(
        StorySummaryAlreadyExistsException("Story summary list for topic $topicId is already present.")
      )
      StorySummaryActionStatus.STORY_SUMMARY_NOT_FOUND -> AsyncResult.failed(
        StorySummaryNotFoundException("Story summary list for topic $topicId not found.")
      )
    }
  }

  private suspend fun getDeferredResultForSubtopic(
    topicId: String,
    deferred: Deferred<SubtopicActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      SubtopicActionStatus.SUCCESS -> AsyncResult.success(null)
      SubtopicActionStatus.SUBTOPIC_ALREADY_EXISTS -> AsyncResult.failed(
        SubtopicAlreadyExistsException("Subtopic list for topic $topicId is already present.")
      )
      SubtopicActionStatus.SUBTOPIC_NOT_FOUND -> AsyncResult.failed(
        SubtopicNotFoundException("Subtopic list for topic $topicId not found.")
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

  private fun retrieveTopicFromJSON(topicId: String): TopicBackend {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createTopicFromJson(
        "fractions_topic.json", "fractions_skills.json", "fractions_stories.json"
      )
      RATIOS_TOPIC_ID -> createTopicFromJson(
        "ratios_topic.json", "ratios_skills.json", "ratios_stories.json"
      )
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
  }

  internal fun retrieveStory(storyId: String): StorySummary {
    return when (storyId) {
      FRACTIONS_STORY_ID_0 -> createStoryFromJsonFile("fractions_stories.json", /* index= */ 0)
      RATIOS_STORY_ID_0 -> createStoryFromJsonFile("ratios_stories.json", /* index= */ 0)
      RATIOS_STORY_ID_1 -> createStoryFromJsonFile("ratios_stories.json", /* index= */ 1)
      else -> throw IllegalArgumentException("Invalid story ID: $storyId")
    }
  }

  // TODO(#45): Expose this as a data provider, or omit if it's not needed.
  private fun retrieveReviewCard(topicId: String, subtopicId: String): RevisionCard {
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
      else -> throw IllegalArgumentException("Invalid topic Name: $topicId")
    }
  }

  /**
   * Creates topic from its json representation. The json file is expected to have
   * a key called 'topic' that holds the topic data.
   */
  private fun createTopicFromJson(
    topicFileName: String,
    skillFileName: String,
    storyFileName: String
  ): TopicBackend {
    val topicData = jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("topic")!!
    val subtopicList: List<Subtopic> =
      createSubtopicListFromJsonArray(topicData.optJSONArray("subtopics"))
    val topicId = topicData.getString("id")
    return TopicBackend.newBuilder()
      .setTopicId(topicId)
      .setName(topicData.getString("name"))
      .setDescription(topicData.getString("description"))
      .addAllSkill(createSkillsFromJson(skillFileName))
      .addAllStory(createStoriesFromJson(storyFileName))
      .setTopicThumbnail(TOPIC_THUMBNAILS.getValue(topicId))
      .setDiskSizeBytes(computeTopicSizeBytes(TOPIC_FILE_ASSOCIATIONS.getValue(topicId)))
      .addAllSubtopic(subtopicList)
      .build()
  }

  /** Creates a sub-topic from its json representation. */
  private fun createSubtopicFromJson(topicFileName: String): RevisionCard {
    val subtopicData =
      jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getJSONObject("page_contents")!!
    val subtopicTitle =
      jsonAssetRetriever.loadJsonFromAsset(topicFileName)?.getString("subtopic_title")!!
    return RevisionCard.newBuilder()
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
  private fun createSubtopicListFromJsonArray(subtopicJsonArray: JSONArray?): List<Subtopic> {
    val subtopicList = mutableListOf<Subtopic>()

    for (i in 0 until subtopicJsonArray!!.length()) {
      val skillIdList = ArrayList<String>()

      val currentSubtopicJsonObject = subtopicJsonArray.optJSONObject(i)
      val skillJsonArray = currentSubtopicJsonObject.optJSONArray("skill_ids")

      for (j in 0 until skillJsonArray.length()) {
        skillIdList.add(skillJsonArray.optString(j))
      }
      val subtopic = Subtopic.newBuilder().setSubtopicId(currentSubtopicJsonObject.optString("id"))
        .setTitle(currentSubtopicJsonObject.optString("title"))
        .setSubtopicThumbnail(createSubtopicThumbnail(currentSubtopicJsonObject.optString("id")))
        .addAllSkillIds(skillIdList).build()
      subtopicList.add(subtopic)
    }
    return subtopicList
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
  private fun createSkillsFromJson(fileName: String): List<SkillSummary> {
    val skillList = mutableListOf<SkillSummary>()
    val skillData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("skill_list")!!
    for (i in 0 until skillData.length()) {
      skillList.add(createSkillFromJson(skillData.getJSONObject(i).getJSONObject("skill")))
    }
    return skillList
  }

  private fun createSkillFromJson(skillData: JSONObject): SkillSummary {
    return SkillSummary.newBuilder()
      .setSkillId(skillData.getString("id"))
      .setDescription(skillData.getString("description"))
      .setSkillThumbnail(createSkillThumbnail(skillData.getString("id")))
      .build()
  }

  /**
   * Creates a list of [StorySummary]s for topic from its json representation. The json file is expected to have
   * a key called 'story_list' that contains an array of story objects, each with the key 'story'.
   */
  private fun createStoriesFromJson(fileName: String): List<StorySummary> {
    val storyList = mutableListOf<StorySummary>()
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    for (i in 0 until storyData.length()) {
      storyList.add(createStoryFromJson(storyData.getJSONObject(i).getJSONObject("story")))
    }
    return storyList
  }

  /** Creates a list of [StorySummary]s for topic given its json representation and the index of the story in json. */
  private fun createStoryFromJsonFile(fileName: String, index: Int): StorySummary {
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    if (storyData.length() < index) {
      return StorySummary.getDefaultInstance()
    }
    return createStoryFromJson(storyData.getJSONObject(index).getJSONObject("story"))
  }

  private fun createStoryFromJson(storyData: JSONObject): StorySummary {
    val storyId = storyData.getString("id")
    return StorySummary.newBuilder()
      .setStoryId(storyId)
      .setStoryName(storyData.getString("title"))
      .setStoryThumbnail(STORY_THUMBNAILS.getValue(storyId))
      .addAllChapter(createChaptersFromJson(storyData.getJSONObject("story_contents").getJSONArray("nodes")))
      .build()
  }

  private fun createChaptersFromJson(chapterData: JSONArray): List<ChapterSummary> {
    val chapterList = mutableListOf<ChapterSummary>()

    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      val explorationId = chapter.getString("exploration_id")
      chapterList.add(
        ChapterSummary.newBuilder()
          .setExplorationId(explorationId)
          .setName(chapter.getString("title"))
          .setChapterPlayState(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
          .setChapterThumbnail(EXPLORATION_THUMBNAILS.getValue(explorationId))
          .build()
      )
    }
    return chapterList
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