package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.domain.devoptions.ForceDownloadRemoteParametersController
import javax.inject.Inject

/** Dialog fragment shown for force downloading remote parameters. */
class ForceDownloadRemoteParametersDialogFragment : InjectableDialogFragment() {
  @Inject
  lateinit var forceDownloadRemoteParametersDialogFragmentPresenter:
    ForceDownloadRemoteParametersDialogFragmentPresenter
  @Inject
  lateinit var forceDownloadRemoteParametersParametersController:
    ForceDownloadRemoteParametersController

  companion object {
    /** Returns a new instance of [ForceDownloadRemoteParametersDialogFragment]. */
    fun newInstance(): ForceDownloadRemoteParametersDialogFragment =
      ForceDownloadRemoteParametersDialogFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return forceDownloadRemoteParametersDialogFragmentPresenter
      .handleOnCreateDialog(forceDownloadRemoteParametersParametersController)
  }
}
