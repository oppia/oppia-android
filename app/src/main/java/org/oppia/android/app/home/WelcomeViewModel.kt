package org.oppia.android.app.home

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.util.datetime.DateTimeUtil
import org.oppia.android.util.system.OppiaClock
import java.util.Objects

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

  // Overriding equals is needed so that DataProvider combine functions used in the HomeViewModel
  // will only rebind when the actual data in the data list changes, rather than when the ViewModel
  // object changes.
  override fun equals(other: Any?): Boolean {
    return other is WelcomeViewModel &&
      this.profileName == other.profileName &&
      this.greeting == other.greeting
  }

  override fun hashCode() = Objects.hash(profileName, greeting)
}
