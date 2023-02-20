package org.oppia.android.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getStringFromBundle
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.extractCurrentUserProfileId
import javax.inject.Inject

const val RESET_PIN_NAME_ARGUMENT_KEY = "ResetPinDialogFragment.reset_pin_name"

/** Dialog Fragment to input new Pin. */
class ResetPinDialogFragment : InjectableDialogFragment() {
  companion object {
    fun newInstance(profileId: ProfileId, name: String): ResetPinDialogFragment {
      val resetPinDialogFragment = ResetPinDialogFragment()
      val args = Bundle()
      args.decorateWithUserProfileId(profileId)
      args.putString(RESET_PIN_NAME_ARGUMENT_KEY, name)
      resetPinDialogFragment.arguments = args
      return resetPinDialogFragment
    }
  }

  @Inject
  lateinit var resetPinDialogFragmentPresenter: ResetPinDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val profileId = arguments?.extractCurrentUserProfileId()
    val name = arguments?.getStringFromBundle(RESET_PIN_NAME_ARGUMENT_KEY)
    checkNotNull(profileId) { "Profile Id must not be null" }
    checkNotNull(name) { "Name must not be null" }
    return resetPinDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      profileId,
      name
    )
  }
}
