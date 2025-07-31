package org.oppia.android.app.flashback

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.FlashbackConfirmationDialogFragmentArguments
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that displays a confirmation dialog for viewing flashback. */
class FlashbackConfirmationDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var flashbackConfirmationDialogFragmentPresenter:
    FlashbackConfirmationDialogFragmentPresenter

  companion object {
    /** Arguments key for FlashbackConfirmationDialogFragment. */
    const val FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY =
      "FlashbackConfirmationDialogFragment.arguments"

    /** Returns a new instance of [FlashbackConfirmationDialogFragment]. */
    fun newInstance(
      stateName: String,
      isFlashbackViewed: Boolean
    ): FlashbackConfirmationDialogFragment {
      val args = FlashbackConfirmationDialogFragmentArguments.newBuilder()
        .setStateName(stateName)
        .setIsFlashbackViewed(isFlashbackViewed)
        .build()
      return FlashbackConfirmationDialogFragment().apply {
        arguments = Bundle().apply {
          putProto(FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val args = arguments?.getProto(
      FLASHBACK_CONFIRMATION_DIALOG_FRAGMENT_ARGUMENTS_KEY,
      FlashbackConfirmationDialogFragmentArguments.getDefaultInstance()
    )
    val stateName = args?.stateName
    checkNotNull(stateName) { "State name must not be null" }
    val isFlashbackViewed = args.isFlashbackViewed
    return flashbackConfirmationDialogFragmentPresenter
      .handleOnCreateDialog(stateName, isFlashbackViewed)
  }
}
