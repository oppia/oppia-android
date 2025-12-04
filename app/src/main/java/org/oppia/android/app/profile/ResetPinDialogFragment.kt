package org.oppia.android.app.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.ResetPinDialogFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Dialog Fragment to input new Pin. */
class ResetPinDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Arguments key for ResetPinDialogFragment. */
    const val RESET_PIN_DIALOG_FRAGMENT_ARGUMENTS_KEY = "ResetPinDialogFragment.arguments"
    fun newInstance(profileId: Int, name: String): ResetPinDialogFragment {
      val args = ResetPinDialogFragmentArguments.newBuilder().apply {
        this.internalProfileId = profileId
        this.name = name
      }.build()
      return ResetPinDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(
            RESET_PIN_DIALOG_FRAGMENT_ARGUMENTS_KEY,
            args
          )
        }
      }
    }
  }

  @Inject
  lateinit var resetPinDialogFragmentPresenter: ResetPinDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = arguments?.getProto(
      RESET_PIN_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      ResetPinDialogFragmentArguments.getDefaultInstance()
    )

    val profileId = args?.internalProfileId
    val name = args?.name
    checkNotNull(profileId) { "Profile Id must not be null" }
    checkNotNull(name) { "Name must not be null" }
    return resetPinDialogFragmentPresenter.handleOnCreateDialog(
      activity as ProfileRouteDialogInterface,
      profileId,
      name
    )
  }
}
