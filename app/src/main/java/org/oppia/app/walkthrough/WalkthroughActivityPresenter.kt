package org.oppia.app.walkthrough

import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.app.walkthrough.welcome.WalkthroughWelcomeFragment
import javax.inject.Inject

/** The presenter for [WalkthroughActivity]. */
@ActivityScope
class WalkthroughActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private var currentProgress = MutableLiveData(0)

  fun handleOnCreate() {
    activity.setContentView(R.layout.walkthrough_activity)
    if (getWalkthroughWelcomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.walkthrough_fragment_placeholder,
        WalkthroughWelcomeFragment()
      ).commitNow().also {
        currentProgress.value = 1
      }
    }

    currentProgress.observe(activity, Observer {
      activity.findViewById<ProgressBar>(R.id.walkthrough_progress_bar).progress = it
    })

    activity.findViewById<ImageView>(R.id.back_button).setOnClickListener {
      currentProgress.value?.let { progress ->
        if (progress > 1) {
          currentProgress.value = progress - 1
        } else {
          activity.onBackPressed()
        }
      }
    }
  }

  private fun getWalkthroughWelcomeFragment(): WalkthroughWelcomeFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.walkthrough_fragment_placeholder) as WalkthroughWelcomeFragment?
  }
  fun changePage(pageNo: Int) {
    when (pageNo) {
      2 -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          WalkthroughTopicListFragment()
        ).commitNow().also {
          currentProgress.value = 2
        }
      }
      3 -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          WalkthroughFinalFragment()
        ).commitNow().also {
          currentProgress.value = 3
        }
      }
    }
  }
}
