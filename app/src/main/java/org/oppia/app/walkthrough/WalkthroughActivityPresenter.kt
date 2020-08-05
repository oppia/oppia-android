package org.oppia.app.walkthrough

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.WalkthroughActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.app.walkthrough.welcome.WalkthroughWelcomeFragment
import org.oppia.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [WalkthroughActivity]. */
@ActivityScope
class WalkthroughActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<WalkthroughViewModel>
): WalkthroughActivityListener {
  private lateinit var topicId: String
  private lateinit var binding: WalkthroughActivityBinding

  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(activity, R.layout.walkthrough_activity)

    binding.apply {
      viewModel = getWalkthroughViewModel()
      presenter = this@WalkthroughActivityPresenter
      lifecycleOwner = activity
    }
    StatusBarColor.statusBarColorUpdate(R.color.walkthroughStatusBar, activity, true)
    val currentFragmentIndex = getWalkthroughViewModel().currentProgress.get()?.minus(1)

    if (currentFragmentIndex == -1 && getWalkthroughWelcomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.walkthrough_fragment_placeholder,
        WalkthroughWelcomeFragment()
      ).commitNow().also {
        getWalkthroughViewModel().currentProgress.set(1)
      }
    } else if (currentFragmentIndex != null) {
      when (currentFragmentIndex) {
        0 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughWelcomeFragment() ?: WalkthroughWelcomeFragment()
          ).commitNow().also {
            getWalkthroughViewModel().currentProgress.set(1)
          }
        1 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughTopicListFragment() ?: WalkthroughTopicListFragment()
          ).commitNow().also {
            getWalkthroughViewModel().currentProgress.set(2)
          }
        2 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughFinalFragment() ?: WalkthroughFinalFragment()
          ).commitNow().also {
            getWalkthroughViewModel().currentProgress.set(3)
          }
      }
    }
  }

  override fun previousPage(currentProgress: Int) {
    if (currentProgress == 1)
      activity.finish()
    else {
      changePage(currentProgress - 2)
    }
  }

  private fun getWalkthroughViewModel(): WalkthroughViewModel {
    return viewModelProvider.getForActivity(activity, WalkthroughViewModel::class.java)
  }

  private fun getWalkthroughWelcomeFragment(): WalkthroughWelcomeFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughWelcomeFragment?
  }

  private fun getWalkthroughTopicListFragment(): WalkthroughTopicListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughTopicListFragment?
  }

  private fun getWalkthroughFinalFragment(): WalkthroughFinalFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughFinalFragment?
  }

  fun changePage(pageNumber: Int) {
    when (pageNumber) {
      WalkthroughPages.WELCOME.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          WalkthroughWelcomeFragment()
        ).commitNow().also {
          getWalkthroughViewModel().currentProgress.set(1)
        }
      }
      WalkthroughPages.TOPIC_LIST.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          WalkthroughTopicListFragment()
        ).commitNow().also {
          getWalkthroughViewModel().currentProgress.set(2)
        }
      }
      WalkthroughPages.FINAL.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          WalkthroughFinalFragment.newInstance(topicId)
        ).commitNow().also {
          getWalkthroughViewModel().currentProgress.set(3)
        }
      }
    }
  }

  fun handleSystemBack() {
    previousPage(getWalkthroughViewModel().currentProgress.get() ?: 1)
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }
}
