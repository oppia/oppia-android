package org.oppia.android.app.topic.conceptcard

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.oppia.android.app.databinding.databinding.ConceptCardFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ConceptCardHtmlParserEntityType
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.WorkedExampleLabels
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.EnableWorkedExamples
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Presenter for [ConceptCardFragment], sets up bindings from ViewModel. */
@FragmentScope
class ConceptCardFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val htmlParserFactory: HtmlParser.Factory,
  @ConceptCardHtmlParserEntityType private val entityType: String,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val conceptCardViewModel: ConceptCardViewModel,
  private val translationController: TranslationController,
  private val appLanguageResourceHandler: AppLanguageResourceHandler,
  @EnableWorkedExamples
  private val enableWorkedExamples: PlatformParameterValue<Boolean>,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) : HtmlParser.CustomOppiaTagActionListener {
  private lateinit var profileId: LegacyProfileId
  private lateinit var toolbar: Toolbar

  /**
   * Sets up data binding and toolbar.
   * Host activity must inherit ConceptCardListener to dismiss this fragment.
   */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    skillId: String,
    profileId: LegacyProfileId
  ): View? {
    this.profileId = profileId
    val binding = ConceptCardFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    val view = binding.conceptCardExplanationText

    conceptCardViewModel.initialize(skillId, profileId)
    logConceptCardEvent(skillId)

    toolbar = binding.conceptCardToolbar
    toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    toolbar.setNavigationContentDescription(
      R.string.navigate_up
    )
    toolbar.setNavigationOnClickListener {
      (fragment.requireActivity() as? ConceptCardListener)?.dismissConceptCard()
    }

    binding.let {
      it.viewModel = conceptCardViewModel
      it.lifecycleOwner = fragment
    }

    conceptCardViewModel.conceptCardLiveData.observe(
      fragment
    ) { ephemeralConceptCard ->
      val explanationHtml =
        translationController.extractString(
          ephemeralConceptCard.conceptCard.explanation,
          ephemeralConceptCard.writtenTranslationContext
        )
      view.text =
        htmlParserFactory.create(
          resourceBucketName,
          entityType,
          skillId,
          customOppiaTagActionListener = this,
          imageCenterAlign = true,
          displayLocale = appLanguageResourceHandler.getDisplayLocale()
        ).parseOppiaHtml(
          explanationHtml,
          view,
          supportsLinks = true,
          supportsConceptCards = true,
          workedExampleLabels = retrieveWorkedExampleLabels()
        )
    }

    return binding.root
  }

  /**
   * Applies edge-to-edge window insets to the concept card's [dialog].
   *
   * The concept card is shown in its own window, so the insets applied to the host activity don't
   * reach it and its toolbar would otherwise be drawn underneath the status bar.
   */
  fun applyEdgeToEdgeInsets(dialog: Dialog) {
    if (!enableEdgeToEdge.value) return
    EdgeToEdgeHelper.applyToDialogTopBar(
      dialog,
      toolbar,
      R.color.component_color_shared_fragment_status_bar_color
    )
  }

  /**
   * Returns the labels to display with worked examples, or null if the worked examples feature is
   * disabled (in which case worked examples aren't displayed at all).
   */
  private fun retrieveWorkedExampleLabels(): WorkedExampleLabels? {
    return if (enableWorkedExamples.value) {
      WorkedExampleLabels(
        questionLabel =
          appLanguageResourceHandler.getStringInLocale(R.string.worked_example_question_label),
        answerLabel =
          appLanguageResourceHandler.getStringInLocale(R.string.worked_example_answer_label)
      )
    } else null
  }

  private fun logConceptCardEvent(skillId: String) {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenConceptCardContext(skillId), profileId
    )
  }

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment
      .bringToFrontOrCreateIfNew(skillId, profileId, fragment.parentFragmentManager)
  }
}
