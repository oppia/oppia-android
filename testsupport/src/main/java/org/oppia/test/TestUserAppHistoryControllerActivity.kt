package org.oppia.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncResult

const val USER_APP_HISTORY_TEST_FRAGMENT_TAG = "test_fragment"

/** A test-only activity used for verifying [org.oppia.domain.UserAppHistoryController]. */
class TestUserAppHistoryControllerActivity: AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportFragmentManager.beginTransaction().add(TestFragment(), USER_APP_HISTORY_TEST_FRAGMENT_TAG).commitNow()
  }

  /** The primary test fragment used within the outer test activity. */
  class TestFragment: Fragment() {
    var userAppHistoryResult: AsyncResult<UserAppHistory>? = null

    fun observeUserAppHistory(userAppHistoryLiveData: LiveData<AsyncResult<UserAppHistory>>) {
      userAppHistoryLiveData.observe(this, Observer<AsyncResult<UserAppHistory>> { result ->
        userAppHistoryResult = result
      })
    }
  }
}