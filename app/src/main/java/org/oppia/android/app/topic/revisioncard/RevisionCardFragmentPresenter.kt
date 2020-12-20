package org.oppia.android.app.topic.revisioncard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.RevisionCardFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.HtmlParser
import org.oppia.android.util.parser.TopicHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** Presenter for [RevisionCardFragment], sets up bindings from ViewModel. */
@FragmentScope
class RevisionCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String,
  private val viewModelProvider: ViewModelProvider<RevisionCardViewModel>
) {

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicId: Int
  ): View? {
    val binding =
      RevisionCardFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val view = binding.revisionCardExplanationText
    val viewModel = getReviewCardViewModel()

    viewModel.setTopicAndSubtopicId(topicId, subtopicId)
    logRevisionCardEvent(topicId, subtopicId)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    viewModel.revisionCardLiveData.observe(
      fragment,
      Observer {
        view.text = htmlParserFactory.create(
          resourceBucketName, entityType, topicId, imageCenterAlign = true
        ).parseOppiaHtml(it.pageContents.html, view)
      }
    )

    return binding.root
  }

  private fun getReviewCardViewModel(): RevisionCardViewModel {
    return viewModelProvider.getForFragment(fragment, RevisionCardViewModel::class.java)
  }

  private fun logRevisionCardEvent(topicId: String, subTopicId: Int) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_REVISION_CARD,
      oppiaLogger.createRevisionCardContext(topicId, subTopicId)
    )
  }
}
