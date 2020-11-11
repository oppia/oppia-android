package org.oppia.android.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicViewModel>,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock
) {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager2: ViewPager2
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
    viewPager2 = binding.root.findViewById(R.id.topic_tabs_viewpager) as ViewPager2
    tabLayout = binding.root.findViewById(R.id.topic_tabs_container) as TabLayout
    this.internalProfileId = internalProfileId
    this.topicId = topicId

    binding.topicToolbar.setNavigationOnClickListener {
      (activity as TopicActivity).finish()
    }

    binding.topicToolbar.setOnClickListener {
      binding.topicToolbarTitle.isSelected = true
    }

    val viewModel = getTopicViewModel()
    viewModel.setInternalProfileId(internalProfileId)
    viewModel.setTopicId(topicId)
    binding.viewModel = viewModel

    setUpViewPager(viewPager2, topicId)
    return binding.root
  }

  private fun setCurrentTab(tab: TopicTab) {
    viewPager2.setCurrentItem(tab.ordinal, true)
    logTopicEvents(tab)
  }

  private fun setUpViewPager(viewPager2: ViewPager2, topicId: String) {
    val adapter =
      ViewPagerAdapter(activity, internalProfileId, topicId, storyId)
    viewPager2.adapter = adapter
    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      when (position) {
        0 -> {
          tab.text = fragment.getString(R.string.info)
          tab.icon = ContextCompat.getDrawable(fragment.context!!, tabIcons[0])
        }
        1 -> {
          tab.text = fragment.getString(R.string.lessons)
          tab.icon = ContextCompat.getDrawable(fragment.context!!, tabIcons[1])
        }
        2 -> {
          tab.text = fragment.getString(R.string.practice)
          tab.icon = ContextCompat.getDrawable(fragment.context!!, tabIcons[2])
        }
        3 -> {
          tab.text = fragment.getString(R.string.revision)
          tab.icon = ContextCompat.getDrawable(fragment.context!!, tabIcons[3])
        }
      }
    }.attach()
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
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_INFO_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logLessonsFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_LESSONS_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logPracticeFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_PRACTICE_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logRevisionFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_REVISION_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }
}
