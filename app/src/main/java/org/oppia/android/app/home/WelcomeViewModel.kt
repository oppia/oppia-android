package org.oppia.android.app.home

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel(
  fragment: Fragment,
  oppiaClock: OppiaClock,
  val profileName: String
): HomeItemViewModel() {
  val greeting: String = DateTimeUtil(
    fragment.requireContext(),
    oppiaClock
  ).getGreetingMessage()
}
