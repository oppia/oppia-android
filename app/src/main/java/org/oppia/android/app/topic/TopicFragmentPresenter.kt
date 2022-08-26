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
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicViewModel>,
  private val oppiaLogger: OppiaLogger,
  @EnableExtraTopicTabsUi private val enableExtraTopicTabsUi: PlatformParameterValue<Boolean>,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager2

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    isConfigChanged: Boolean
  ): View? {
    val binding = TopicFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    this.storyId = storyId
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
      ViewPagerAdapter(fragment, internalProfileId, topicId, storyId, enableExtraTopicTabsUi.value)
    viewPager2.adapter = adapter
    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      val topicTab = TopicTab.getTabForPosition(position, enableExtraTopicTabsUi.value)
      tab.text = resourceHandler.getStringInLocale(topicTab.tabLabelResId)
      tab.icon = ContextCompat.getDrawable(activity, topicTab.tabIconResId)
    }.attach()
    if (!isConfigChanged && topicId.isNotEmpty()) {
      if (enableExtraTopicTabsUi.value) {
        setCurrentTab(if (storyId.isNotEmpty()) TopicTab.LESSONS else TopicTab.INFO)
      }
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
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenInfoTabContext(topicId))
  }

  private fun logLessonsFragmentEvent(topicId: String) {
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenLessonsTabContext(topicId))
  }

  private fun logPracticeFragmentEvent(topicId: String) {
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenPracticeTabContext(topicId))
  }

  private fun logRevisionFragmentEvent(topicId: String) {
    oppiaLogger.logImportantEvent(oppiaLogger.createOpenRevisionTabContext(topicId))
  }
}
