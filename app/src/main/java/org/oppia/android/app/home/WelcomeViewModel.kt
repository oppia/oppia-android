package org.oppia.android.app.home

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.system.OppiaClock

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel(
  fragment: Fragment,
  oppiaClock: OppiaClock,
  val profileName: String
) : HomeItemViewModel() {

  /** Text [String] to greet the learner and display on-screen when launching the home activity. */
  val greeting: String = DateTimeUtil(
    fragment.requireContext(),
    oppiaClock
  ).getGreetingMessage()

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }
    if (other == null || other.javaClass != javaClass) {
      return false
    }
    val otherResult = other as WelcomeViewModel
    return otherResult.profileName == this.profileName
  }
}
