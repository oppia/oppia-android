package org.oppia.android.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

const val RESET_PIN_PROFILE_ID_ARGUMENT_KEY = "ResetPinDialogFragment.reset_pin_profile_id"
const val RESET_PIN_NAME_ARGUMENT_KEY = "ResetPinDialogFragment.reset_pin_name"

/** Dialog Fragment to input new Pin. */
class ResetPinDialogFragment : InjectableDialogFragment() {
  companion object {
    fun newInstance(profileId: Int, name: String): ResetPinDialogFragment {
      val resetPinDialogFragment = ResetPinDialogFragment()
      val args = Bundle()
      args.putInt(RESET_PIN_PROFILE_ID_ARGUMENT_KEY, profileId)
      args.putString(RESET_PIN_NAME_ARGUMENT_KEY, name)
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

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val profileId = arguments?.getInt(RESET_PIN_PROFILE_ID_ARGUMENT_KEY)
    val name = arguments?.getString(RESET_PIN_NAME_ARGUMENT_KEY)
    checkNotNull(profileId) { "Profile Id must not be null" }
    checkNotNull(name) { "Name must not be null" }
    return resetPinDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      profileId,
      name
    )
  }
}
