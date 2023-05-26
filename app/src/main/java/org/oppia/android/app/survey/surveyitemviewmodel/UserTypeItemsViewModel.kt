package org.oppia.android.app.survey.surveyitemviewmodel

import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.UserTypeAnswer
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import javax.inject.Inject

/** [SurveyAnswerItemViewModel] for providing the type of user question options. */
class UserTypeItemsViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler,
) : SurveyAnswerItemViewModel(ViewType.USER_TYPE_OPTIONS) {
  val optionItems: ObservableList<MultipleChoiceOptionContentViewModel> = getUserTypeOptions()

  private val selectedItems: MutableList<Int> = mutableListOf()

  override fun updateSelection(itemIndex: Int, isCurrentlySelected: Boolean): Boolean {
    optionItems.forEach { item -> item.isAnswerSelected.set(false) }
    selectedItems.clear()
    selectedItems += itemIndex
    return true
  }

  private fun getUserTypeOptions(): ObservableArrayList<MultipleChoiceOptionContentViewModel> {
    val observableList = ObservableArrayList<MultipleChoiceOptionContentViewModel>()
    observableList += UserTypeAnswer.values()
      .filter { it.isValid() }
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
    return observableList.apply { reverse() }
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
