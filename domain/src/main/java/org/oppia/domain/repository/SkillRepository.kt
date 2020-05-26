package org.oppia.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import org.oppia.app.model.ConceptCardView
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.SkillSummaryDatabase
import org.oppia.app.model.SkillSummaryDomain
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_0
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_1
import org.oppia.domain.topic.FRACTIONS_SKILL_ID_2
import org.oppia.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.domain.topic.RATIOS_SKILL_ID_0
import org.oppia.domain.topic.RATIOS_TOPIC_ID
import org.oppia.domain.topic.TOPIC_FILE_ASSOCIATIONS
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger

private const val ADD_SKILL_SUMMARY_LIST_TRANSFORMED_PROVIDER_ID = "add_skill_list_transformed_id"
private const val GET_SKILL_SUMMARY_TRANSFORMED_PROVIDER_ID = "get_skill_summary_transformed_id"

/** Controller for retrieving all aspects of a topic. */
@Singleton
class SkillRepository @Inject constructor(
  cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val logger: Logger
) {

  /** Indicates that the given skill summary already exists. */
  class SkillSummaryAlreadyExistsException(msg: String) : Exception(msg)

  /** Indicates that the given skill summary is not found. */
  class SkillSummaryNotFoundException(msg: String) : Exception(msg)

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

  private val skillSummaryDataStore =
    cacheStoreFactory.create(
      "skill_summary_database",
      SkillSummaryDatabase.getDefaultInstance()
    )

  // TODO(#272): Remove init block when storeDataAsync is fixed
  init {
    skillSummaryDataStore.primeCacheAsync().invokeOnCompletion {
      it?.let {
        logger.e(
          "DOMAIN",
          "Failed to prime cache skillSummaryDataStore ahead of LiveData conversion for SkillRepository.",
          it
        )
      }
    }
  }

  /**
   * This method stores all skills to proto database by reading these skills from corresponding json files
   * and returns a successful [LiveData]
   */
  fun storeAllSkillSummariesToProtoDatabase(): LiveData<AsyncResult<Any?>> {
    return MutableLiveData(
      try {
        TOPIC_FILE_ASSOCIATIONS.keys.forEach { topicId ->
          addSkillSummaryList(retrieveSkillSummaryDomainListFromJson(topicId))
        }
        AsyncResult.success<Any?>(SkillSummaryActionStatus.SUCCESS)
      } catch (e: Exception) {
        AsyncResult.failed<Any?>(e)
      }
    )
  }

  /**
   * Returns the [ConceptCardView] that can be viewed in concept cards.
   *
   * @param skillId the ID corresponding to the skill for which [ConceptCardView] needs to be fetched.
   * @return a [DataProvider] for a [ConceptCardView].
   */
  fun getSkillSummaryDataProvider(skillId: String): DataProvider<ConceptCardView> {
    return dataProviders.transformAsync<SkillSummaryDatabase, ConceptCardView>(
      GET_SKILL_SUMMARY_TRANSFORMED_PROVIDER_ID,
      skillSummaryDataStore
    ) {
      val skillSummaryDomain = it.skillSummaryDatabaseMap[skillId]
      val conceptCardView = convertSkillSummaryDomainToConceptCardView(skillSummaryDomain)
      AsyncResult.success(conceptCardView)
    }
  }

  /**
   * Adds a list of skills to offline storage.
   *
   * @param skillSummaryDomainList List of skill summaries which needs to be saved offline.
   * @return a [LiveData] that indicates the success/failure of this add operation.
   */
  fun addSkillSummaryList(skillSummaryDomainList: List<SkillSummaryDomain>): DataProvider<Any?> {
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

  private fun convertSkillSummaryDomainToConceptCardView(skillSummaryDomain: SkillSummaryDomain?): ConceptCardView {
    return if (skillSummaryDomain == null) {
      ConceptCardView.getDefaultInstance()
    } else {
      ConceptCardView.newBuilder()
        .setSkillId(skillSummaryDomain.skillId)
        .setDescription(skillSummaryDomain.description)
        .setSkillThumbnail(skillSummaryDomain.skillThumbnail)
        .setThumbnailUrl(skillSummaryDomain.thumbnailUrl)
        .build()
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

  private fun retrieveSkillSummaryDomainListFromJson(topicId: String): List<SkillSummaryDomain> {
    return when (topicId) {
      FRACTIONS_TOPIC_ID -> createSkillsFromJson("fractions_skills.json")
      RATIOS_TOPIC_ID -> createSkillsFromJson("ratios_skills.json")
      else -> throw IllegalArgumentException("Invalid topic ID: $topicId")
    }
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
}
