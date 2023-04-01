package org.oppia.android.app.notice

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/**
 * Dialog fragment to be shown when the user may be unaware that they're using a beta pre-release
 * version of the app.
 */
class BetaNoticeDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [BetaNoticeDialogFragment]. */
    fun newInstance(): BetaNoticeDialogFragment = BetaNoticeDialogFragment()
  }

  @Inject lateinit var presenter: BetaNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return presenter.handleOnCreateDialog()
  }
}
