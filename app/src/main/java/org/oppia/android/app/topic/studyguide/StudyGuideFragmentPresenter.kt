package org.oppia.android.app.topic.studyguide

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.StudyGuideFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.StudyGuideFragmentArguments
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.studyguide.StudyGuideFragment.Companion.STUDY_GUIDE_FRAGMENT_ARGUMENTS_KEY
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import javax.inject.Inject

/** Presenter for [StudyGuideFragment], sets up bindings from ViewModel. */
@FragmentScope
class StudyGuideFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val studyGuideViewModelFactory: StudyGuideViewModel.Factory,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var profileId: LegacyProfileId

  /** Handles the [Fragment.onAttach] portion of [StudyGuideFragment]'s lifecycle. */
  fun handleAttach(context: Context) {
    fontScaleConfigurationUtil.adjustFontScale(context, retrieveReadingTextSize())
  }

  /** Handles the [Fragment.onCreateView] portion of [StudyGuideFragment]'s lifecycle. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicId: Int,
    profileId: LegacyProfileId,
    subtopicListSize: Int
  ): View? {
    this.profileId = profileId

    val binding =
      StudyGuideFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val viewModel = studyGuideViewModelFactory.create(
      topicId,
      subtopicId,
      profileId,
      subtopicListSize
    )

    logStudyGuideEvent(topicId, subtopicId)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    // TODO(#6104): Populate the section RecyclerView with a BindableAdapter (heading/content rows).

    profileManagementController.getProfile(profileId)
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
