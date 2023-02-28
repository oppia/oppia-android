package org.oppia.android.scripts.gae.compat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.oppia.android.scripts.gae.compat.LoadResult.Companion.combine
import org.oppia.android.scripts.gae.compat.LoadResult.Companion.flatten
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityFailure
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityResult
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityResult.Compatible
import org.oppia.android.scripts.gae.compat.StructureCompatibilityChecker.CompatibilityResult.Incompatible
import org.oppia.android.scripts.gae.json.AndroidActivityHandlerService
import org.oppia.android.scripts.gae.json.GaeExploration
import org.oppia.android.scripts.gae.json.GaeSkill
import org.oppia.android.scripts.gae.json.GaeStory
import org.oppia.android.scripts.gae.json.GaeSubtopic
import org.oppia.android.scripts.gae.json.GaeSubtopicPage
import org.oppia.android.scripts.gae.json.GaeTopic
import org.oppia.android.scripts.gae.json.VersionedStructure
import org.oppia.android.scripts.gae.proto.LocalizationTracker
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.VALID_LANGUAGE_TYPES
import org.oppia.android.scripts.gae.proto.LocalizationTracker.Companion.resolveLanguageCode
import org.oppia.proto.v1.structure.LanguageType
import org.oppia.proto.v1.structure.SubtopicPageIdDto
import org.oppia.android.scripts.gae.compat.VersionedStructureReference.Exploration as VersionedExploration
import org.oppia.android.scripts.gae.compat.VersionedStructureReference.Skill as VersionedSkill
import org.oppia.android.scripts.gae.compat.VersionedStructureReference.Story as VersionedStory
import org.oppia.android.scripts.gae.compat.VersionedStructureReference.SubtopicPage as VersionedSubtopicPage
import org.oppia.android.scripts.gae.compat.VersionedStructureReference.Topic as VersionedTopic

private typealias GenericStructureReference =
  VersionedStructureReference<out StructureId, out VersionedStructure>
private typealias GenericLoadResult = LoadResult<VersionedStructure>
private typealias VersionStructureMap = MutableMap<GenericStructureReference, GenericLoadResult>

class TopicPackRepository(
  private val androidService: AndroidActivityHandlerService,
  private val coroutineDispatcher: CoroutineDispatcher,
  localizationTracker: LocalizationTracker,
  private val constraints: StructureCompatibilityChecker.CompatibilityConstraints
) {
  private val textCollector by lazy { SubtitledHtmlCollector(localizationTracker) }
  private val compatibilityChecker by lazy {
    StructureCompatibilityChecker(constraints, localizationTracker, textCollector)
  }
  private val cachedStructures = mutableMapOf<StructureId, VersionStructureMap>()

  // TODO: We need to be able to retrieve assets irrespective of schemas...
  fun downloadConstructedCompleteTopicAsync(topicId: String): Deferred<CompleteTopicPack> {
    // TODO:
    // Algorithm: pick the newest transitive closure of a topic & its dependencies such that all
    // structures within the closure are compatible with the app.
    // Per topic:
    // - For a version, verify the topic structure itself is compatible.
    //   - If it isn't, try the previous version.
    //   - If no versions are, shortcircuit: the topic is wholly incompatible.
    // - For a compatible version, collect the transitive closure of structures.
    // - Verify that each structure is compatible.
    // - If any structure is not compatible, try earlier versions one at-a-time until compatibility
    //   is found.
    // - If no version is found compatible, this version of the topic is not compatible. Try a
    //   previous version.
    // - If no version of the topic has a compatible closure, the topic is wholly incompatible.
    return CoroutineScope(coroutineDispatcher).async {
      when (val result = tryCreateCompatiblePack(topicId)) {
        is LoadResult.Pending -> error("Pack result should not be pending for topic: $topicId.")
        is LoadResult.Success -> result.value
        is LoadResult.Failure -> {
          error(
            "Failed to load complete topic pack with ID: $topicId. Encountered failures:" +
              "\n${result.computeFailureString()}."
          )
        }
      }
    }
  }

  private suspend fun tryCreateCompatiblePack(topicId: String): LoadResult<CompleteTopicPack> {
    // Attempt to load a completely internally consistent topic pack for the latest topic version.
    // If that fails, try the next previous version of the topic and continue until either no
    // versions remain or one is found to be able to be loaded.
    val result = tryCreatePackForLatestTrackedTopicVersion(topicId)
    if (result is LoadResult.Failure) {
      val structureId = StructureId.Topic(topicId)
      val structureMap = cachedStructures.getValue(structureId)
      if (structureMap.size > 1) {
        structureMap.invalidateVersion(structureMap.findMostRecent(structureId))
        return tryCreateCompatiblePack(topicId) // Try again for the next version.
      }
    }
    return result // The result either passed, or there are no more topics to try.
  }

  private suspend fun tryCreatePackForLatestTrackedTopicVersion(
    topicId: String
  ): LoadResult<CompleteTopicPack> {
    // TODO:
    // Algorithm:
    // 1. Attempt to create a complete topic. If any constituent structures fail to import, the whole topic is unavailable.
    // 2. Verify cross-structure compatibility. If any structure violates cross-structure consistency, back that structure up 1 version and try (1) again.
    // 3. If at least one structure fails to ever be compatible, the topic isn't supported. Otherwise, it is.

    // First, try to create a complete topic. All structures must be available at at least one
    // version.
    return tryLoadTopic(topicId).transformAsync { gaeTopic ->
      tryLoadPackFragments(gaeTopic).combine(TopicPackFragment::combineWith)
    }.transform(TopicPackFragment::toTopicPack)
  }

  private suspend fun tryLoadPackFragments(
    gaeTopic: GaeTopic
  ): List<LoadResult<TopicPackFragment>> {
    val subtopicsResult = tryLoadSubtopics(gaeTopic.id, gaeTopic.computeContainedSubtopicMap())
    val storiesResult = tryLoadStories(gaeTopic.computeReferencedStoryIds())
    val explorationsResult = storiesResult.transformAsync { storiesPack ->
      tryLoadExplorations(
        expIds = storiesPack.expectedStories.values.flatSet {
          it.computeReferencedExplorationIds()
        }
      )
    }
    return listOf(
      LoadResult.Success(TopicPackFragment(topic = gaeTopic)),
      subtopicsResult,
      storiesResult,
      explorationsResult,
      tryLoadSkillsClosureAsFragment(gaeTopic, subtopicsResult, storiesResult, explorationsResult),
      LoadResult.Success(
        TopicPackFragment(defaultLanguage = gaeTopic.languageCode.resolveLanguageCode())
      )
    )
  }

  private suspend fun tryLoadSubtopics(
    topicId: String,
    subtopics: Map<Int, GaeSubtopic>
  ): LoadResult<TopicPackFragment> {
    return subtopics.keys.map { subtopicIndex ->
      SubtopicPageIdDto.newBuilder().apply {
        this.topicId = topicId
        this.subtopicIndex = subtopicIndex
      }.build()
    }.map { subtopicId ->
      CoroutineScope(coroutineDispatcher).async {
        tryLoadSubtopicPage(
          subtopicId.topicId, subtopicId.subtopicIndex, subtopics.getValue(subtopicId.subtopicIndex)
        ).transform { subtopicId to it }
      }
    }.awaitAll().combine { subtopicPages ->
      TopicPackFragment(subtopicPages = subtopicPages.toMap())
    }
  }

  private suspend fun tryLoadStories(storyIds: Set<String>): LoadResult<TopicPackFragment> {
    return storyIds.map { storyId ->
      CoroutineScope(coroutineDispatcher).async { tryLoadStory(storyId) }
    }.awaitAll().combine { stories ->
      TopicPackFragment(stories = stories.associateBy(GaeStory::id))
    }
  }

  private suspend fun tryLoadExplorations(expIds: Set<String>): LoadResult<TopicPackFragment> {
    return expIds.map { expId ->
      CoroutineScope(coroutineDispatcher).async { tryLoadExploration(expId) }
    }.awaitAll().combine { explorations ->
      TopicPackFragment(explorations = explorations.associateBy { it.exploration.id })
    }
  }

  private suspend fun tryLoadSkillsClosureAsFragment(
    gaeTopic: GaeTopic,
    subtopicsResult: LoadResult<TopicPackFragment>,
    storiesResult: LoadResult<TopicPackFragment>,
    explorationsResult: LoadResult<TopicPackFragment>
  ): LoadResult<TopicPackFragment> {
    // Use the topic & all loaded subtopics/stories/explorations to determine the initial set of
    // skill IDs, then retrieve a complete skills list closure before constructing and returning a
    // topic pack fragment.
    return subtopicsResult.transformAsync { subtopicPagesFragment ->
      storiesResult.transformAsync { storiesFragment ->
        explorationsResult.transformAsync { explorationsFragment ->
          val topicSkillIds = gaeTopic.collectSkillIds()
          val subtopicSkillIds =
            subtopicPagesFragment.expectedSubtopicPages.values.flatSet { it.collectSkillIds() }
          val storySkillIds =
            storiesFragment.expectedStories.values.flatSet { it.collectSkillIds() }
          val expSkillIds =
            explorationsFragment.expectedExplorations.values.flatSet { it.collectSkillIds() }
          val initialSkillIds = topicSkillIds + subtopicSkillIds + storySkillIds + expSkillIds
          tryLoadSkillsClosure(initialSkillIds)
        }
      }
    }.transform { TopicPackFragment(referencedSkills = it.associateBy(GaeSkill::id)) }
  }

  private suspend fun tryLoadSkillsClosure(skillIds: Set<String>): LoadResult<List<GaeSkill>> {
    // Load skills in a loop until all known skills are loaded (since concept cards may themselves
    // reference other skills not referenced elsewhere in a topic).
    return tryLoadSkills(skillIds).transformAsync { skills ->
      val allReferencedSkillIds = skillIds + skills.flatSet { it.collectSkillIds() }
      if (allReferencedSkillIds != skillIds) {
        tryLoadSkillsClosure(allReferencedSkillIds)
      } else LoadResult.Success(skills)
    }
  }

  private suspend fun tryLoadSkills(skillIds: Set<String>): LoadResult<List<GaeSkill>> {
    return skillIds.map { skillId ->
      CoroutineScope(coroutineDispatcher).async { tryLoadSkill(skillId) }
    }.awaitAll().flatten()
  }

  private suspend fun tryLoadTopic(topicId: String): LoadResult<GaeTopic> =
    tryLoadLatestStructure(StructureId.Topic(topicId), ::VersionedTopic).safeCast()

  private suspend fun tryLoadSubtopicPage(
    topicId: String,
    subtopicIndex: Int,
    correspondingGaeSubtopic: GaeSubtopic
  ): LoadResult<GaeSubtopicPage> {
    return tryLoadLatestStructure(StructureId.Subtopic(topicId, subtopicIndex)) { id, version ->
      VersionedSubtopicPage(id, version, correspondingGaeSubtopic)
    }.safeCast()
  }

  private suspend fun tryLoadStory(storyId: String): LoadResult<GaeStory> =
    tryLoadLatestStructure(StructureId.Story(storyId), ::VersionedStory).safeCast()

  private suspend fun tryLoadExploration(expId: String): LoadResult<CompleteExploration> {
    return tryLoadLatestStructure(StructureId.Exploration(expId)) { id, version ->
      VersionedExploration(id, version, coroutineDispatcher, constraints)
    }.safeCast()
  }

  private suspend fun tryLoadSkill(skillId: String): LoadResult<GaeSkill> =
    tryLoadLatestStructure(StructureId.Skill(skillId), ::VersionedSkill).safeCast()

  private suspend fun <S : VersionedStructure, I : StructureId> tryLoadLatestStructure(
    structureId: I,
    createReference: (I, Int) -> VersionedStructureReference<I, S>
  ): GenericLoadResult {
    // Note that these operations aren't atomic, but fetching and checking a structure is idempotent
    // so multiple operations can kick-off and the last result taken for future caching.
    val structureMap = cachedStructures.getOrPut(structureId) {
      // If no version of this structure has been loaded yet, preload the latest version and pending
      // results for all previous versions.
      val versionedRef = createReference(structureId, VersionedStructureReference.INVALID_VERSION)
      val (structure, result) = versionedRef.loadLatest(androidService, compatibilityChecker)
      val latestVersion = versionedRef.toNewVersion(structure.version)
      mutableMapOf<GenericStructureReference, GenericLoadResult>().also { structureMap ->
        structureMap[latestVersion] = result
        for (it in 1 until latestVersion.version) {
          structureMap[versionedRef.toNewVersion(it)] = LoadResult.Pending()
        }
      }
    }

    // Start backwards from the most recent (known) version of the structure until one is found
    // that's at least directly compatible with the import pipeline. No guarantees are made yet
    // about cross-structure compatibility as that's checked later.
    var checkedReference: GenericStructureReference? = structureMap.findMostRecent(structureId)
    var lastInvalidReference: GenericStructureReference? = null
    while (checkedReference != null) {
      val result = tryLoadStructure(structureMap, checkedReference)
      if (lastInvalidReference != null) structureMap.invalidateVersion(lastInvalidReference)
      if (result is LoadResult.Success<*>) return result
      lastInvalidReference = checkedReference // This structure isn't compatible.
      checkedReference = checkedReference.toPreviousVersion()
    }

    // If no versions match, return the failures of the oldest structure (since all others have been
    // eliminated).
    return tryLoadStructure(structureMap, structureMap.findMostRecent(structureId))
  }

  private suspend fun tryLoadStructure(
    versionStructureMap: VersionStructureMap,
    reference: GenericStructureReference
  ): GenericLoadResult {
    return when (val result = versionStructureMap.getValue(reference)) {
      is LoadResult.Pending -> {
        reference.loadVersioned(androidService, compatibilityChecker).also {
          versionStructureMap[reference] = it
        }
      }
      is LoadResult.Success, is LoadResult.Failure -> result
    }
  }

  private inline fun <reified S : VersionedStructure> GenericLoadResult.safeCast(): LoadResult<S> {
    return when (this) {
      is LoadResult.Pending -> LoadResult.Pending()
      is LoadResult.Success -> LoadResult.Success(value as S)
      is LoadResult.Failure -> LoadResult.Failure(failures)
    }
  }

  private fun VersionStructureMap.findMostRecent(
    structureId: StructureId
  ): GenericStructureReference {
    return checkNotNull(keys.maxByOrNull { it.version }) {
      "Failed to find most recent structure reference in map: $this for ID: $structureId."
    }
  }

  private fun VersionStructureMap.invalidateVersion(reference: GenericStructureReference) {
    require(reference == findMostRecent(reference.structureId)) {
      "Can only invalidate the most recent version of a structure."
    }
    check(size > 1) { "Cannot remove the final structure." }
    remove(reference)
  }

  private fun GaeTopic.collectSkillIds(): Set<String> =
    textCollector.collectSubtitles(this).extractSkillIds() + computeDirectlyReferencedSkillIds()

  private fun GaeSubtopicPage.collectSkillIds(): Set<String> =
    textCollector.collectSubtitles(this).extractSkillIds()

  private fun GaeStory.collectSkillIds(): Set<String> =
    textCollector.collectSubtitles(this).extractSkillIds() + computeDirectlyReferencedSkillIds()

  private fun CompleteExploration.collectSkillIds(): Set<String> {
    return textCollector.collectSubtitles(this).extractSkillIds() +
      exploration.computeDirectlyReferencedSkillIds()
  }

  private fun GaeSkill.collectSkillIds(): Set<String> =
    textCollector.collectSubtitles(this).extractSkillIds() + computeDirectlyReferencedSkillIds()

  private data class TopicPackFragment(
    val topic: GaeTopic? = null,
    val subtopicPages: Map<SubtopicPageIdDto, GaeSubtopicPage>? = null,
    val stories: Map<String, GaeStory>? = null,
    val explorations: Map<String, CompleteExploration>? = null,
    val referencedSkills: Map<String, GaeSkill>? = null,
    val defaultLanguage: LanguageType? = null
  ) {
    val expectedTopic by lazy { checkNotNull(topic) { "Topic was not initialized." } }
    val expectedSubtopicPages by lazy {
      checkNotNull(subtopicPages) { "Subtopic pages were not initialized." }
    }
    val expectedStories by lazy { checkNotNull(stories) { "Stories were not initialized." } }
    val expectedExplorations by lazy {
      checkNotNull(explorations) { "Explorations were not initialized." }
    }
    val expectedReferencedSkills by lazy {
      checkNotNull(referencedSkills) { "Skills were not initialized." }
    }
    val expectedDefaultLanguage by lazy {
      checkNotNull(defaultLanguage) { "Default language was not initialized." }
    }

    fun toTopicPack(): CompleteTopicPack {
      return CompleteTopicPack(
        topic = expectedTopic,
        subtopicPages = expectedSubtopicPages,
        stories = expectedStories,
        explorations = expectedExplorations,
        referencedSkills = expectedReferencedSkills,
        defaultLanguage = expectedDefaultLanguage
      )
    }

    fun combineWith(other: TopicPackFragment): TopicPackFragment {
      return copy(
        topic = expectOne(topic, other.topic),
        subtopicPages = expectOne(subtopicPages, other.subtopicPages),
        stories = expectOne(stories, other.stories),
        explorations = expectOne(explorations, other.explorations),
        referencedSkills = expectOne(referencedSkills, other.referencedSkills),
        defaultLanguage = expectOne(defaultLanguage, other.defaultLanguage)
      )
    }

    private companion object {
      private fun <T> expectOne(first: T?, second: T?): T? {
        return when {
          first != null && second == null -> first
          first == null && second != null -> second
          first == null && second == null -> null
          else -> error("Expected to pick one of, not both: $first, $second.")
        }
      }
    }
  }

  private companion object {
    private const val CONCEPT_CARD_TAG = "oppia-noninteractive-skillreview"
    private const val SKILL_ID_ATTRIBUTE_NAME = "skill_id-with-value"
    private val CONCEPT_CARD_PATTERN = "<$CONCEPT_CARD_TAG.+?</$CONCEPT_CARD_TAG>".toRegex()

    private fun <I, O> Iterable<I>.flatSet(transform: (I) -> Set<O>): Set<O> =
      flatMapTo(mutableSetOf(), transform)

    private fun Set<SubtitledHtmlCollector.SubtitledText>.extractSkillIds(): Set<String> =
      map { it.text }.flatSet { it.extractSkillIds() }

    private fun String.extractSkillIds(): Set<String> =
      CONCEPT_CARD_PATTERN.findAll(this).map { it.value.extractSkillId() }.toSet()

    private fun String.extractSkillId(): String =
      substringAfter("$SKILL_ID_ATTRIBUTE_NAME=\"").substringBefore("\"").replace("&amp;quot;", "")
  }
}

private sealed class LoadResult<out T> {
  fun <I, O> combineWith(other: LoadResult<I>, combine: (T, I) -> O): LoadResult<O> {
    return when (this) {
      is Pending -> Pending() // At least one is pending.
      is Success -> when (other) {
        is Pending -> Pending() // At least one is pending.
        is Success -> Success(combine(value, other.value)) // Both are successes.
        is Failure -> Failure(other.failures) // At least one is failing.
      }
      is Failure -> when (other) {
        is Pending -> Pending() // At least one is pending.
        is Success -> Failure(failures) // At least one is failing.
        is Failure -> Failure(failures + other.failures) // Both are failing.
      }
    }
  }

  fun <O> transform(operation: (T) -> O): LoadResult<O> = transformAsync { Success(operation(it)) }

  inline fun <O> transformAsync(operation: (T) -> LoadResult<O>): LoadResult<O> {
    return when (this) {
      is Pending -> Pending()
      is Success -> operation(value)
      is Failure -> Failure(failures)
    }
  }

  // Note that the 'unused' here helps to ensure that all instances of 'Pending' act like a
  // singleton (as though it were an object) without losing its generic type safety.
  data class Pending<out T>(val unused: Int = 0) : LoadResult<T>()

  data class Success<out T>(val value: T) : LoadResult<T>()

  data class Failure<out T>(val failures: List<CompatibilityFailure>) : LoadResult<T>() {
    fun computeFailureString(): String = failures.joinToString(separator = "\n") { "- $it" }
  }

  companion object {
    fun <T> List<LoadResult<T>>.flatten(): LoadResult<List<T>> = combine<T, List<T>> { it }

    fun <I, O> List<LoadResult<I>>.combine(transform: (List<I>) -> O): LoadResult<O> {
      return fold(Success(listOf<I>()) as LoadResult<List<I>>) { ongoing, newValue ->
        ongoing.combineWith(newValue, Collection<I>::plus)
      }.transform(transform)
    }

    fun <T> List<LoadResult<T>>.combine(combine: (T, T) -> T): LoadResult<T> =
      reduce { ongoing, newValue -> ongoing.combineWith(newValue, combine) }
  }
}

private sealed class VersionedStructureReference<I : StructureId, S : VersionedStructure> {
  abstract val structureId: I
  abstract val version: Int

  abstract fun fetchLatestFromRemoteAsync(service: AndroidActivityHandlerService): Deferred<S>

  abstract fun fetchVersionedFromRemoteAsync(service: AndroidActivityHandlerService): Deferred<S>

  abstract fun checkCompatibility(
    checker: StructureCompatibilityChecker,
    structure: S
  ): CompatibilityResult

  abstract fun toNewVersion(newVersion: Int): VersionedStructureReference<I, S>

  fun toPreviousVersion(): VersionedStructureReference<I, S>? =
    (version - 1).takeIf { it > 0 }?.let { toNewVersion(it) }

  suspend fun loadLatest(
    service: AndroidActivityHandlerService,
    checker: StructureCompatibilityChecker
  ): Pair<S, LoadResult<S>> =
    fetchLatestFromRemoteAsync(service).let { it.await() to it.toLoadResult(checker) }

  suspend fun loadVersioned(
    service: AndroidActivityHandlerService,
    checker: StructureCompatibilityChecker
  ): LoadResult<S> = fetchVersionedFromRemoteAsync(service).toLoadResult(checker)

  private suspend fun Deferred<S>.toLoadResult(
    checker: StructureCompatibilityChecker
  ): LoadResult<S> {
    val structure = await()
    return when (val compatibilityResult = checkCompatibility(checker, structure)) {
      Compatible -> LoadResult.Success(structure)
      is Incompatible -> LoadResult.Failure(compatibilityResult.failures)
    }
  }

  data class Topic(
    override val structureId: StructureId.Topic,
    override val version: Int
  ) : VersionedStructureReference<StructureId.Topic, GaeTopic>() {
    override fun fetchLatestFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchLatestTopicAsync(structureId.id)

    override fun fetchVersionedFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchTopicByVersionAsync(structureId.id, version)

    override fun toNewVersion(newVersion: Int) = copy(version = newVersion)

    override fun checkCompatibility(checker: StructureCompatibilityChecker, structure: GaeTopic) =
      checker.isTopicItselfCompatible(structure)
  }

  data class SubtopicPage(
    override val structureId: StructureId.Subtopic,
    override val version: Int,
    val correspondingGaeSubtopic: GaeSubtopic
  ) : VersionedStructureReference<StructureId.Subtopic, GaeSubtopicPage>() {
    override fun fetchLatestFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchLatestRevisionCardAsync(structureId.topicId, structureId.subtopicIndex)

    override fun fetchVersionedFromRemoteAsync(
      service: AndroidActivityHandlerService
    ): Deferred<GaeSubtopicPage> {
      return service.fetchRevisionCardByVersionAsync(
        structureId.topicId, structureId.subtopicIndex, version
      )
    }

    override fun toNewVersion(newVersion: Int) = copy(version = newVersion)

    override fun checkCompatibility(
      checker: StructureCompatibilityChecker,
      structure: GaeSubtopicPage
    ) = checker.isSubtopicPageItselfCompatible(structure, correspondingGaeSubtopic)
  }

  data class Story(
    override val structureId: StructureId.Story,
    override val version: Int
  ) : VersionedStructureReference<StructureId.Story, GaeStory>() {
    override fun fetchLatestFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchLatestStoryAsync(structureId.id)

    override fun fetchVersionedFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchStoryByVersionAsync(structureId.id, version)

    override fun toNewVersion(newVersion: Int) = copy(version = newVersion)

    override fun checkCompatibility(checker: StructureCompatibilityChecker, structure: GaeStory) =
      checker.isStoryItselfCompatible(structure)
  }

  data class Exploration(
    override val structureId: StructureId.Exploration,
    override val version: Int,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val compatibilityConstraints: StructureCompatibilityChecker.CompatibilityConstraints
  ) : VersionedStructureReference<StructureId.Exploration, CompleteExploration>() {
    override fun fetchLatestFromRemoteAsync(
      service: AndroidActivityHandlerService
    ): Deferred<CompleteExploration> {
      return CoroutineScope(coroutineDispatcher).async {
        service.downloadExploration(service.fetchLatestExplorationAsync(structureId.id))
      }
    }

    override fun fetchVersionedFromRemoteAsync(
      service: AndroidActivityHandlerService
    ): Deferred<CompleteExploration> {
      return CoroutineScope(coroutineDispatcher).async {
        service.downloadExploration(service.fetchExplorationByVersionAsync(structureId.id, version))
      }
    }

    override fun toNewVersion(newVersion: Int) = copy(version = newVersion)

    override fun checkCompatibility(
      checker: StructureCompatibilityChecker,
      structure: CompleteExploration
    ) = checker.isExplorationItselfCompatible(structure)

    private suspend fun AndroidActivityHandlerService.downloadExploration(
      gaeExploration: Deferred<GaeExploration>
    ): CompleteExploration {
      val exploration = gaeExploration.await()
      val translations = VALID_LANGUAGE_TYPES.map { languageType ->
        fetchExplorationTranslationsAsync(
          structureId.id, exploration.version, languageType.toContentLanguageCode()
        )
      }.awaitAll()
      return CompleteExploration(
        exploration, translations.associateBy { it.languageCode.resolveLanguageCode() }
      )
    }
  }

  data class Skill(
    override val structureId: StructureId.Skill,
    override val version: Int
  ) : VersionedStructureReference<StructureId.Skill, GaeSkill>() {
    override fun fetchLatestFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchLatestConceptCardAsync(structureId.id)

    override fun fetchVersionedFromRemoteAsync(service: AndroidActivityHandlerService) =
      service.fetchConceptCardByVersionAsync(structureId.id, version)

    override fun toNewVersion(newVersion: Int) = copy(version = newVersion)

    override fun checkCompatibility(checker: StructureCompatibilityChecker, structure: GaeSkill) =
      checker.isSkillItselfCompatible(structure)
  }

  companion object {
    const val INVALID_VERSION = 0

    private fun LanguageType.toContentLanguageCode(): String {
      return when (this) {
        LanguageType.ENGLISH -> "en"
        LanguageType.ARABIC -> "ar"
        LanguageType.HINDI -> "hi"
        LanguageType.HINGLISH -> "hi-en"
        // Note: Oppia web doesn't support pt-br specific content translations yet.
        LanguageType.BRAZILIAN_PORTUGUESE -> "pt"
        LanguageType.SWAHILI -> "sw"
        LanguageType.LANGUAGE_CODE_UNSPECIFIED, LanguageType.UNRECOGNIZED ->
          error("Unsupported language type: $this.")
      }
    }
  }
}

private sealed class StructureId {
  data class Topic(val id: String) : StructureId()

  data class Subtopic(val topicId: String, val subtopicIndex: Int) : StructureId()

  data class Story(val id: String) : StructureId()

  data class Exploration(val id: String) : StructureId()

  data class Skill(val id: String) : StructureId()
}
