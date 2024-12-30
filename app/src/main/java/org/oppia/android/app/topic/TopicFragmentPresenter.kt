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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.TopicFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.platformparameter.EnableExtraTopicTabsUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModel: TopicViewModel,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  @EnableExtraTopicTabsUi private val enableExtraTopicTabsUi: PlatformParameterValue<Boolean>,
  private val resourceHandler: AppLanguageResourceHandler
) {
  @Inject
  lateinit var accessibilityService: AccessibilityService

  private lateinit var tabLayout: TabLayout
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager2

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    isConfigChanged: Boolean
  ): View {
    val binding = TopicFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.lifecycleOwner = fragment
    this.storyId = storyId
    viewPager = binding.root.findViewById(R.id.topic_tabs_viewpager) as ViewPager2
    tabLayout = binding.root.findViewById(R.id.topic_tabs_container) as TabLayout
    this.profileId = profileId
    this.topicId = topicId

    binding.topicToolbar.setNavigationOnClickListener {
      (activity as TopicActivity).finish()
    }
    if (!accessibilityService.isScreenReaderEnabled()) {
      binding.topicToolbarTitle.setOnClickListener {
        binding.topicToolbarTitle.isSelected = true
      }
    }
    viewModel.setProfileId(profileId)
    viewModel.setTopicId(topicId)
    binding.viewModel = viewModel

    setUpViewPager(viewPager, classroomId, topicId, isConfigChanged)
    return binding.root
  }

  /** Requests the spotlights required in this activity to be enqueued. */
  fun startSpotlight() {
    viewModel.numberOfChaptersCompletedLiveData.observe(fragment) { numberOfChaptersCompleted ->
      if (numberOfChaptersCompleted != null) {
        val lessonsTabView = tabLayout.getTabAt(computeTabPosition(TopicTab.LESSONS))?.view
        lessonsTabView?.let {
          val lessonsTabSpotlightTarget = SpotlightTarget(
            lessonsTabView,
            resourceHandler.getStringInLocale(R.string.topic_lessons_tab_spotlight_hint),
            SpotlightShape.RoundedRectangle,
            Spotlight.FeatureCase.TOPIC_LESSON_TAB
          )
          checkNotNull(getSpotlightManager()).requestSpotlight(lessonsTabSpotlightTarget)

          if (numberOfChaptersCompleted > 2) {
            val revisionTabView = tabLayout.getTabAt(computeTabPosition(TopicTab.REVISION))?.view
            val revisionTabSpotlightTarget = SpotlightTarget(
              revisionTabView!!,
              resourceHandler.getStringInLocale(R.string.topic_revision_tab_spotlight_hint),
              SpotlightShape.RoundedRectangle,
              Spotlight.FeatureCase.TOPIC_REVISION_TAB
            )
            checkNotNull(getSpotlightManager()).requestSpotlight(revisionTabSpotlightTarget)
          }
        }
      }
    }
  }

  private fun getSpotlightManager(): SpotlightManager? {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager
  }

  private fun setCurrentTab(tab: TopicTab) {
    viewPager.setCurrentItem(computeTabPosition(tab), true)
  }

  private fun computeTabPosition(tab: TopicTab): Int {
    return if (enableExtraTopicTabsUi.value) tab.positionWithFourTabs else tab.positionWithTwoTabs
  }

  private fun setUpViewPager(
    viewPager2: ViewPager2,
    classroomId: String,
    topicId: String,
    isConfigChanged: Boolean
  ) {
    val adapter =
      ViewPagerAdapter(
        fragment,
        profileId,
        classroomId,
        topicId,
        storyId,
        enableExtraTopicTabsUi.value
      )
    viewPager2.adapter = adapter
    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      val topicTab = TopicTab.getTabForPosition(position, enableExtraTopicTabsUi.value)
      tab.text = resourceHandler.getStringInLocale(topicTab.tabLabelResId)
      tab.icon = ContextCompat.getDrawable(activity, topicTab.tabIconResId)
      tab.contentDescription = resourceHandler.getStringInLocale(topicTab.contentDescriptionResId)
    }.attach()
    if (!isConfigChanged && topicId.isNotEmpty()) {
      if (enableExtraTopicTabsUi.value) {
        setCurrentTab(if (storyId.isNotEmpty()) TopicTab.LESSONS else TopicTab.INFO)
      } else {
        setCurrentTab(TopicTab.LESSONS)
      }
    }
    viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        logTopicEvents(TopicTab.getTabForPosition(position, enableExtraTopicTabsUi.value))
      }
    })
  }

  private fun logTopicEvents(tab: TopicTab) {
    val eventContext = when (tab) {
      TopicTab.INFO -> oppiaLogger.createOpenInfoTabContext(topicId)
      TopicTab.LESSONS -> oppiaLogger.createOpenLessonsTabContext(topicId)
      TopicTab.PRACTICE -> oppiaLogger.createOpenPracticeTabContext(topicId)
      TopicTab.REVISION -> oppiaLogger.createOpenRevisionTabContext(topicId)
    }
    analyticsController.logImportantEvent(eventContext, profileId)
  }
}
