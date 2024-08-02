package org.oppia.android.app.player.state

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableFastLanguageSwitchingInLesson
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor(
  private val explorationProgressController: ExplorationProgressController,
  private val translationController: TranslationController,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val oppiaLogger: OppiaLogger,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  @EnableFastLanguageSwitchingInLesson
  private val enableFastLanguageSwitchingInLesson: PlatformParameterValue<Boolean>
) : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList()
  val rightItemList: ObservableList<StateItemViewModel> = ObservableArrayList()

  val isSplitView = ObservableField(false)
  val centerGuidelinePercentage = ObservableField(0.5f)

  val isAudioBarVisible = ObservableField(false)

  val isHintBulbVisible = ObservableField(false)
  val isHintOpenedAndUnRevealed = ObservableField(false)

  val hasSupportForSwitchingToSwahili: Boolean = enableFastLanguageSwitchingInLesson.value
  val hasSwahiliTranslations: LiveData<Boolean> by lazy {
    Transformations.map(
      explorationProgressController.getCurrentState().toLiveData(),
      ::processWhetherSwahiliIsSupported
    )
  }
  val hasEnabledSwahiliTranslations: LiveData<Boolean> by lazy {
    Transformations.map(
      translationController.getWrittenTranslationContentLanguage(profileId).toLiveData(),
      ::processIsCurrentLanguageSwahili
    )
  }

  val allowInLessonQuickLanguageSwitching: LiveData<Boolean> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
      ::processAllowInLessonQuickLanguageSwitching
    )
  }

  var currentStateName = ObservableField<String>(null as? String?)

  private val canSubmitAnswer = ObservableField(false)
  private lateinit var profileId: ProfileId

  fun initializeProfile(profileId: ProfileId) {
    this.profileId = profileId
  }

  fun setAudioBarVisibility(audioBarVisible: Boolean) {
    isAudioBarVisible.set(audioBarVisible)
  }

  fun setHintBulbVisibility(hintBulbVisible: Boolean) {
    isHintBulbVisible.set(hintBulbVisible)
  }

  fun setHintOpenedAndUnRevealedVisibility(hintOpenedAndUnRevealedVisible: Boolean) {
    isHintOpenedAndUnRevealed.set(hintOpenedAndUnRevealedVisible)
  }

  fun setCanSubmitAnswer(canSubmitAnswer: Boolean) = this.canSubmitAnswer.set(canSubmitAnswer)

  fun getCanSubmitAnswer(): ObservableField<Boolean> = canSubmitAnswer

  fun getPendingAnswer(
    retrieveAnswerHandler: (List<StateItemViewModel>) -> InteractionAnswerHandler?
  ): UserAnswer? {
    return getPendingAnswerWithoutError(
      retrieveAnswerHandler(
        getAnswerItemList()
      )
    )
  }

  fun canQuicklyToggleBetweenSwahiliAndEnglish(
    hasSwahiliTranslations: Boolean,
    allowInLessonLangSwitching: Boolean
  ): Boolean {
    // This logic has to be done in Kotlin since there seems to be a bug in the generated Java by
    // the databinding compiler that can result in a NPE being thrown in code that shouldn't
    // actually be throwing it (see https://issuetracker.google.com/issues/144246528 for context).
    // Essentially, the following example of generated code results in an NPE unexpectedly:
    //   Boolean value = boolean_value ? Boolean_value : false (Boolean_value can be null)
    return hasSwahiliTranslations && hasSupportForSwitchingToSwahili && allowInLessonLangSwitching
  }

  fun toggleContentLanguage(isSwahiliEnabled: Boolean) {
    val languageSelection = WrittenTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = if (isSwahiliEnabled) OppiaLanguage.ENGLISH else OppiaLanguage.SWAHILI
    }.build()
    val updateResultProvider =
      explorationProgressController.updateWrittenTranslationContentLanguageMidLesson(
        profileId, languageSelection
      )
    val updateResultLiveData = updateResultProvider.toLiveData()
    updateResultLiveData.observe(
      fragment,
      object : Observer<AsyncResult<Any>> {
        override fun onChanged(result: AsyncResult<Any>?) {
          if (result is AsyncResult.Failure) {
            oppiaLogger.e(
              "StateViewModel",
              "Failed to update content language to:" +
                " ${languageSelection.selectedLanguage.ordinal}.",
              result.error
            )
          }
          if (result !is AsyncResult.Pending) {
            updateResultLiveData.removeObserver(this)
          }
        }
      }
    )
  }

  fun getUserAnswerState(
    retrieveAnswerHandler: (List<StateItemViewModel>) -> InteractionAnswerHandler?
  ): UserAnswerState {
    return retrieveAnswerHandler(getAnswerItemList())?.getUserAnswerState()
      ?: UserAnswerState.getDefaultInstance()
  }

  private fun getPendingAnswerWithoutError(
    answerHandler: InteractionAnswerHandler?
  ): UserAnswer? {
    return if (answerHandler?.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME) == null) {
      answerHandler?.getPendingAnswer()
    } else {
      null
    }
  }

  private fun getAnswerItemList(): List<StateItemViewModel> {
    return if (isSplitView.get() == true) {
      rightItemList
    } else {
      itemList
    }
  }

  private fun processIsCurrentLanguageSwahili(languageResult: AsyncResult<OppiaLanguage>): Boolean {
    return when (languageResult) {
      is AsyncResult.Pending -> false
      is AsyncResult.Success -> languageResult.value == OppiaLanguage.SWAHILI
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "StateViewModel", "Failed to retrieve content language.", languageResult.error
        )
        false
      }
    }
  }

  private fun processAllowInLessonQuickLanguageSwitching(
    profileResult: AsyncResult<Profile>
  ): Boolean {
    return when (profileResult) {
      is AsyncResult.Pending -> false // Assume the setting is off until verified.
      is AsyncResult.Success -> profileResult.value.allowInLessonQuickLanguageSwitching
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "StateViewModel",
          "Failed to retrieve profile for current ID: $profileId.",
          profileResult.error
        )
        false // Assume the setting is off since a retrieval error occurred.
      }
    }
  }

  private fun processWhetherSwahiliIsSupported(stateResult: AsyncResult<EphemeralState>): Boolean {
    return when (stateResult) {
      is AsyncResult.Pending -> false
      is AsyncResult.Success -> {
        // It would be nice if there was a domain utility to do this if it's needed elsewhere (or,
        // better yet, just using the language protos directly in the state structure so no raw
        // language codes need to be processed).
        val state = stateResult.value.state
        state.writtenTranslationsMap[state.content.contentId]?.translationMappingMap?.keys?.any {
          // Only enable in-lesson language switching if the main content of a state is available in
          // Swahili.
          machineLocale.run { it.toMachineLowerCase() == "sw" }
        } ?: false
      }
      is AsyncResult.Failure -> {
        oppiaLogger.e("StateViewModel", "Failed to retrieve state.", stateResult.error)
        false
      }
    }
  }
}
