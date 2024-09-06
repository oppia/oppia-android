package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.SurveySelectedAnswer
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.app.survey.PreviousAnswerHandler
import org.oppia.android.app.survey.SelectedAnswerAvailabilityReceiver
import org.oppia.android.app.survey.SelectedAnswerHandler
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import javax.inject.Inject
import org.oppia.android.util.enumfilter.filterByEnumCondition

/** [SurveyAnswerItemViewModel] for providing the type of user question options. */
class UserTypeItemsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
  private val selectedAnswerAvailabilityReceiver: SelectedAnswerAvailabilityReceiver,
  private val answerHandler: SelectedAnswerHandler
) : SurveyAnswerItemViewModel(ViewType.USER_TYPE_OPTIONS), PreviousAnswerHandler {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getUserTypeOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    if (!selectedItems.contains(itemIndex)) {
      selectedItems.clear()
      selectedItems += itemIndex
    } else {
      selectedItems.clear()
    }

    updateIsAnswerAvailable()

    if (selectedItems.isNotEmpty()) {
      getPendingAnswer(itemIndex)
    }

    return selectedItems.isNotEmpty()
  }

  val isAnswerAvailable = ObservableField(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          selectedAnswerAvailabilityReceiver.onPendingAnswerAvailabilityCheck(
            selectedItems.isNotEmpty()
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  private fun updateIsAnswerAvailable() {
    val selectedItemListWasEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != selectedItemListWasEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getPendingAnswer(itemIndex: Int) {
    val typeCase = itemIndex + 1
    val answerValue = UserTypeAnswer.forNumber(typeCase)
    val answer = SurveySelectedAnswer.newBuilder()
      .setQuestionName(SurveyQuestionName.USER_TYPE)
      .setUserType(answerValue)
      .build()
    answerHandler.getMultipleChoiceAnswer(answer)
  }

  override fun getPreviousAnswer(): SurveySelectedAnswer {
    return SurveySelectedAnswer.getDefaultInstance()
  }

  override fun restorePreviousAnswer(previousAnswer: SurveySelectedAnswer) {
    // Index 0 corresponds to ANSWER_UNSPECIFIED which is not a valid option so it's filtered out.
    // Valid enum type numbers start from 1 while list item indices start from 0, hence the minus(1)
    // to get the correct index to update. Notice that for [getPendingAnswer] we increment the index
    // to get the correct typeCase to save.
    val previousSelection = previousAnswer.userType.number.takeIf { it != 0 }?.minus(1)

    selectedItems.apply {
      clear()
      previousSelection?.let { optionIndex ->
        add(optionIndex)
        updateIsAnswerAvailable()
        getPendingAnswer(optionIndex)
        optionItems[optionIndex].isAnswerSelected.set(true)
      }
    }
  }

  private fun getUserTypeOptions(): ObservableArrayList<MultipleChoiceOptionContentViewModel> {
    val observableList=ObservableArrayList<MultipleChoiceOptionContentViewModel>()
    val filteredUserTypes=filterByEnumCondition(
      UserTypeAnswer.values().toList(),
      {it},
      {it.isValid()}
    )
    observableList+=filteredUserTypes

      .mapIndexed { index, userTypeOption ->
        when (userTypeOption) {
          UserTypeAnswer.LEARNER ->
            MultipleChoiceOptionContentViewModel(
              resourceHandler.getStringInLocale(
                R.string.user_type_answer_learner
              ),
              index,
              this
            )
          UserTypeAnswer.TEACHER -> MultipleChoiceOptionContentViewModel(
            resourceHandler.getStringInLocale(
              R.string.user_type_answer_teacher
            ),
            index,
            this
          )

          UserTypeAnswer.PARENT ->
            MultipleChoiceOptionContentViewModel(
              resourceHandler.getStringInLocale(
                R.string.user_type_answer_parent
              ),
              index,
              this
            )

          UserTypeAnswer.OTHER ->
            MultipleChoiceOptionContentViewModel(
              resourceHandler.getStringInLocale(
                R.string.user_type_answer_other
              ),
              index,
              this
            )
          else -> throw IllegalStateException("Invalid UserTypeAnswer")
        }
      }
    return observableList
  }

  companion object {

    /** Returns whether a [UserTypeAnswer] is valid. */
    fun UserTypeAnswer.isValid(): Boolean {
      return when (this) {
        UserTypeAnswer.UNRECOGNIZED, UserTypeAnswer.USER_TYPE_UNSPECIFIED -> false
        else -> true
      }
    }
  }
}
