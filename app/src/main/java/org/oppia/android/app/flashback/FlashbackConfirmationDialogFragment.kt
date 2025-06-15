package org.oppia.android.app.flashback

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.app.model.FlashbackConfirmationDialogFragmentArguments

//subha 1.4 final
/** Fragment that displays a dialog for survey exit confirmation. */
class FlashbackConfirmationDialogFragment : InjectableDialogFragment() {

  companion object {
    /** Arguments key for FlashbackConfirmationDialogFragment. */
    const val FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY = "FlashbackConfirmationDialogFragment.arguments"

    /** Returns a new instance of [FlashbackConfirmationDialogFragment]. */
    fun newInstance(stateName: String): FlashbackConfirmationDialogFragment {
      val args = FlashbackConfirmationDialogFragmentArguments.newBuilder().setStateName(stateName).build()
      return FlashbackConfirmationDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  @Inject lateinit var presenter: FlashbackConfirmationDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = arguments?.getProto(
      FlashbackConfirmationDialogFragment.FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      FlashbackConfirmationDialogFragmentArguments.getDefaultInstance()
    )
    val stateName = args?.stateName
    checkNotNull(stateName) { "State name must not be null" }
    return presenter.handleOnCreateDialog(stateName)
  }
}