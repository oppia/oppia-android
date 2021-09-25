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

  /** Text [String] to greet the learner and display on-screen when launching the home activity. */
  val greeting: String = dateTimeUtil.getGreetingMessage()

  /**
   * Returns the user-readable portion of the welcome screen greeting that contains the user's name.
   */
  fun computeProfileNameText(): String {
    return resourceHandler.getStringInLocaleWithWrapping(R.string.welcome_profile_name, profileName)
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
