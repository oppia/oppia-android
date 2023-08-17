package org.oppia.android.app.home

import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.datetime.DateTimeUtil
import java.util.Objects

/** [ViewModel] for welcome text in home screen. */
class WelcomeViewModel(
  private val profileName: String,
  private val resourceHandler: AppLanguageResourceHandler,
  dateTimeUtil: DateTimeUtil
) : HomeItemViewModel() {

  /** Text [String] to greet the learner. */
  val greeting: String = dateTimeUtil.getGreetingMessage()

  /**
   *  Returns the string which contains greeting message with user's name and
   *  display on-screen when launching the home activity.
   */
  fun welcomeText(): String {
    val profName = resourceHandler.getStringInLocaleWithWrapping(
      R.string.welcome_profile_name, profileName
    )
    return "$greeting $profName"
  }

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
