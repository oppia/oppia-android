package org.oppia.android.app.story

import android.content.res.Resources
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TypefaceSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.android.databinding.StoryChapterViewBinding
import org.oppia.android.databinding.StoryFragmentBinding
import org.oppia.android.databinding.StoryHeaderViewBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [StoryFragment]. */
class StoryFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String
) {
  private val routeToExplorationListener = activity as RouteToExplorationListener

  private lateinit var binding: StoryFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var linearSmoothScroller: RecyclerView.SmoothScroller

  @Inject
  lateinit var storyViewModel: StoryViewModel

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): View? {
    binding = StoryFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    storyViewModel.setInternalProfileId(internalProfileId)
    storyViewModel.setTopicId(topicId)
    storyViewModel.setStoryId(storyId)
    logStoryActivityEvent(topicId, storyId)

    binding.storyToolbar.setNavigationOnClickListener {
      (activity as StoryActivity).finish()
    }

    binding.storyToolbarTitle.setOnClickListener {
      binding.storyToolbarTitle.isSelected = true
    }

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)
    linearSmoothScroller = createSmoothScroller()

    binding.storyChapterList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    // NB: Both the view model and lifecycle owner must be set in order to correctly bind LiveData elements to
    // data-bound view models.
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = storyViewModel
    }
    return binding.root
  }

  fun handleSelectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    routeToExplorationListener.routeToExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen,
      isCheckpointingEnabled = false
    )
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StoryItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<StoryItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is StoryHeaderViewModel -> ViewType.VIEW_TYPE_HEADER
          is StoryChapterSummaryViewModel -> ViewType.VIEW_TYPE_CHAPTER
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_HEADER,
        inflateDataBinding = StoryHeaderViewBinding::inflate,
        setViewModel = StoryHeaderViewBinding::setViewModel,
        transformViewModel = { it as StoryHeaderViewModel }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_CHAPTER,
        inflateView = { parent ->
          StoryChapterViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<StoryChapterViewBinding>(view)!!
          val storyItemViewModel = viewModel as StoryChapterSummaryViewModel
          binding.viewModel = storyItemViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName,
              entityType,
              storyItemViewModel.storyId,
              imageCenterAlign = true
            ).parseOppiaHtml(
              storyItemViewModel.summary, binding.chapterSummary
            )
          if (storyItemViewModel.chapterSummary.chapterPlayState
            == ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
          ) {
            val missingPrerequisiteSummary = fragment.getString(
              R.string.chapter_prerequisite_title_label,
              storyItemViewModel.index.toString(),
              storyItemViewModel.missingPrerequisiteChapter.name
            )
            val chapterLockedSpannable = SpannableString(missingPrerequisiteSummary)
            val clickableSpan = object : ClickableSpan() {
              override fun onClick(widget: View) {
                smoothScrollToPosition(storyItemViewModel.index - 1)
              }

              override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
              }
            }
            chapterLockedSpannable.setSpan(
              clickableSpan,
              /* start= */ LOCKED_CARD_PREFIX_LENGTH,
              /* end= */ chapterLockedSpannable.length - LOCKED_CARD_SUFFIX_LENGTH,
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            chapterLockedSpannable.setSpan(
              TypefaceSpan("sans-serif-medium"),
              /* start= */ LOCKED_CARD_PREFIX_LENGTH,
              /* end= */ chapterLockedSpannable.length - LOCKED_CARD_SUFFIX_LENGTH,
              Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.htmlContent = chapterLockedSpannable
            binding.chapterSummary.movementMethod = LinkMovementMethod.getInstance()
          }
        }
      )
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_HEADER,
    VIEW_TYPE_CHAPTER
  }

  fun smoothScrollToPosition(position: Int) {
    linearSmoothScroller.targetPosition = position
    linearLayoutManager.startSmoothScroll(linearSmoothScroller)
    binding.storyChapterList.layoutManager = linearLayoutManager
  }

  private fun createSmoothScroller(): RecyclerView.SmoothScroller {
    val milliSecondsPerInch = 100f

    return object : LinearSmoothScroller(activity) {
      override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
      }

      override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        return milliSecondsPerInch / displayMetrics!!.densityDpi
      }

      override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
        return super.calculateDyToMakeVisible(view, snapPreference) + dipToPixels(48)
      }
    }
  }

  private fun dipToPixels(dipValue: Int): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dipValue.toFloat(),
      Resources.getSystem().displayMetrics
    ).toInt()
  }

  private fun logStoryActivityEvent(topicId: String, storyId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      EventLog.EventAction.OPEN_STORY_ACTIVITY,
      oppiaLogger.createStoryContext(topicId, storyId)
    )
  }

  companion object {
    private const val LOCKED_CARD_PREFIX_LENGTH = 9
    private const val LOCKED_CARD_SUFFIX_LENGTH = 24
  }
}
