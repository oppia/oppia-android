package org.oppia.android.app.topic.studyguide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.StudyGuideFragmentBinding
import org.oppia.android.app.databinding.databinding.StudyGuideSectionContentBinding
import org.oppia.android.app.databinding.databinding.StudyGuideSectionHeadingBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.StudyGuideFragmentArguments
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.studyguide.StudyGuideFragment.Companion.STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import org.oppia.android.util.profile.toProfileIdPreservingZero
import javax.inject.Inject

/** Presenter for [StudyGuideFragment], sets up bindings from ViewModel. */
@FragmentScope
class StudyGuideFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val studyGuideViewModelFactory: StudyGuideViewModel.Factory,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val profileManagementController: ProfileManagementController
) : HtmlParser.CustomOppiaTagActionListener {
  private lateinit var profileId: LegacyProfileId
  private lateinit var topicId: String

  /** Handles the [Fragment.onAttach] portion of [StudyGuideFragment]'s lifecycle. */
  fun handleAttach(context: Context) {
    fontScaleConfigurationUtil.adjustFontScale(context, retrieveReadingTextSize())
  }

  /** Handles the [Fragment.onCreateView] portion of [StudyGuideFragment]'s lifecycle. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicIndex: Int,
    profileId: LegacyProfileId,
    subtopicListSize: Int
  ): View? {
    this.profileId = profileId
    this.topicId = topicId

    val binding =
      StudyGuideFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val viewModel = studyGuideViewModelFactory.create(
      topicId,
      subtopicIndex,
      profileId,
      subtopicListSize
    )

    logStudyGuideEvent(topicId, subtopicIndex)

    binding.studyGuideSectionRecyclerView.adapter = createRecyclerViewAdapter()

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    profileManagementController.getProfile(profileId.toProfileIdPreservingZero())
      .toLiveData().observe(
        fragment
      ) { result ->
        val readingTextSize = retrieveReadingTextSize()
        if (result is AsyncResult.Success) {
          if (result.value.readingTextSize != readingTextSize) {
            // Since text views are based on sp for sizing, the activity needs to be recreated so that
            // sp can be correctly recomputed.
            fragment.requireActivity().recreate()
          }
        }
      }
    return binding.root
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() {
    ConceptCardFragment.dismissAll(fragment.childFragmentManager)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StudyGuideItemViewModel> {
    return multiTypeBuilderFactory.create<StudyGuideItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is StudyGuideSectionHeadingViewModel -> ViewType.SECTION_HEADING
        is StudyGuideSectionContentViewModel -> ViewType.SECTION_CONTENT
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }
      .registerViewDataBinder(
        viewType = ViewType.SECTION_HEADING,
        inflateDataBinding = StudyGuideSectionHeadingBinding::inflate,
        setViewModel = StudyGuideSectionHeadingBinding::setViewModel,
        transformViewModel = { it as StudyGuideSectionHeadingViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.SECTION_CONTENT,
        inflateDataBinding = StudyGuideSectionContentBinding::inflate,
        setViewModel = this::bindSectionContent,
        transformViewModel = { it as StudyGuideSectionContentViewModel }
      )
      .build()
  }

  private fun bindSectionContent(
    binding: StudyGuideSectionContentBinding,
    contentViewModel: StudyGuideSectionContentViewModel
  ) {
    binding.viewModel = contentViewModel
    binding.studyGuideSectionContentText.text = htmlParserFactory.create(
      resourceBucketName,
      entityType,
      topicId,
      imageCenterAlign = true,
      customOppiaTagActionListener = this,
      displayLocale = appLanguageResourceHandler.getDisplayLocale()
    ).parseOppiaHtml(
      contentViewModel.contentHtml,
      binding.studyGuideSectionContentText,
      supportsLinks = true,
      supportsConceptCards = true
    )
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.bringToFrontOrCreateIfNew(skillId, profileId, fragment.childFragmentManager)
  }

  private enum class ViewType {
    /** Represents the view type for a study guide section's heading. */
    SECTION_HEADING,

    /** Represents the view type for a study guide section's content. */
    SECTION_CONTENT
  }

  private fun logStudyGuideEvent(topicId: String, subTopicId: Int) {
    // Reuses the revision card event since study guides are its successor screen.
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenRevisionCardContext(topicId, subTopicId),
      profileId
    )
  }

  private fun retrieveReadingTextSize(): ReadingTextSize {
    return fragment.requireArguments()
      .getProto(
        STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY,
        StudyGuideFragmentArguments.getDefaultInstance()
      ).readingTextSize
  }
}
