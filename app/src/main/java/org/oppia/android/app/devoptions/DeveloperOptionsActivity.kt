package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import javax.inject.Inject

const val LAST_LOADED_FRAGMENT_KEY = "LAST_LOADED_FRAGMENT_KEY"
const val MARK_CHAPTERS_COMPLETED_FRAGMENT = "MARK_CHAPTERS_COMPLETED_FRAGMENT"
const val MARK_STORIES_COMPLETED_FRAGMENT = "MARK_STORIES_COMPLETED_FRAGMENT"
const val MARK_TOPICS_COMPLETED_FRAGMENT = "MARK_TOPICS_COMPLETED_FRAGMENT"
const val EVENT_LOGS_FRAGMENT = "EVENT_LOGS_FRAGMENT"
const val FORCE_NETWORK_TYPE_FRAGMENT = "FORCE_NETWORK_TYPE_FRAGMENT"

/** Activity for Developer Options. */
class DeveloperOptionsActivity : InjectableAppCompatActivity() {
  @Inject lateinit var developerOptionsActivityPresenter: DeveloperOptionsActivityPresenter
  private lateinit var lastLoadedFragment: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    lastLoadedFragment = if (savedInstanceState != null)
        savedInstanceState.get(LAST_LOADED_FRAGMENT_KEY) as String
      else
        EVENT_LOGS_FRAGMENT
    developerOptionsActivityPresenter.handleOnCreate(lastLoadedFragment)
    title = getString(R.string.developer_options_title)
  }

  companion object {
    /** Function to create intent for DeveloperOptionsActivity */
    fun createDeveloperOptionsActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, internalProfileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    developerOptionsActivityPresenter.handleOnSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }
}
