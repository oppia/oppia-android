package org.oppia.android.app.notice

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Dialog fragment that informs the user of an app deprecation. */
class ForcedAppDeprecationNoticeDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [ForcedAppDeprecationNoticeDialogFragment]. */
    fun newInstance(): ForcedAppDeprecationNoticeDialogFragment {
      return ForcedAppDeprecationNoticeDialogFragment()
    }
  }

  @Inject lateinit var presenter: ForcedAppDeprecationNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return presenter.handleOnCreateDialog()
  }
}
