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
import org.oppia.app.model.Exploration
import org.oppia.app.model.Hint
import org.oppia.app.model.Interaction
import org.oppia.app.model.Outcome
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.exploration.ExplorationRetriever
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.caching.AssetRepository
import org.oppia.util.caching.TopicListToCache
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.ImageDownloadUrlTemplate
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
  @DefaultGcsPrefix private val gcsPrefix: String,
  @DefaultResourceBucketName private val gcsResource: String,
  @ImageDownloadUrlTemplate private val imageDownloadUrlTemplate: String,
  @TopicListToCache private val topicListToCache: List<String>
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

      // Only download binary assets for one fractions lesson. The others can still be streamed.
      val explorations = loadExplorations(topicListToCache)
      val voiceoverUrls = listOf<String>()
      val imageUrls = collectAllImageUrls(explorations).toSet()
      logger.d(
        "AssetRepo",
        "Downloading up to ${voiceoverUrls.size} voiceovers and ${imageUrls.size} images"
      )
      val startTime = SystemClock.elapsedRealtime()
      val downloadUrls = (voiceoverUrls + imageUrls).filterNot(
        assetRepository::isRemoteBinarAssetDownloaded
      )
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
            primeDownloadStatus.observe(appCompatActivity, object : Observer<PrimeAssetsStatus> {
              override fun onChanged(primeAssetsStatus: PrimeAssetsStatus?) {
                primeAssetsStatus?.let { status ->
                  if (status.totalDownloadCount > 0 && !dialogShown.get()) {
                    showProgressDialog(appCompatActivity, dialogStyleResId)
                  }
                }
              }
            })
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

  private fun loadExplorations(explorationIds: Collection<String>): Collection<Exploration> {
    return explorationIds.map(explorationRetriever::loadExploration)
  }

  @Suppress("unused") // Voiceovers can't be played while offline, so don't cache them for now.
  private fun collectAllDesiredVoiceoverUrls(
    explorations: Collection<Exploration>
  ): Collection<String> {
    return explorations.flatMap(::collectDesiredVoiceoverUrls)
  }

  private fun collectDesiredVoiceoverUrls(exploration: Exploration): Collection<String> {
    return extractDesiredVoiceovers(exploration).map { voiceover ->
      getUriForVoiceover(
        exploration.id,
        voiceover
      )
    }
  }

  private fun extractDesiredVoiceovers(exploration: Exploration): Collection<Voiceover> {
    val states = exploration.statesMap.values
    val mappings = states.flatMap(::getDesiredVoiceoverMapping)
    return mappings.flatMap { it.voiceoverMappingMap.values }
  }

  private fun getDesiredVoiceoverMapping(state: State): Collection<VoiceoverMapping> {
    val voiceoverMappings = state.recordedVoiceoversMap
    val contentIds = extractDesiredContentIds(state).filter(String::isNotEmpty)
    return voiceoverMappings.filterKeys(contentIds::contains).values
  }

  /** Returns all collection IDs from the specified [State] that can actually be played by a user. */
  private fun extractDesiredContentIds(state: State): Collection<String> {
    val stateContentSubtitledHtml = state.content
    val defaultFeedbackSubtitledHtml = state.interaction.defaultOutcome.feedback
    val answerGroupOutcomes = state.interaction.answerGroupsList
      .map(AnswerGroup::getOutcome)
    val answerGroupsSubtitledHtml = answerGroupOutcomes.map(Outcome::getFeedback)
    val targetedSubtitledHtmls =
      answerGroupsSubtitledHtml + stateContentSubtitledHtml + defaultFeedbackSubtitledHtml
    return targetedSubtitledHtmls.map(SubtitledHtml::getContentId)
  }

  private fun collectAllImageUrls(explorations: Collection<Exploration>): Collection<String> {
    return explorations.flatMap(::collectImageUrls)
  }

  private fun collectImageUrls(exploration: Exploration): Collection<String> {
    val subtitledHtmls = collectSubtitledHtmls(exploration)
    val imageSources = subtitledHtmls.flatMap(::getImageSourcesFromHtml)
    return imageSources.toSet().map { imageSource ->
      getUriForImage(exploration.id, imageSource)
    }
  }

  private fun collectSubtitledHtmls(exploration: Exploration): Collection<SubtitledHtml> {
    val states = exploration.statesMap.values
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

  private fun getImageSourcesFromHtml(subtitledHtml: SubtitledHtml): Collection<String> {
    val parsedHtml = parseHtml(replaceCustomOppiaImageTag(subtitledHtml.html))
    val imageSpans = parsedHtml.getSpans(
      0,
      parsedHtml.length,
      ImageSpan::class.java
    )
    return imageSpans.toList().map(ImageSpan::getSource)
  }

  private fun parseHtml(html: String): Spannable {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) as Spannable
  }

  private fun replaceCustomOppiaImageTag(html: String): String {
    return html.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      .replace(CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      .replace("&amp;quot;", "")
  }

  private fun getUriForVoiceover(explorationId: String, voiceover: Voiceover): String {
    return "https://storage.googleapis.com/${gcsResource}/exploration" +
      "/$explorationId/assets/audio/${voiceover.fileName}"
  }

  private fun getUriForImage(explorationId: String, imageFileName: String): String {
    val downloadUrlFile = String.format(
      imageDownloadUrlTemplate, "exploration", explorationId, imageFileName
    )
    return "$gcsPrefix/$gcsResource/$downloadUrlFile"
  }

  private data class PrimeAssetsStatus(
    val currentDownloadCount: Int, val totalDownloadCount: Int, val failedDownloadCount: Int = 0
  )
}
