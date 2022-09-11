package org.oppia.android.app.topic.revisioncard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment.Companion.CONCEPT_CARD_DIALOG_FRAGMENT_TAG
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.RevisionCardFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType
import javax.inject.Inject

/** Presenter for [RevisionCardFragment], sets up bindings from ViewModel. */
@FragmentScope
class RevisionCardFragmentPresenter @Inject constructor(
  activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String,
  @TopicHtmlParserEntityType private val entityType: String,
  private val viewModelProvider: ViewModelProvider<RevisionCardViewModel>,
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler
) : HtmlParser.CustomOppiaTagActionListener {
  private lateinit var profileId: ProfileId
  private val routeToReviewListener = activity as RouteToRevisionCardListener

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    topicId: String,
    subtopicId: Int,
    profileId: ProfileId
  ): View? {
    this.profileId = profileId

    val binding =
      RevisionCardFragmentBinding.inflate(
        inflater,
        container,
        /* attachToRoot= */ false
      )
    val view = binding.revisionCardExplanationText
    val viewModel = getReviewCardViewModel()

    viewModel.initialize(topicId, subtopicId, profileId)
    logRevisionCardEvent(topicId, subtopicId)

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    setUpRevisionNavigationCards(binding, topicId, subtopicId)

    viewModel.revisionCardLiveData.observe(
      fragment,
      { ephemeralRevisionCard ->
        val pageContentsHtml =
          translationController.extractString(
            ephemeralRevisionCard.revisionCard.pageContents,
            ephemeralRevisionCard.writtenTranslationContext
          )
        view.text = htmlParserFactory.create(
          resourceBucketName, entityType, topicId, imageCenterAlign = true,
          customOppiaTagActionListener = this,
          displayLocale = appLanguageResourceHandler.getDisplayLocale()
        ).parseOppiaHtml(
          pageContentsHtml, view, supportsLinks = true, supportsConceptCards = true
        )
      }
    )

    return binding.root
  }

  private fun setUpRevisionNavigationCards(
    binding: RevisionCardFragmentBinding,
    topicId: String,
    subtopicId: Int
  ) {
    binding.nextSubtopicImageView.setEntityType(entityType)
    binding.prevSubtopicImageView.setEntityType(entityType)

    binding.previousNavigationCard.setOnClickListener {
      routeToReviewListener.routeToRevisionCard(profileId.internalId, topicId, subtopicId - 1)
    }

    binding.nextNavigationCard.setOnClickListener {
      routeToReviewListener.routeToRevisionCard(profileId.internalId, topicId, subtopicId + 1)
    }

    getReviewCardViewModel().nextSubtopicLiveData.observe(fragment) { nextSubtopic ->
      if (nextSubtopic == EphemeralSubtopic.getDefaultInstance()) {
        binding.nextNavigationCard.visibility = View.INVISIBLE
      }
      binding.nextSubtopicImageView.setLessonThumbnail(nextSubtopic.subtopic.subtopicThumbnail)
      binding.nextSubtopicTitle.text = translationController.extractString(
        nextSubtopic.subtopic.title,
        nextSubtopic.writtenTranslationContext
      )
    }

    getReviewCardViewModel().previousSubtopicLiveData.observe(fragment) { previousSubtopic ->
      if (previousSubtopic.equals(EphemeralSubtopic.getDefaultInstance())) {
        binding.previousNavigationCard.visibility = View.INVISIBLE
      }
      binding.prevSubtopicImageView.setLessonThumbnail(previousSubtopic.subtopic.subtopicThumbnail)
      binding.prevSubtopicTitle.text = translationController.extractString(
        previousSubtopic.subtopic.title,
        previousSubtopic.writtenTranslationContext
      )
    }
  }

  /** Dismisses the concept card fragment if it's currently active in this fragment. */
  fun dismissConceptCard() {
    fragment.childFragmentManager.findFragmentByTag(
      CONCEPT_CARD_DIALOG_FRAGMENT_TAG
    )?.let { dialogFragment ->
      fragment.childFragmentManager.beginTransaction().remove(dialogFragment).commitNow()
    }
  }

  private fun getReviewCardViewModel(): RevisionCardViewModel {
    return viewModelProvider.getForFragment(fragment, RevisionCardViewModel::class.java)
  }

  private fun logRevisionCardEvent(topicId: String, subTopicId: Int) {
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenRevisionCardContext(topicId, subTopicId))
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment
      .newInstance(skillId, profileId)
      .showNow(fragment.childFragmentManager, CONCEPT_CARD_DIALOG_FRAGMENT_TAG)
  }
}
