package org.oppia.android.app.survey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.oppia.android.app.model.EphemeralSurveyQuestion
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.survey.surveyitemviewmodel.FreeFormItemsViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.MarketFitItemsViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.NpsItemsViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.SurveyAnswerItemViewModel
import org.oppia.android.app.survey.surveyitemviewmodel.UserTypeItemsViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.SurveyFragmentBinding
import org.oppia.android.databinding.SurveyFreeFormLayoutBinding
import org.oppia.android.databinding.SurveyMarketFitQuestionLayoutBinding
import org.oppia.android.databinding.SurveyNpsScoreLayoutBinding
import org.oppia.android.databinding.SurveyUserTypeQuestionLayoutBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.survey.SurveyProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [SurveyFragment]. */
class SurveyFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val surveyProgressController: SurveyProgressController,
  private val surveyViewModel: SurveyViewModel,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  private val analyticsController: AnalyticsController
) {
  private val ephemeralQuestionLiveData: LiveData<AsyncResult<EphemeralSurveyQuestion>> by lazy {
    surveyProgressController.getCurrentQuestion().toLiveData()
  }

  private lateinit var profileId: ProfileId
  private lateinit var binding: SurveyFragmentBinding
  private lateinit var surveyToolbar: Toolbar
  private lateinit var answerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver
  private lateinit var answerHandler: SelectedAnswerHandler
  private lateinit var questionSelectedAnswer: SurveySelectedAnswer
  private var isCurrentQuestionTerminal: Boolean = false

  /** Sets up data binding. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    explorationId: String,
    topicId: String,
    fragment: SurveyFragment
  ): View? {
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    this.answerAvailabilityReceiver = fragment
    this.answerHandler = fragment

    binding = SurveyFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      viewModel = surveyViewModel
    }

    surveyToolbar = binding.surveyToolbar
    activity.setSupportActionBar(surveyToolbar)
    surveyToolbar.setNavigationOnClickListener {
      val dialogFragment = ExitSurveyConfirmationDialogFragment.newInstance(profileId)
      dialogFragment.showNow(fragment.childFragmentManager, TAG_EXIT_SURVEY_CONFIRMATION_DIALOG)
    }

    binding.surveyAnswersRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.surveyNextButton.setOnClickListener {
      if (::questionSelectedAnswer.isInitialized) {
        surveyProgressController.submitAnswer(questionSelectedAnswer)
      }
    }

    binding.surveyPreviousButton.setOnClickListener {
      surveyProgressController.moveToPreviousQuestion()
    }

    logBeginSurveyEvent(explorationId, topicId, profileId)

    subscribeToCurrentQuestion()

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<SurveyAnswerItemViewModel> {
    return multiTypeBuilderFactory
      .create<SurveyAnswerItemViewModel, SurveyAnswerItemViewModel.ViewType> { viewModel ->
        when (viewModel) {
          is MarketFitItemsViewModel -> {
            SurveyAnswerItemViewModel.ViewType.MARKET_FIT_OPTIONS
          }
          is UserTypeItemsViewModel -> {
            SurveyAnswerItemViewModel.ViewType.USER_TYPE_OPTIONS
          }
          is NpsItemsViewModel -> {
            SurveyAnswerItemViewModel.ViewType.NPS_OPTIONS
          }
          is FreeFormItemsViewModel -> {
            SurveyAnswerItemViewModel.ViewType.FREE_FORM_ANSWER
          }
          else -> {
            throw IllegalStateException("Invalid ViewType")
          }
        }
      }
      .registerViewDataBinder(
        viewType = SurveyAnswerItemViewModel.ViewType.USER_TYPE_OPTIONS,
        inflateDataBinding = SurveyUserTypeQuestionLayoutBinding::inflate,
        setViewModel = SurveyUserTypeQuestionLayoutBinding::setViewModel,
        transformViewModel = { it as UserTypeItemsViewModel }
      )
      .registerViewDataBinder(
        viewType = SurveyAnswerItemViewModel.ViewType.MARKET_FIT_OPTIONS,
        inflateDataBinding = SurveyMarketFitQuestionLayoutBinding::inflate,
        setViewModel = SurveyMarketFitQuestionLayoutBinding::setViewModel,
        transformViewModel = { it as MarketFitItemsViewModel }
      )
      .registerViewBinder(
        viewType = SurveyAnswerItemViewModel.ViewType.NPS_OPTIONS,
        inflateView = { parent ->
          SurveyNpsScoreLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<SurveyNpsScoreLayoutBinding>(view)!!
          val npsViewModel = viewModel as NpsItemsViewModel
          binding.viewModel = npsViewModel

          val flexLayoutManager = FlexboxLayoutManager(activity)
          flexLayoutManager.flexDirection = FlexDirection.ROW
          flexLayoutManager.justifyContent = JustifyContent.CENTER

          binding.surveyNpsButtonsContainer.layoutManager = flexLayoutManager
        }
      )
      .registerViewDataBinder(
        viewType = SurveyAnswerItemViewModel.ViewType.FREE_FORM_ANSWER,
        inflateDataBinding = SurveyFreeFormLayoutBinding::inflate,
        setViewModel = SurveyFreeFormLayoutBinding::setViewModel,
        transformViewModel = { it as FreeFormItemsViewModel }
      )
      .build()
  }

  private fun subscribeToCurrentQuestion() {
    ephemeralQuestionLiveData.observe(
      fragment.viewLifecycleOwner
    ) {
      processEphemeralQuestionResult(it)
    }
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralSurveyQuestion>) {
    when (result) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "SurveyFragment", "Failed to retrieve ephemeral question", result.error
        )
      }
      is AsyncResult.Pending -> {} // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralQuestion(result.value)
    }
  }

  private fun processEphemeralQuestion(ephemeralQuestion: EphemeralSurveyQuestion) {
    val questionName = ephemeralQuestion.question.questionName
    surveyViewModel.itemList.clear()
    when (questionName) {
      SurveyQuestionName.USER_TYPE -> surveyViewModel.itemList.add(
        UserTypeItemsViewModel(
          resourceHandler,
          answerAvailabilityReceiver,
          answerHandler
        )
      )
      SurveyQuestionName.MARKET_FIT -> surveyViewModel.itemList.add(
        MarketFitItemsViewModel(
          resourceHandler,
          answerAvailabilityReceiver,
          answerHandler
        )
      )
      SurveyQuestionName.NPS -> surveyViewModel.itemList.add(
        NpsItemsViewModel(
          answerAvailabilityReceiver,
          answerHandler
        )
      )
      SurveyQuestionName.PROMOTER_FEEDBACK -> surveyViewModel.itemList.add(
        FreeFormItemsViewModel(
          answerAvailabilityReceiver,
          questionName,
          answerHandler
        )
      )
      SurveyQuestionName.PASSIVE_FEEDBACK -> surveyViewModel.itemList.add(
        FreeFormItemsViewModel(
          answerAvailabilityReceiver,
          questionName,
          answerHandler
        )
      )
      SurveyQuestionName.DETRACTOR_FEEDBACK -> surveyViewModel.itemList.add(
        FreeFormItemsViewModel(
          answerAvailabilityReceiver,
          questionName,
          answerHandler
        )
      )
      else -> {}
    }

    this.isCurrentQuestionTerminal = ephemeralQuestion.terminalQuestion
    updateProgress(ephemeralQuestion.currentQuestionIndex, ephemeralQuestion.totalQuestionCount)
    updateQuestionText(questionName)

    if (ephemeralQuestion.selectedAnswer != SurveySelectedAnswer.getDefaultInstance()) {
      surveyViewModel.retrievePreviousAnswer(
        ephemeralQuestion.selectedAnswer,
        ::getPreviousAnswerHandler
      )
    }
  }

  private fun getPreviousAnswerHandler(
    itemList: List<SurveyAnswerItemViewModel>
  ): PreviousAnswerHandler? {
    return itemList.findLast { it is PreviousAnswerHandler } as? PreviousAnswerHandler
  }

  private fun updateProgress(currentQuestionIndex: Int, questionCount: Int) {
    surveyViewModel.updateQuestionProgress(
      progressPercentage = (((currentQuestionIndex + 1) / questionCount.toDouble()) * 100).toInt()
    )
    toggleNavigationButtonVisibility(currentQuestionIndex, questionCount)
  }

  private fun updateQuestionText(questionName: SurveyQuestionName) {
    surveyViewModel.updateQuestionText(questionName)
  }

  /**
   * Updates whether the 'next' button should be active based on whether an answer to the current
   * question has been provided.
   */
  fun updateNextButton(inputAnswerAvailable: Boolean) {
    surveyViewModel.setCanMoveToNextQuestion(inputAnswerAvailable)
  }

  /** Retrieves the answer that was selected by the user for a question. */
  fun getPendingAnswer(answer: SurveySelectedAnswer) {
    this.questionSelectedAnswer = answer
  }

  /**
   * Retrieves and submits the free text answer that was provided by the user, then navigates to the
   * final screen.
   */
  fun submitFreeFormAnswer(answer: SurveySelectedAnswer) {
    hideKeyboard()
    surveyProgressController.submitAnswer(answer).toLiveData().observe(
      fragment,
      { result ->
        when (result) {
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "SurveyFragment", "Failed to submit free form answer", result.error
            )
          }
          is AsyncResult.Pending -> {} // Do nothing until a valid result is available.
          is AsyncResult.Success -> {
            val dialogFragment = SurveyOutroDialogFragment.newInstance()
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction
              .add(dialogFragment, TAG_SURVEY_OUTRO_DIALOG)
              .commit()
            activity.supportFragmentManager.executePendingTransactions()
          }
        }
      }
    )
  }

  private fun toggleNavigationButtonVisibility(questionIndex: Int, questionCount: Int) {
    when (questionIndex) {
      0 -> {
        binding.surveyNextButton.visibility = View.VISIBLE
        binding.surveyPreviousButton.visibility = View.GONE
      }
      (questionCount - 1) -> {
        binding.surveyNextButton.visibility = View.GONE
        binding.surveyPreviousButton.visibility = View.VISIBLE
      }
      else -> {
        binding.surveyNextButton.visibility = View.VISIBLE
        binding.surveyPreviousButton.visibility = View.VISIBLE
      }
    }
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.requireView().windowToken,
      0 // Flag value to force hide the keyboard when possible.
    )
  }

  private fun logBeginSurveyEvent(
    explorationId: String,
    topicId: String,
    profileId: ProfileId
  ) {
    analyticsController.logImportantEvent(
      oppiaLogger.createBeginSurveyContext(
        explorationId,
        topicId
      ),
      profileId = profileId
    )
  }
}
