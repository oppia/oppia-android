package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Dialog fragment shown to prompt a full app restart. */
class AppRestartDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var appRestartDialogFragmentPresenter:
    AppRestartDialogFragmentPresenter

  companion object {
    /** Returns a new instance of [AppRestartDialogFragment]. */
    fun newInstance(): AppRestartDialogFragment =
      AppRestartDialogFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return appRestartDialogFragmentPresenter.handleOnCreateDialog()
  }
}
