package org.oppia.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

const val KEY_RESET_PIN_PROFILE_ID = "RESET_PIN_PROFILE_ID"
const val KEY_RESET_PIN_NAME = "RESET_PIN_NAME"

/** Dialog Fragment to input new Pin. */
class ResetPinDialogFragment : InjectableDialogFragment() {
  companion object {
    fun newInstance(profileId: Int, name: String): ResetPinDialogFragment {
      val resetPinDialogFragment = ResetPinDialogFragment()
      val args = Bundle()
      args.putInt(KEY_RESET_PIN_PROFILE_ID, profileId)
      args.putString(KEY_RESET_PIN_NAME, name)
      resetPinDialogFragment.arguments = args
      return resetPinDialogFragment
    }
  }

  @Inject
  lateinit var resetPinDialogFragmentPresenter: ResetPinDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  @ExperimentalCoroutinesApi
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val profileId = arguments?.getInt(KEY_RESET_PIN_PROFILE_ID)
    val name = arguments?.getString(KEY_RESET_PIN_NAME)
    checkNotNull(profileId) { "Profile Id must not be null" }
    checkNotNull(name) { "Name must not be null" }
    return resetPinDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      profileId,
      name
    )
  }
}
