package org.oppia.app.walkthrough

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val KEY_CURRENT_FRAGMENT_INDEX = "CURRENT_FRAGMENT_INDEX"

/** Activity that contains the walkthrough flow for users. */
class WalkthroughActivity : InjectableAppCompatActivity(), WalkthroughFragmentChangeListener {
  @Inject
  lateinit var walkthroughActivityPresenter: WalkthroughActivityPresenter

  private var currentFragmentIndex: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState != null) {
      currentFragmentIndex = savedInstanceState.getInt(KEY_CURRENT_FRAGMENT_INDEX, -1)
      if (currentFragmentIndex == -1) {
        currentFragmentIndex = null
      }
    }
    activityComponent.inject(this)
    walkthroughActivityPresenter.handleOnCreate(currentFragmentIndex)
  }

  override fun currentPage(walkthroughPage: Int) {
    currentFragmentIndex = walkthroughPage
    walkthroughActivityPresenter.changePage(walkthroughPage)
  }

  override fun pageWithTopicId(walkthroughPage: Int, topicId: String) {
    currentFragmentIndex = walkthroughPage
    walkthroughActivityPresenter.setTopicId(topicId)
    walkthroughActivityPresenter.changePage(walkthroughPage)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentFragmentIndex != null) {
      outState.putInt(KEY_CURRENT_FRAGMENT_INDEX, currentFragmentIndex!!)
    }
  }

  override fun onBackPressed() {
    currentFragmentIndex =
      if (currentFragmentIndex != 0) currentFragmentIndex?.minus(1) else currentFragmentIndex
    walkthroughActivityPresenter.handleSystemBack()
  }

  companion object {
    internal const val WALKTHROUGH_ACTIVITY_INTERNAL_PROFILE_ID_KEY =
      "WalkthroughActivity.internal_profile_id"

    fun createWalkthroughActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, WalkthroughActivity::class.java)
      intent.putExtra(WALKTHROUGH_ACTIVITY_INTERNAL_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
