package org.oppia.android.app.topic

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.shape.RoundedRectangle
import java.util.*
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SpotlightState
import org.oppia.android.app.model.TopicSpotlightCheckpoint
import org.oppia.android.app.onboarding.SpotlightNavigationListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.OverlayBinding
import org.oppia.android.databinding.OverlayOverLeftBinding
import org.oppia.android.databinding.OverlayOverRightBinding
import org.oppia.android.databinding.OverlayUnderLeftBinding
import org.oppia.android.databinding.OverlayUnderRightBinding
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
) : SpotlightNavigationListener {
  private lateinit var tabLayout: TabLayout
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var viewPager: ViewPager2
  private lateinit var overlayBinding: Any
  private lateinit var binding: TopicFragmentBinding
  private lateinit var spotlight: Spotlight
  private lateinit var anchor: View


  private val infoTabSpotlightTarget by lazy {
    anchor = getTab(TopicTab.INFO)

    Target.Builder()
      .setAnchor(anchor)
      .setShape(RoundedRectangle(anchor.height.toFloat(), anchor.width.toFloat(), 24f))
      .setOverlay(getSpotlightOverlay()!!)
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {
          // any additional behaviour

//          setConstraints()

        }

        override fun onEnded() {
//          getTopicViewModel().recordSpotlightCheckpoint(
//            TopicSpotlightCheckpoint.LastScreenViewed.INFO_TAB_SPOTLIGHT,
//          )
        }
      })
      .build()
  }

  private val lessonsTabSpotlightTarget by lazy {
    val anchor = getTab(TopicTab.LESSONS)

    Target.Builder()
      .setAnchor(getTab(TopicTab.LESSONS))
      .setShape(RoundedRectangle(anchor.height.toFloat(), anchor.width.toFloat(), 24f))
      .setOverlay(getSpotlightOverlay()!!)
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {

        }

        override fun onEnded() {
//          getTopicViewModel().recordSpotlightCheckpoint(
//            TopicSpotlightCheckpoint.LastScreenViewed.LESSONS_TAB_SPOTLIGHT,
//          )
        }
      })
      .build()
  }

  private val practiceTabSpotlightTarget by lazy {

    Target.Builder()
      .setAnchor(getTab(TopicTab.PRACTICE))
      .setShape(RoundedRectangle(anchor.height.toFloat(), anchor.width.toFloat(), 24f))
      .setOverlay(getSpotlightOverlay()!!)
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {
        }

        override fun onEnded() {
//          getTopicViewModel().recordSpotlightCheckpoint(
//            TopicSpotlightCheckpoint.LastScreenViewed.PRACTICE_TAB_SPOTLIGHT,
//          )
        }
      })
      .build()
  }

  private val revisionTabSpotlightTarget by lazy {

    Target.Builder()
      .setAnchor(getTab(TopicTab.REVISION))
      .setShape(RoundedRectangle(anchor.height.toFloat(), anchor.width.toFloat(), 24f))
      .setOverlay(getSpotlightOverlay()!!)
      .setOnTargetListener(object : OnTargetListener {
        override fun onStarted() {
        }

        override fun onEnded() {
//          getTopicViewModel().recordSpotlightCheckpoint(
//            TopicSpotlightCheckpoint.LastScreenViewed.REVISION_TAB_SPOTLIGHT,
//          )
        }
      })
      .build()
  }

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
    val targets = ArrayList<Target>()

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
                  targets.add(lessonsTabSpotlightTarget)
                  targets.add(practiceTabSpotlightTarget)
                  targets.add(revisionTabSpotlightTarget)
                  startSpotlight(targets)
                }
                TopicSpotlightCheckpoint.LastScreenViewed.LESSONS_TAB_SPOTLIGHT -> {
                  targets.add(practiceTabSpotlightTarget)
                  targets.add(revisionTabSpotlightTarget)
                  startSpotlight(targets)
                }
                TopicSpotlightCheckpoint.LastScreenViewed.PRACTICE_TAB_SPOTLIGHT -> {
                  targets.add(revisionTabSpotlightTarget)
                  startSpotlight(targets)
                }
              }
            } else if (spotlightState == SpotlightState.SPOTLIGHT_STATE_UNKNOWN) {
              targets.add(infoTabSpotlightTarget)
              targets.add(lessonsTabSpotlightTarget)
              startSpotlight(targets)
            }
          }
        }
      }
    )
  }

  private fun startSpotlight(targets: ArrayList<Target>) {
    spotlight = Spotlight.Builder(activity)
      .setTargets(targets)
      .setBackgroundColorRes(R.color.spotlightBackground)
      .setDuration(1000L)
      .setAnimation(DecelerateInterpolator(2f))
      .setOnSpotlightListener(object : OnSpotlightListener {
        override fun onStarted() {
        }

        override fun onEnded() {
        }
      })
      .build()

    spotlight.start()
  }

  override fun clickOnDismiss() {
    spotlight.finish()
  }

  override fun clickOnNextTip() {
    spotlight.next()
  }

  val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

  private lateinit var overlayHintVerticalPosition: OverlayHintVerticalPosition
  private lateinit var overlayHintHorizontalPosition: OverlayHintHorizontalPosition

  private fun getAnchorTopMargin(): Int {
    return anchor.top
  }

  private fun getAnchorBottomMargin(): Int {
    return anchor.bottom
  }

  private fun getAnchorLeftMargin(): Int {
    return anchor.left
  }

  private fun getAnchorRightMargin(): Int {
    return anchor.right
  }

  private fun getAnchorHeight(): Int {
    return anchor.height
  }

  private fun getAnchorWidth(): Int {
    return anchor.width
  }

//  private fun getArrowTopMargin(): Int {
//      return 0
//  TODO: this is only possible after reources can be accessed from this class. Not a necessity currently
//  }

//  private fun getArrowHeight(): Int{
//    resources.getDimensionPixelSize(R.dimen.arrow_height)
//  }

  private fun calculateOverlayVerticalHintPosition() {
    overlayHintVerticalPosition = if (getAnchorBottomMargin() > getAnchorTopMargin()) {
      OverlayHintVerticalPosition.UNDER
    } else OverlayHintVerticalPosition.OVER
  }

  private fun calculateOverlayHorizontalHintPosition() {
    overlayHintHorizontalPosition = if (getAnchorLeftMargin() > getAnchorRightMargin()) {
      OverlayHintHorizontalPosition.RIGHT
    } else OverlayHintHorizontalPosition.LEFT
  }

  private fun getSpotlightOverlay(): View? {

    calculateOverlayHorizontalHintPosition()
    calculateOverlayVerticalHintPosition()


    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER) {
      overlayBinding = OverlayUnderRightBinding.inflate(fragment.layoutInflater)
      (overlayBinding as OverlayUnderRightBinding).let {
        it.lifecycleOwner = fragment
        it.presenter = this
      }
      val arrowParams = (overlayBinding as OverlayUnderRightBinding).arrow.layoutParams as ViewGroup.MarginLayoutParams
      arrowParams.setMargins(
        calculateArrowLeftMargin().dp,
        calculateArrowTopMargin().dp,
        10.dp,
        10.dp
      )
      (overlayBinding as OverlayUnderRightBinding).arrow.layoutParams = arrowParams

       return (overlayBinding as OverlayUnderRightBinding).root
    }

    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER) {
      overlayBinding = OverlayOverRightBinding.inflate(fragment.layoutInflater)
      (overlayBinding as OverlayOverRightBinding).let {
        it.lifecycleOwner = fragment
        it.presenter = this
      }

      val arrowParams = (overlayBinding as OverlayOverRightBinding).arrow.layoutParams as ViewGroup.MarginLayoutParams
      arrowParams.setMargins(
        calculateArrowLeftMargin().dp,
        calculateArrowTopMargin().dp,
        10.dp,
        10.dp
      )
      (overlayBinding as OverlayOverRightBinding).arrow.layoutParams = arrowParams

      return (overlayBinding as OverlayOverRightBinding).root
    }

    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER) {
      overlayBinding = OverlayOverLeftBinding.inflate(fragment.layoutInflater)
      (overlayBinding as OverlayOverLeftBinding).let {
        it.lifecycleOwner = fragment
        it.presenter = this
      }

      val arrowParams = (overlayBinding as OverlayOverLeftBinding).arrow.layoutParams as ViewGroup.MarginLayoutParams
      arrowParams.setMargins(
        calculateArrowLeftMargin().dp,
        calculateArrowTopMargin().dp,
        10.dp,
        10.dp
      )
      (overlayBinding as OverlayOverLeftBinding).arrow.layoutParams = arrowParams

      return (overlayBinding as OverlayOverLeftBinding).root
    }

    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER) {
      overlayBinding = OverlayUnderLeftBinding.inflate(fragment.layoutInflater)
      (overlayBinding as OverlayUnderLeftBinding).let {
        it.lifecycleOwner = fragment
        it.presenter = this
      }

      val arrowParams = (overlayBinding as OverlayUnderLeftBinding).arrow.layoutParams as ViewGroup.MarginLayoutParams
      arrowParams.setMargins(
        calculateArrowLeftMargin().dp,
        calculateArrowTopMargin().dp,
        10.dp,
        10.dp
      )
      (overlayBinding as OverlayUnderLeftBinding).arrow.layoutParams = arrowParams

      return (overlayBinding as OverlayUnderLeftBinding).root
    }
    return null
  }

  fun calculateArrowTopMargin(): Int {
    calculateOverlayVerticalHintPosition()
    return if (overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER) {
      getAnchorTopMargin() + getAnchorHeight()
    } else {
      getAnchorBottomMargin() + getAnchorHeight()
    }
  }

  fun setConstraints() {
    val set = ConstraintSet()

//    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER){
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.RIGHT,
//        overlayBinding.arrow.id,
//        ConstraintSet.RIGHT,
//        0
//      )
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.BOTTOM,
//        overlayBinding.arrow.id,
//        ConstraintSet.TOP,
//        0
//      )
//    }
//    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT && overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER){
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.RIGHT,
//        overlayBinding.arrow.id,
//        ConstraintSet.RIGHT,
//        0
//      )
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.TOP,
//        overlayBinding.arrow.id,
//        ConstraintSet.BOTTOM,
//        0
//      )
//    }
//    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.OVER){
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.LEFT,
//        overlayBinding.arrow.id,
//        ConstraintSet.LEFT,
//        0
//      )
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.BOTTOM,
//        overlayBinding.arrow.id,
//        ConstraintSet.BOTTOM,
//        0
//      )
//    }
//    if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.LEFT || overlayHintVerticalPosition == OverlayHintVerticalPosition.UNDER){
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.LEFT,
//        overlayBinding.arrow.id,
//        ConstraintSet.LEFT,
//        0
//      )
//      set.connect(
//        overlayBinding.customText.id,
//        ConstraintSet.BOTTOM,
//        overlayBinding.arrow.id,
//        ConstraintSet.TOP,
//        0
//      )
//    }

//    set.applyTo(overlayBinding.overlayConstraintLayout)
  }

  fun calculateArrowLeftMargin(): Int {
    calculateOverlayHorizontalHintPosition()
    return if (overlayHintHorizontalPosition == OverlayHintHorizontalPosition.RIGHT) {
      getAnchorLeftMargin() + getAnchorWidth()
    } else {
      getAnchorLeftMargin() + 10
    }
  }

  sealed class OverlayHintVerticalPosition {
    object OVER : OverlayHintVerticalPosition()
    object UNDER : OverlayHintVerticalPosition()
  }

  sealed class OverlayHintHorizontalPosition {
    object RIGHT : OverlayHintHorizontalPosition()
    object LEFT : OverlayHintHorizontalPosition()
  }
}
