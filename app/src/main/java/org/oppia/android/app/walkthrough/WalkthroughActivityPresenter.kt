package org.oppia.android.app.walkthrough

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.walkthrough.end.WalkthroughFinalFragment
import org.oppia.android.app.walkthrough.topiclist.WalkthroughTopicListFragment
import org.oppia.android.app.walkthrough.welcome.WalkthroughWelcomeFragment
import org.oppia.android.databinding.WalkthroughActivityBinding
import org.oppia.android.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [WalkthroughActivity]. */
@ActivityScope
class WalkthroughActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val walkthroughViewModel: WalkthroughViewModel
) : WalkthroughActivityListener {
  private lateinit var topicId: String
  private lateinit var binding: WalkthroughActivityBinding
  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    binding = DataBindingUtil.setContentView(activity, R.layout.walkthrough_activity)
    profileId = ProfileId.newBuilder().apply { this.internalId = internalProfileId }.build()

    binding.apply {
      viewModel = walkthroughViewModel
      presenter = this@WalkthroughActivityPresenter
      lifecycleOwner = activity
    }
    StatusBarColor.statusBarColorUpdate(
      R.color.component_color_walkthrough_activity_status_bar_color,
      activity,
      true
    )
    val currentFragmentIndex = walkthroughViewModel.currentProgress.get()?.minus(1)

    if (currentFragmentIndex == -1 && getWalkthroughWelcomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.walkthrough_fragment_placeholder,
        createWalkthroughWelcomeFragment()
      ).commitNow().also {
        walkthroughViewModel.currentProgress.set(1)
      }
    } else if (currentFragmentIndex != null) {
      when (currentFragmentIndex) {
        0 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughWelcomeFragment() ?: createWalkthroughWelcomeFragment()
          ).commitNow().also {
            walkthroughViewModel.currentProgress.set(1)
          }
        1 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughTopicListFragment() ?: createWalkthroughTopicListFragment()
          ).commitNow().also {
            walkthroughViewModel.currentProgress.set(2)
          }
        2 ->
          activity.supportFragmentManager.beginTransaction().replace(
            R.id.walkthrough_fragment_placeholder,
            getWalkthroughFinalFragment() ?: createWalkthroughFinalFragment()
          ).commitNow().also {
            walkthroughViewModel.currentProgress.set(3)
          }
      }
    }
  }

  override fun moveToPreviousPage(currentProgress: Int) {
    if (currentProgress == 1)
      activity.finish()
    else {
      changePage(currentProgress - 2)
    }
  }

  private fun getWalkthroughWelcomeFragment(): WalkthroughWelcomeFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughWelcomeFragment?
  }

  private fun createWalkthroughWelcomeFragment(): WalkthroughWelcomeFragment =
    WalkthroughWelcomeFragment.createFragment(profileId.internalId)

  private fun getWalkthroughTopicListFragment(): WalkthroughTopicListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughTopicListFragment?
  }

  private fun createWalkthroughTopicListFragment(): WalkthroughTopicListFragment =
    WalkthroughTopicListFragment.createFragment(profileId.internalId)

  private fun getWalkthroughFinalFragment(): WalkthroughFinalFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.walkthrough_fragment_placeholder
      ) as WalkthroughFinalFragment?
  }

  private fun createWalkthroughFinalFragment(): WalkthroughFinalFragment =
    WalkthroughFinalFragment.newInstance(topicId, profileId.internalId)

  fun changePage(pageNumber: Int) {
    when (pageNumber) {
      WalkthroughPages.WELCOME.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          createWalkthroughWelcomeFragment()
        ).commitNow().also {
          walkthroughViewModel.currentProgress.set(1)
        }
      }
      WalkthroughPages.TOPIC_LIST.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          createWalkthroughTopicListFragment()
        ).commitNow().also {
          walkthroughViewModel.currentProgress.set(2)
        }
      }
      WalkthroughPages.FINAL.value -> {
        activity.supportFragmentManager.beginTransaction().replace(
          R.id.walkthrough_fragment_placeholder,
          createWalkthroughFinalFragment()
        ).commitNow().also {
          walkthroughViewModel.currentProgress.set(3)
        }
      }
    }
  }

  fun handleSystemBack() {
    moveToPreviousPage(walkthroughViewModel.currentProgress.get() ?: 1)
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }
}
