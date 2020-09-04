package org.oppia.domain.topic

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.text.Spannable
import android.text.style.ImageSpan
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.ConceptCard
import org.oppia.app.model.Exploration
import org.oppia.app.model.Hint
import org.oppia.app.model.Interaction
import org.oppia.app.model.Outcome
import org.oppia.app.model.Question
import org.oppia.app.model.RevisionCard
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Subtopic
import org.oppia.app.model.Topic
import org.oppia.domain.exploration.ExplorationRetriever
import org.oppia.domain.question.QuestionRetriever
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.TopicListToCache
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.gcsresource.QuestionResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.ConceptCardHtmlParserEntityType
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.parser.StoryHtmlParserEntityType
import org.oppia.util.parser.ThumbnailDownloadUrlTemplate
import org.oppia.util.parser.TopicHtmlParserEntityType
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

/**
 * Implementation of [PrimeTopicAssetsController] which primes assets & shows UI affordances before
 * and after priming.
 */
@Singleton
class PrimeTopicAssetsControllerImpl @Inject constructor(
  private val context: Context,
  private val logger: ConsoleLogger,
  private val assetRepository: AssetRepository,
  private val topicController: TopicController,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val explorationRetriever: ExplorationRetriever,
  private val questionRetriever: QuestionRetriever,
  private val conceptCardRetriever: ConceptCardRetriever,
  private val revisionCardRetriever: RevisionCardRetriever,
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultResourceBucketName private val gcsResource: String,
  @QuestionResourceBucketName private val questionGcsResource: String,
  @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
  @ThumbnailDownloadUrlTemplate private val thumbnailDownloadUrlTemplate: String,
  @TopicListToCache private val topicListToCache: List<String>,
  @ExplorationHtmlParserEntityType private val explorationEntityType: String,
  @ConceptCardHtmlParserEntityType private val conceptCardEntityType: String,
  @TopicHtmlParserEntityType private val topicEntityType: String,
  @StoryHtmlParserEntityType private val storyEntityType: String
) : PrimeTopicAssetsController {

  // NOTE TO DEVELOPERS: Don't ever do this. The application should use shared dispatchers to
  // control resources & coordinate tests. This custom dispatcher is needed since priming is a
  // dispatcher-intensive operation and using the shared background dispatcher ends up blocking the
  // app UI, potentially in a breaking way.
  private val extraDispatcher = Executors.newFixedThreadPool(
    /* nThreads= */ 4
  ).asCoroutineDispatcher()
  // NOTE TO DEVELOPERS: Never do this. We should never hold activity references in singleton
  // objects, even as weak references. This is being done to keep priming code isolated so that it's
  // easier to remove after #169 is completed.
  private val extraDispatcherScope = CoroutineScope(extraDispatcher)
  private val primeDownloadStatus = MutableLiveData(PrimeAssetsStatus(0, 0))
  private val currentDownloadCount = AtomicInteger()
  private val failedDownloadCount = AtomicInteger()
  private val dialogShown = AtomicBoolean()
  private val dialogDismissed = AtomicBoolean()

  override fun downloadAssets(dialogStyleResId: Int) {
    prepareUiForDownloadStatusChanges(dialogStyleResId)

    // Ensure all JSON files are available in memory for quick retrieval.
    val allFiles = mutableListOf<String>()
    allFiles.add("topics.json")
    val topicIdJsonArray = jsonAssetRetriever
      .loadJsonFromAsset("topics.json")!!
      .getJSONArray("topic_id_list")
    for (i in 0 until topicIdJsonArray.length()) {
      allFiles.addAll(topicController.getAssetFileNameList(topicIdJsonArray.optString(i)))
    }

    val primeAssetJobs = allFiles.map {
      extraDispatcherScope.async {
        assetRepository.primeTextFileFromLocalAssets(it)
      }
    }

    // The following job encapsulates all startup loading. NB: We don't currently wait on this job
    // to complete because it's fine to try to load the assets at the same time as priming the
    // cache, and it's unlikely the user can get into an exploration fast enough to try to load an
    // asset that would trigger a strict mode crash.
    extraDispatcherScope.launch {
      primeAssetJobs.forEach { it.await() }

      // Only download binary assets for configured topics. The others can still be streamed.
      val topics = loadTopics(topicListToCache)
      val explorationIds = topics.flatMap(::extractExplorationIds).toSet()
      val skillIds = topics.flatMap(::extractSkillIds).toSet()

      val explorations = loadExplorations(explorationIds)
      val questions = loadQuestions(skillIds)
      val conceptCards = loadConceptCards(skillIds)
      val revisionCards = loadRevisionCards(topics)

      val thumbnailUrls = topics.flatMap(::collectThumbnailUrls)
      val explorationImageUrls = explorations.flatMap(::collectImageUrls)
      val questionImageUrls = questions.flatMap(::collectImageUrls)
      val conceptCardImageUrls = conceptCards.flatMap(::collectImageUrls)
      val revisionCardImageUrls = revisionCards.flatMap(::collectImageUrls)
      val imageUrls = (
        thumbnailUrls +
          explorationImageUrls +
          questionImageUrls +
          conceptCardImageUrls +
          revisionCardImageUrls
        ).toSet()
      logger.d("AssetRepo", "Downloading up to ${imageUrls.size} images")
      val startTime = SystemClock.elapsedRealtime()
      val downloadUrls = imageUrls.filterNot(assetRepository::isRemoteBinarAssetDownloaded)
      val assetDownloadCount = downloadUrls.size
      primeDownloadStatus.postValue(
        PrimeAssetsStatus(currentDownloadCount.get(), assetDownloadCount)
      )
      downloadUrls.map { url: String ->
        extraDispatcherScope.async {
          try {
            assetRepository.primeRemoteBinaryAsset(url)
          } catch (e: Exception) {
            failedDownloadCount.incrementAndGet()
            logger.w("AssetRepo", "Failed to download $url because $e")
          }
          primeDownloadStatus.postValue(
            PrimeAssetsStatus(
              currentDownloadCount.incrementAndGet(), assetDownloadCount, failedDownloadCount.get()
            )
          )
        }
      }.forEach { it.await() }
      val endTime = SystemClock.elapsedRealtime()
      logger.d(
        "AssetRepo",
        "Finished downloading voiceovers and images in ${endTime - startTime}ms"
      )

      // Send the final count since everything should be done now. This is redundant, but it's meant
      // to make sure the dialog reaches a finalized state.
      primeDownloadStatus.postValue(PrimeAssetsStatus(assetDownloadCount, assetDownloadCount))
    }
  }

  private fun prepareUiForDownloadStatusChanges(dialogStyleResId: Int) {
    // Reference: https://stackoverflow.com/a/37713320.
    val application = context.applicationContext as Application
    application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
      override fun onActivityPaused(activity: Activity?) {}
      override fun onActivityResumed(activity: Activity?) {}
      override fun onActivityStarted(activity: Activity?) {}
      override fun onActivityDestroyed(activity: Activity?) {}
      override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
      override fun onActivityStopped(activity: Activity?) {}
      override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (!dialogDismissed.get()) {
          activity?.let {
            val appCompatActivity = it as AppCompatActivity
            primeDownloadStatus.observe(
              appCompatActivity,
              Observer<PrimeAssetsStatus> { primeAssetsStatus ->
                primeAssetsStatus?.let { status ->
                  if (status.totalDownloadCount > 0 && !dialogShown.get()) {
                    showProgressDialog(appCompatActivity, dialogStyleResId)
                  }
                }
              }
            )
          }
        }
      }
    })
  }

  @SuppressLint("SetTextI18n") // This is a temporary, alpha-release only feature.
  private fun showProgressDialog(activity: Activity, dialogStyleResId: Int) {
    // Programmatically create the layout to avoid resource deps and to keep priming isolated.
    val layout = LinearLayout(activity)
    layout.orientation = LinearLayout.VERTICAL
    val textView = TextView(activity)
    layout.addView(textView)
    textView.text = "Downloading assets for offline support."
    val resources = activity.resources
    val marginPx = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics
    ).toInt()
    (textView.layoutParams as LinearLayout.LayoutParams).setMargins(
      /* left= */ marginPx, /* top= */ marginPx, /* right= */ marginPx, /* bottom= */ marginPx
    )
    textView.textSize = 14f
    textView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    val progressBar = ProgressBar(
      activity, /* attrs= */ null, android.R.attr.progressBarStyleHorizontal
    )
    layout.addView(progressBar)
    (progressBar.layoutParams as LinearLayout.LayoutParams).setMargins(
      /* left= */ marginPx, /* top= */ 0, /* right= */ marginPx, /* bottom= */ 0
    )
    val dialog = AlertDialog.Builder(activity, dialogStyleResId)
      .setView(layout)
      .setPositiveButton("Close") { dialog, _ ->
        dialogDismissed.set(true)
        dialog.dismiss()
      }.create()
    dialog.setCanceledOnTouchOutside(false)
    dialog.show()
    dialogShown.set(true)
    primeDownloadStatus.observeForever(object : Observer<PrimeAssetsStatus> {
      override fun onChanged(status: PrimeAssetsStatus?) {
        status?.let {
          progressBar.max = it.totalDownloadCount
          if (it.currentDownloadCount > progressBar.progress) {
            progressBar.progress = it.currentDownloadCount
          }
          if (it.currentDownloadCount == it.totalDownloadCount) {
            if (it.failedDownloadCount > 0) {
              textView.text = "Finished downloading, but some failed to download. Please try again."
            } else {
              textView.text = "Finished downloading assets for offline support."
            }
            primeDownloadStatus.removeObserver(this)
          }
        }
      }
    })
  }

  private fun loadTopics(topicIds: Collection<String>): Collection<Topic> {
    return topicIds.map(topicController::retrieveTopic)
  }

  private fun loadExplorations(explorationIds: Collection<String>): Collection<Exploration> {
    return explorationIds.map(explorationRetriever::loadExploration)
  }

  private fun loadQuestions(skillIds: Collection<String>): Collection<Question> {
    return questionRetriever.loadQuestions(skillIds.toList())
  }

  private fun loadConceptCards(skillIds: Collection<String>): Collection<ConceptCard> {
    return skillIds.map(conceptCardRetriever::loadConceptCard)
  }

  private fun loadRevisionCards(topics: Collection<Topic>): List<Pair<String, RevisionCard>> {
    return topics.flatMap {
      loadRevisionCards(it.topicId to it.subtopicList.map(Subtopic::getSubtopicId))
    }
  }

  private fun loadRevisionCards(
    topicIdToSubtopicIds: Pair<String, Iterable<Int>>
  ): Collection<Pair<String, RevisionCard>> {
    val topicId = topicIdToSubtopicIds.first
    return topicIdToSubtopicIds.second.map {
      topicId to revisionCardRetriever.loadRevisionCard(topicId, it)
    }
  }

  private fun extractExplorationIds(topic: Topic): List<String> {
    val chapters = topic.storyList.flatMap(StorySummary::getChapterList)
    return chapters.map(ChapterSummary::getExplorationId)
  }

  private fun extractSkillIds(topic: Topic): List<String> {
    return topic.subtopicList.flatMap(Subtopic::getSkillIdsList)
  }

  private fun collectThumbnailUrls(topic: Topic): Collection<String> {
    val thumbnailUrls = mutableListOf<String>()
    val topicThumbnail = topic.topicThumbnail
    if (topicThumbnail.thumbnailFilename.isNotBlank()) {
      thumbnailUrls += getUriForThumbnail(
        topic.topicId, topicEntityType, topicThumbnail.thumbnailFilename
      )
    }

    for (storySummary in topic.storyList) {
      val storyThumbnail = storySummary.storyThumbnail
      if (storyThumbnail.thumbnailFilename.isNotBlank()) {
        thumbnailUrls += getUriForThumbnail(
          storySummary.storyId, storyEntityType, storyThumbnail.thumbnailFilename
        )
      }

      for (chapterSummary in storySummary.chapterList) {
        val chapterThumbnail = chapterSummary.chapterThumbnail
        if (chapterThumbnail.thumbnailFilename.isNotBlank()) {
          thumbnailUrls += getUriForThumbnail(
            storySummary.storyId, storyEntityType, chapterThumbnail.thumbnailFilename
          )
        }
      }
    }

    for (subtopic in topic.subtopicList) {
      val subtopicThumbnail = subtopic.subtopicThumbnail
      if (subtopicThumbnail.thumbnailFilename.isNotBlank()) {
        thumbnailUrls += getUriForThumbnail(
          topic.topicId, topicEntityType, subtopicThumbnail.thumbnailFilename
        )
      }
    }

    return thumbnailUrls
  }

  private fun collectImageUrls(exploration: Exploration): Collection<String> {
    return collectImageUrls(exploration, exploration.id, explorationEntityType, ::getUriForImage) {
      collectSubtitledHtmls(it.statesMap.values)
    }
  }

  private fun collectImageUrls(question: Question): Collection<String> {
    // TODO(#497): Update this to properly link to question assets.
    val skillId = question.linkedSkillIdsList.firstOrNull() ?: ""
    return collectImageUrls(question, skillId, "skill", ::getUriForQuestionImage) {
      collectSubtitledHtmls(listOf(question.questionState))
    }
  }

  private fun collectImageUrls(conceptCard: ConceptCard): Collection<String> {
    return collectImageUrls(
      conceptCard,
      conceptCard.skillId,
      conceptCardEntityType,
      ::getUriForImage,
      ::collectSubtitledHtmls
    )
  }

  private fun collectImageUrls(
    topicIdToRevisionCard: Pair<String, RevisionCard>
  ): Collection<String> {
    return collectImageUrls(
      topicIdToRevisionCard.second,
      topicIdToRevisionCard.first,
      topicEntityType,
      ::getUriForImage,
      ::collectSubtitledHtmls
    )
  }

  private fun <T> collectImageUrls(
    entity: T,
    entityId: String,
    entityType: String,
    computeUriForImage: (String, String, String) -> String,
    collectSubtitledHtmls: (T) -> Collection<SubtitledHtml>
  ): Collection<String> {
    val subtitledHtmls = collectSubtitledHtmls(entity)
    val imageSources = subtitledHtmls.flatMap(::getImageSourcesFromHtml)
    return imageSources.toSet().map { imageSource ->
      computeUriForImage(entityId, entityType, imageSource)
    }
  }

  private fun collectSubtitledHtmls(states: Iterable<State>): Collection<SubtitledHtml> {
    val stateContents = states.map(State::getContent)
    val stateInteractions = states.map(State::getInteraction)

    val stateSolutions =
      stateInteractions.map(Interaction::getSolution).map(Solution::getExplanation)
    val stateHints = stateInteractions.flatMap(
      Interaction::getHintList
    ).map(Hint::getHintContent)

    val answerGroupOutcomes =
      stateInteractions.flatMap(Interaction::getAnswerGroupsList).map(AnswerGroup::getOutcome)
    val defaultOutcomes = stateInteractions.map(Interaction::getDefaultOutcome)
    val outcomeFeedbacks = (answerGroupOutcomes + defaultOutcomes)
      .map(Outcome::getFeedback)

    val allSubtitledHtmls = stateContents + stateSolutions + stateHints + outcomeFeedbacks
    return allSubtitledHtmls.filter { it != SubtitledHtml.getDefaultInstance() }
  }

  private fun collectSubtitledHtmls(conceptCard: ConceptCard): Collection<SubtitledHtml> {
    return conceptCard.workedExampleList + conceptCard.explanation
  }

  private fun collectSubtitledHtmls(revisionCard: RevisionCard): Collection<SubtitledHtml> {
    return listOf(revisionCard.pageContents)
  }

  private fun getImageSourcesFromHtml(subtitledHtml: SubtitledHtml): Collection<String> {
    val parsedHtml = parseHtml(replaceCustomOppiaImageTag(subtitledHtml.html))
    val imageSpans = parsedHtml.getSpans(
      0,
      parsedHtml.length,
      ImageSpan::class.java
    )
    return imageSpans.toList().mapNotNull(ImageSpan::getSource)
  }

  private fun parseHtml(html: String): Spannable {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
  }

  private fun replaceCustomOppiaImageTag(html: String): String {
    return html.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      .replace(CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      .replace("&amp;quot;", "")
  }

  private fun getUriForImage(entityId: String, entityType: String, imageFileName: String): String {
    return computeUrlForImageDownloads(
      imageDownloadUrlTemplate, gcsResource, entityType, entityId, imageFileName
    )
  }

  private fun getUriForQuestionImage(
    entityId: String,
    entityType: String,
    imageFileName: String
  ): String {
    return computeUrlForImageDownloads(
      imageDownloadUrlTemplate, questionGcsResource, entityType, entityId, imageFileName
    )
  }

  private fun getUriForThumbnail(
    entityId: String,
    entityType: String,
    imageFileName: String
  ): String {
    return computeUrlForImageDownloads(
      thumbnailDownloadUrlTemplate, gcsResource, entityType, entityId, imageFileName
    )
  }

  private fun computeUrlForImageDownloads(
    template: String,
    gcsBucket: String,
    entityType: String,
    entityId: String,
    imageFileName: String
  ): String {
    val downloadUrlFile = String.format(template, entityType, entityId, imageFileName)
    return "$gcsPrefix/$gcsBucket/$downloadUrlFile"
  }

  private data class PrimeAssetsStatus(
    val currentDownloadCount: Int,
    val totalDownloadCount: Int,
    val failedDownloadCount: Int = 0
  )
}
