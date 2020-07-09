package org.oppia.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.oppia.app.R
import org.oppia.app.databinding.TopicFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EventLog
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.oppialogger.analytics.AnalyticsController
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicViewModel>,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock
) {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager
  private val tabIcons =
    intArrayOf(
      R.drawable.ic_info_icon_24dp,
      R.drawable.ic_lessons_icon_24dp,
      R.drawable.ic_practice_icon_24dp,
      R.drawable.ic_revision_icon_24dp
    )

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String
  ): View? {
    val binding = TopicFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    this.storyId = storyId
    viewPager = binding.root.findViewById(R.id.topic_tabs_viewpager) as ViewPager
    tabLayout = binding.root.findViewById(R.id.topic_tabs_container) as TabLayout
    this.internalProfileId = internalProfileId
    this.topicId = topicId

    binding.topicToolbar.setNavigationOnClickListener {
      (activity as TopicActivity).finish()
    }

    val viewModel = getTopicViewModel()
    viewModel.setInternalProfileId(internalProfileId)
    viewModel.setTopicId(topicId)
    binding.viewModel = viewModel

    setUpViewPager(viewPager, topicId)
    return binding.root
  }

  private fun setCurrentTab(tab: TopicTab) {
    viewPager.setCurrentItem(tab.ordinal, true)
    logTopicEvents(tab)
  }

  private fun setUpViewPager(viewPager: ViewPager, topicId: String) {
    val adapter =
      ViewPagerAdapter(fragment.childFragmentManager, internalProfileId, topicId, storyId)
    viewPager.adapter = adapter
    tabLayout.setupWithViewPager(viewPager)
    tabLayout.getTabAt(0)!!.setText(fragment.getString(R.string.info)).setIcon(tabIcons[0])
    tabLayout.getTabAt(1)!!.setText(fragment.getString(R.string.lessons)).setIcon(tabIcons[1])
    tabLayout.getTabAt(2)!!.setText(fragment.getString(R.string.practice)).setIcon(tabIcons[2])
    tabLayout.getTabAt(3)!!.setText(fragment.getString(R.string.revision)).setIcon(tabIcons[3])
    if (topicId.isNotEmpty() && storyId.isNotEmpty())
      setCurrentTab(TopicTab.LESSONS)
    else if (topicId.isNotEmpty() && storyId.isEmpty())
      setCurrentTab(TopicTab.INFO)
  }

  private fun getTopicViewModel(): TopicViewModel {
    return viewModelProvider.getForFragment(fragment, TopicViewModel::class.java)
  }

  private fun logTopicEvents(tab: TopicTab) {
    when (tab) {
      TopicTab.INFO -> logInfoFragmentEvent(topicId)
      TopicTab.LESSONS -> logLessonsFragmentEvent(topicId)
      TopicTab.PRACTICE -> logPracticeFragmentEvent(topicId)
      TopicTab.REVISION -> logRevisionFragmentEvent(topicId)
    }
  }

  private fun logInfoFragmentEvent(topicId: String) {
    analyticsController.logTransitionEvent(
      fragment.requireActivity().applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_INFO_TAB,
      analyticsController.createTopicContext(topicId)
    )
  }

  private fun logLessonsFragmentEvent(topicId: String) {
    analyticsController.logTransitionEvent(
      fragment.requireActivity().applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_LESSONS_TAB,
      analyticsController.createTopicContext(topicId)
    )
  }

  private fun logPracticeFragmentEvent(topicId: String) {
    analyticsController.logTransitionEvent(
      fragment.requireActivity().applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_PRACTICE_TAB,
      analyticsController.createTopicContext(topicId)
    )
  }

  private fun logRevisionFragmentEvent(topicId: String) {
    analyticsController.logTransitionEvent(
      fragment.requireActivity().applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_REVISION_TAB,
      analyticsController.createTopicContext(topicId)
    )
  }
}
