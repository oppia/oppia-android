package org.oppia.android.app.devoptions

import android.app.Dialog
import android.content.Context
import android.os.Bundle
<<<<<<< HEAD
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
=======
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableDialogFragment

/** Dialog fragment that prompts the user to restart the app when platform parameters are updated. */
class PlatformParameterRestartDialogFragment: InjectableDialogFragment() {

  @Inject
  lateinit var presenter: PlatformParameterRestartDialogFragmentPresenter
  companion object{
    /** Returns a new instance of [PlatformParameterRestartDialogFragment]. */
    fun newInstance(): PlatformParameterRestartDialogFragment = PlatformParameterRestartDialogFragment()
>>>>>>> 18896503a (added restart dialog implementation)
  }
  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
<<<<<<< HEAD
    return platformParameterRestartDialogFragmentPresenter.handleOnCreateDialog()
  }
}
=======
       return presenter.handleOnCreateDialog()
  }
}
>>>>>>> 18896503a (added restart dialog implementation)
