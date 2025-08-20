package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import javax.inject.Inject

/** Dialog fragment to restart the app when platform parameters are updated. */
class PlatformParameterRestartDialogFragment : InjectableDialogFragment() {

  @Inject
  lateinit var platformParameterRestartDialogFragmentPresenter:
    PlatformParameterRestartDialogFragmentPresenter
  companion object {
    /** Returns a new instance of [PlatformParameterRestartDialogFragment]. */
    fun newInstance(): PlatformParameterRestartDialogFragment =
      PlatformParameterRestartDialogFragment()
  }
  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return platformParameterRestartDialogFragmentPresenter.handleOnCreateDialog()
  }
}
