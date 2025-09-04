package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.domain.devoptions.ForceDownloadParametersController
import javax.inject.Inject

/** Dialog fragment shown for force downloading remote parameters. */
class ForceDownloadDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var forceDownloadDialogFragmentPresenter:
    ForceDownloadDialogFragmentPresenter
  @Inject
  lateinit var forceDownloadParametersController: ForceDownloadParametersController

  companion object {
    /** Returns a new instance of [ForceDownloadDialogFragment]. */
    fun newInstance(): ForceDownloadDialogFragment =
      ForceDownloadDialogFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return forceDownloadDialogFragmentPresenter
      .handleOnCreateDialog(forceDownloadParametersController)
  }
}
