package org.oppia.android.app.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.takusemba.spotlight.Target
import java.util.*
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightState
import org.oppia.android.app.model.TopicSpotlightCheckpoint
import org.oppia.android.app.spotlight.OverlayPositionAutomator
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.TopicFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.spotlight.SpotlightActivity
import org.oppia.android.domain.spotlight.SpotlightStateController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.system.OppiaClock

/** The presenter for [TopicFragment]. */
@FragmentScope
class TopicFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<TopicViewModel>,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val spotlightStateController: SpotlightStateController,
  @EnableSpotlightUi val enableSpotlightUi: PlatformParameterValue<Boolean>,
  @EnablePracticeTab private val enablePracticeTab: Boolean,
  private val resourceHandler: AppLanguageResourceHandler
) {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager2
  private lateinit var overlayBinding: Any
  private lateinit var binding: TopicFragmentBinding
  private lateinit var spotlightOverlayPositionAutomator: OverlayPositionAutomator

  private fun getTab(tab: TopicTab): View {
    return tabLayout.getTabAt(tab.ordinal)!!.view
  }

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    isConfigChanged: Boolean
  ): View? {
    binding = TopicFragmentBinding.inflate(
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


    spotlightOverlayPositionAutomator = OverlayPositionAutomator(activity, fragment)

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
      ViewPagerAdapter(fragment, internalProfileId, topicId, storyId, enablePracticeTab)
    viewPager2.adapter = adapter
    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
      val topicTab = TopicTab.getTabForPosition(position, enablePracticeTab)
      tab.text = resourceHandler.getStringInLocale(topicTab.tabLabelResId)
      tab.icon = ContextCompat.getDrawable(activity, topicTab.tabIconResId)
    }.attach()
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
      oppiaLogger.createOpenInfoTabContext(topicId)
    )
  }

  private fun logLessonsFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      oppiaLogger.createOpenLessonsTabContext(topicId)
    )
  }

  private fun logPracticeFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      oppiaLogger.createOpenPracticeTabContext(topicId)
    )
  }

  private fun logRevisionFragmentEvent(topicId: String) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentTimeMs(),
      oppiaLogger.createOpenRevisionTabContext(topicId)
    )
  }

  fun retrieveCheckpointAndInitializeSpotlight() {
//    val targets = ArrayList<Target>()

    val profileId = ProfileId.newBuilder()
      .setInternalId(internalProfileId)
      .build()
    val checkpointLiveData = spotlightStateController.retrieveSpotlightCheckpoint(
      profileId,
      SpotlightActivity.TOPIC_ACTIVITY
    ).toLiveData()

    checkpointLiveData.observe(
      fragment,
      object : Observer<AsyncResult<Any>> {
        override fun onChanged(it: AsyncResult<Any>?) {
          if (it is AsyncResult.Success) {
            checkpointLiveData.removeObserver(this)
            val spotlightState = (it.value as TopicSpotlightCheckpoint).spotlightState
            if (spotlightState == SpotlightState.SPOTLIGHT_STATE_COMPLETED ||
              spotlightState == SpotlightState.SPOTLIGHT_STATE_DISMISSED
            ) {
              return
            } else if (spotlightState == SpotlightState.SPOTLIGHT_STATE_PARTIAL) {
              val lastScreenViewed = (it.value as TopicSpotlightCheckpoint).lastScreenViewed
              when (lastScreenViewed) {
                TopicSpotlightCheckpoint.LastScreenViewed.INFO_TAB_SPOTLIGHT -> {
//                  targets.add(lessonsTabSpotlightTarget)
//                  targets.add(practiceTabSpotlightTarget)
//                  targets.add(revisionTabSpotlightTarget)
//                  spotlightOverlayPositionAutomator.startSpotlight(targets)
                }
                TopicSpotlightCheckpoint.LastScreenViewed.LESSONS_TAB_SPOTLIGHT -> {
//                  targets.add(practiceTabSpotlightTarget)
//                  targets.add(revisionTabSpotlightTarget)
//                  spotlightOverlayPositionAutomator.startSpotlight(targets)
                }
                TopicSpotlightCheckpoint.LastScreenViewed.PRACTICE_TAB_SPOTLIGHT -> {
//                  targets.add(revisionTabSpotlightTarget)
//                  spotlightOverlayPositionAutomator.startSpotlight(targets)
                }
              }
            } else if (spotlightState == SpotlightState.SPOTLIGHT_STATE_UNKNOWN) {
              spotlightOverlayPositionAutomator.createTarget(
                getTab(TopicTab.INFO),
                OverlayPositionAutomator.Companion.SpotlightShape.RoundedRectangle
              )
              spotlightOverlayPositionAutomator.createTarget(
                getTab(TopicTab.LESSONS),
                OverlayPositionAutomator.Companion.SpotlightShape.RoundedRectangle
              )
              spotlightOverlayPositionAutomator.createTarget(
                getTab(TopicTab.PRACTICE),
                OverlayPositionAutomator.Companion.SpotlightShape.RoundedRectangle
              )
              spotlightOverlayPositionAutomator.createTarget(
                getTab(TopicTab.REVISION),
                OverlayPositionAutomator.Companion.SpotlightShape.RoundedRectangle
              )
              spotlightOverlayPositionAutomator.startSpotlight()
            }
          }
        }
      }
    )
  }

}
