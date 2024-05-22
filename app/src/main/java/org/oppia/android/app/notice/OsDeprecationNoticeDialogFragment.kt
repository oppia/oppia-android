package org.oppia.android.app.notice

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/**
 * Dialog fragment that informs the user that their phone OS is no longer supported by Oppia and
 * they will no longer be able to update their app to the latest version.
 */
class OsDeprecationNoticeDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [OsDeprecationNoticeDialogFragment]. */
    fun newInstance(): OsDeprecationNoticeDialogFragment {
      return OsDeprecationNoticeDialogFragment()
    }
  }

  @Inject lateinit var osDeprecationNoticeDialogFragmentPresenter:
    OsDeprecationNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return osDeprecationNoticeDialogFragmentPresenter.handleOnCreateDialog()
  }
}
