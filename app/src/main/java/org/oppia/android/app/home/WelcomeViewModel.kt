package org.oppia.android.app.home

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel @Inject constructor(
  fragment: Fragment,
  oppiaClock: OppiaClock
  ) : HomeItemViewModel() {
  private val greeting: String = DateTimeUtil(
    fragment.requireContext(),
    oppiaClock
  ).getGreetingMessage()

  val profileName = ObservableField<String>("")

  fun getGreeting() : String {
    return greeting
  }
}
