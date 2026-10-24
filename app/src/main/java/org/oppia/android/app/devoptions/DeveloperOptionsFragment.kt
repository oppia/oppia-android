package org.oppia.android.app.devoptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Developer Options of the application. */
class DeveloperOptionsFragment : InjectableFragment() {
  @Inject
  lateinit var developerOptionsFragmentPresenter: DeveloperOptionsFragmentPresenter

  companion object {
    fun newInstance(): DeveloperOptionsFragment {
      return DeveloperOptionsFragment()
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return developerOptionsFragmentPresenter.handleCreateView(inflater, container)
  }

  /** Called when the user clicks the 'delete all non-admin profiles' button. */
  fun deleteAllNonAdminProfiles() {
    developerOptionsFragmentPresenter.deleteAllNonAdminProfiles()
  }

  /** Called when the user clicks the 'add profile' button. */
  fun createProfile(count: Int) {
    developerOptionsFragmentPresenter.createProfile(count)
  }
}
