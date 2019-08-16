package org.oppia.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.model.UserAppHistory
import org.oppia.util.data.AsyncResult

const val USER_APP_HISTORY_TEST_FRAGMENT_TAG = "test_fragment"

/** A test-only activity used for verifying [org.oppia.domain.UserAppHistoryController]. */
class TestUserAppHistoryControllerActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportFragmentManager.beginTransaction().add(TestFragment(), USER_APP_HISTORY_TEST_FRAGMENT_TAG).commitNow()
  }

  /** The primary test fragment used within the outer test activity. */
  class TestFragment : Fragment() {
    var userAppHistoryResult: AsyncResult<UserAppHistory>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return inflater.inflate(R.layout.user_app_history_test_fragment, container, /* attachToRoot= */ false)
    }

    /**
     * Observes the specified [LiveData] and updates this fragment's output text once ready. See [getOutputTextView].
     */
    fun observeUserAppHistory(userAppHistoryLiveData: LiveData<AsyncResult<UserAppHistory>>) {
      userAppHistoryLiveData.observe(this, Observer<AsyncResult<UserAppHistory>> { result ->
        userAppHistoryResult = result

//        val outputTextView = getOutputTextView()
//        outputTextView.text = getString(R.string.test_output_result, result.getOrThrow().alreadyOpenedApp)
      })
    }

    /** Returns the test's output [TextView] for result verification. */
    fun getOutputTextView(): TextView {
      return view?.findViewById(R.id.test_output_text)!!
    }
  }
}