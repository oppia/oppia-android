package org.oppia.android.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ConceptCardFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ConceptCardHtmlParserEntityType
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel */
@FragmentScope
class ConceptCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val htmlParserFactory: HtmlParser.Factory,
  @ConceptCardHtmlParserEntityType private val entityType: String,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>,
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  private val factory: ConceptCardFragment.Factory
) {
  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    skillId: String,
    profileId: ProfileId
  ): View? {
    val binding = ConceptCardFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val view = binding.conceptCardExplanationText
    val viewModel = getConceptCardViewModel()

    viewModel.initialize(skillId, profileId)
    logConceptCardEvent(skillId)

    binding.conceptCardToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    binding.conceptCardToolbar.setNavigationContentDescription(
      R.string.concept_card_close_icon_description
    )
    binding.conceptCardToolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? ConceptCardListener)?.dismissConceptCard()
    }

    binding.let {
      it.viewModel = viewModel
      it.lifecycleOwner = fragment
    }

    viewModel.conceptCardLiveData.observe(
      fragment,
      { ephemeralConceptCard ->
        val explanationHtml =
          translationController.extractString(
            ephemeralConceptCard.conceptCard.explanation,
            ephemeralConceptCard.writtenTranslationContext
          )
        view.text = htmlParserFactory
          .create(
            resourceBucketName,
            entityType,
            skillId,
            imageCenterAlign = true,
            displayLocale = appLanguageResourceHandler.getDisplayLocale()
          )
          .parseOppiaHtml(
            explanationHtml, view
          )
      }
    )

    return binding.root
  }

  /**
   * handles operation when when onDestroy is called.
   */
  fun handleOnDestroy() {
    factory.handleStackWhenCardDestroy()
  }

  private fun getConceptCardViewModel(): ConceptCardViewModel {
    return viewModelProvider.getForFragment(fragment, ConceptCardViewModel::class.java)
  }

  private fun logConceptCardEvent(skillId: String) {
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenConceptCardContext(skillId))
  }
}
