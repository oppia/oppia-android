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
  private val oppiaClock: OppiaClock,
  @EnablePracticeTab private val enablePracticeTab: Boolean
) {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager2

  // TODO(3082): Remove this variable with the one, received from Home Activity
  private var isTopicDownloaded = false

  // TODO(3082): Replace this variable with the injected annotation
  private var enableMyDownloads = true

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    isConfigChanged: Boolean,
    enableMyDownloads: Boolean,
    isTopicDownloaded: Boolean
  ): View? {
    val binding = TopicFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    this.storyId = storyId
    this.enableMyDownloads = enableMyDownloads
    this.isTopicDownloaded = isTopicDownloaded
    viewPager = binding.root.findViewById(R.id.topic_tabs_viewpager) as ViewPager2
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
    viewModel.isTopicDownloaded = isTopicDownloaded
    viewModel.enableMyDownloads = enableMyDownloads
    binding.viewModel = viewModel

    setUpViewPager(viewPager, topicId, isConfigChanged)
    return binding.root
  }

  private fun setCurrentTab(tab: TopicTab) {
    viewPager.setCurrentItem(tab.ordinal, true)
    logTopicEvents(tab)
  }

  private fun setUpViewPager(viewPager2: ViewPager2, topicId: String, isConfigChanged: Boolean) {
    val adapter =
      ViewPagerAdapter(
        fragment,
        internalProfileId,
        topicId,
        storyId,
        enablePracticeTab,
        enableMyDownloads,
        isTopicDownloaded
      )
    viewPager2.adapter = adapter
    // TODO(#3072): check if topic is already downloaded or not
    if (!(enableMyDownloads && !isTopicDownloaded)) {
      TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
        val topicTab = TopicTab.getTabForPosition(position, enablePracticeTab)
        tab.text = fragment.getString(topicTab.tabLabelResId)
        tab.icon = ContextCompat.getDrawable(activity, topicTab.tabIconResId)
      }.attach()
    }
    if (!isConfigChanged && topicId.isNotEmpty()) {
      setCurrentTab(if (storyId.isNotEmpty()) TopicTab.LESSONS else TopicTab.INFO)
    }
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
      oppiaClock.getCurrentTimeMs(),
      EventLog.EventAction.OPEN_INFO_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logLessonsFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      EventLog.EventAction.OPEN_LESSONS_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logPracticeFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      EventLog.EventAction.OPEN_PRACTICE_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }

  private fun logRevisionFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      EventLog.EventAction.OPEN_REVISION_TAB,
      oppiaLogger.createTopicContext(topicId)
    )
  }
}
