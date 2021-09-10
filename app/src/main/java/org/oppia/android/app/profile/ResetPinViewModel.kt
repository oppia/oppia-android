package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

private const val DEFAULT_NAME = ""

/** The ViewModel for [ResetPinDialogFragment]. */
@FragmentScope
class ResetPinViewModel @Inject constructor(
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  val inputPin = ObservableField("")
  val errorMessage = ObservableField("")
  val resetPinInputPinHintText: ObservableField<String> =
    ObservableField(computeResetPinInputPinHint(DEFAULT_NAME))

  fun setName(name: String) {
    resetPinInputPinHintText.set(computeResetPinInputPinHint(name))
  }

  private fun computeResetPinInputPinHint(name: String): String {
    return resourceHandler.getStringInLocale(R.string.admin_settings_enter_user_new_pin, name)
  }
}
