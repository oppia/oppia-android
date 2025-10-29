package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Dialog fragment shown to prompt the user to save/discard the changes. */
class PendingChangesDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var pendingChangesDialogFragmentPresenter:
    PendingChangesDialogFragmentPresenter

  companion object {
    /** Returns a new instance of [PendingChangesDialogFragment]. */
    fun newInstance(): PendingChangesDialogFragment =
      PendingChangesDialogFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return pendingChangesDialogFragmentPresenter.handleOnCreateDialog()
  }
}
