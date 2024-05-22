package org.oppia.android.app.notice

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Dialog fragment that informs the user that a new app version is available for download. */
class OptionalAppDeprecationNoticeDialogFragment : InjectableDialogFragment() {
  companion object {
    /** Returns a new instance of [OptionalAppDeprecationNoticeDialogFragment]. */
    fun newInstance(): OptionalAppDeprecationNoticeDialogFragment {
      return OptionalAppDeprecationNoticeDialogFragment()
    }
  }

  @Inject lateinit var optionalAppDeprecationNoticeDialogFragmentPresenter:
    OptionalAppDeprecationNoticeDialogFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return optionalAppDeprecationNoticeDialogFragmentPresenter.handleOnCreateDialog()
  }
}
