package org.oppia.android.app.profile

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.databinding.databinding.ProfileItemBinding
import org.oppia.android.app.databinding.databinding.ProfileSelectionFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileChooserActivityParams.ParentScreen
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.onboarding.CreateProfileActivity
import org.oppia.android.app.onboarding.IntroActivity
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.toProfileIdPreservingZero
import javax.inject.Inject

private val COLORS_LIST = listOf(
  R.color.component_color_avatar_background_1_color,
  R.color.component_color_avatar_background_2_color,
  R.color.component_color_avatar_background_3_color,
  R.color.component_color_avatar_background_4_color,
  R.color.component_color_avatar_background_5_color,
  R.color.component_color_avatar_background_6_color,
  R.color.component_color_avatar_background_7_color,
  R.color.component_color_avatar_background_8_color,
  R.color.component_color_avatar_background_9_color,
  R.color.component_color_avatar_background_10_color,
  R.color.component_color_avatar_background_11_color,
  R.color.component_color_avatar_background_12_color,
  R.color.component_color_avatar_background_13_color,
  R.color.component_color_avatar_background_14_color,
  R.color.component_color_avatar_background_15_color,
  R.color.component_color_avatar_background_16_color,
  R.color.component_color_avatar_background_17_color,
  R.color.component_color_avatar_background_18_color,
  R.color.component_color_avatar_background_19_color,
  R.color.component_color_avatar_background_20_color,
  R.color.component_color_avatar_background_21_color,
  R.color.component_color_avatar_background_22_color,
  R.color.component_color_avatar_background_23_color,
  R.color.component_color_avatar_background_24_color
)

/** The presenter for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val context: Context,
  private val chooserViewModel: ProfileChooserViewModel,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var binding: ProfileSelectionFragmentBinding
  private var isBecomeAdministratorNoticeShowing = false
  private var hasRequestedAdminProfileSpotlight = false

  private val isRtl by lazy {
    resourceHandler.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

  /** Binds ViewModel and sets up RecyclerView Adapter. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    adminProfileId: LegacyProfileId,
    parentScreen: ParentScreen
  ): View? {
    if (parentScreen == ParentScreen.CREATE_ADMIN_PIN_SCREEN) {
      // The admin onboarding ends here in order to prevent the admin from seeing the onboarding
      // flow again if they exit the app at this point.
      profileManagementController.markProfileOnboardingEnded(
        adminProfileId.toProfileIdPreservingZero()
      )
    }

    binding =
      ProfileSelectionFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
        .apply {
          viewModel = chooserViewModel
          lifecycleOwner = fragment
        }

    logProfileChooserEvent()

    requestSpotlight(
      anchor = binding.profileSelectionSettingIcon,
      hintId = R.string.profile_chooser_spotlight_admin_controls,
      feature = Spotlight.FeatureCase.PROFILE_ADMIN_CONTROLS_ITEM
    )
    if (binding.addProfileButton.visibility == View.VISIBLE) {
      requestSpotlight(
        anchor = binding.addProfileButton,
        hintId = R.string.profile_chooser_spotlight_add_learner,
        feature = Spotlight.FeatureCase.PROFILE_ADD_LEARNER_FAB
      )
    }

    binding.apply {
      when (Resources.getSystem().configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> setUpPortraitMode()
        Configuration.ORIENTATION_LANDSCAPE -> setUpLandscapeMode()
      }
    }

    binding.addProfileButton.setOnClickListener { addProfileButtonClickListener() }
    binding.addProfilePrompt.setOnClickListener { addProfileButtonClickListener() }

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToRootConstraintLayout(
        activity,
        binding.root as ConstraintLayout,
        R.color.component_color_shared_profile_status_bar_color
      )
    }
    return binding.root
  }

  private fun ProfileSelectionFragmentBinding.setUpPortraitMode() {
    setListSpanCount()

    profilesList?.apply {
      isNestedScrollingEnabled = false
      adapter = createRecyclerViewAdapter()
    }
  }

  private fun ProfileSelectionFragmentBinding.setUpLandscapeMode() {
    val snapHelper = StartSnapHelper()
    profilesListLandscape?.apply {
      isNestedScrollingEnabled = false
      adapter = createRecyclerViewAdapter()
    }

    val layoutManager = profilesListLandscape?.layoutManager as LinearLayoutManager?

    profilesListLandscape?.onFlingListener = null

    profilesListLandscape?.viewTreeObserver?.addOnGlobalLayoutListener {
      val landscapeList = profilesListLandscape as RecyclerView

      if (landscapeList.shouldShowScrollArrows()) {
        profileListScrollLeft?.visibility = View.VISIBLE
        profileListScrollRight?.visibility = View.VISIBLE
      } else {
        profileListScrollLeft?.visibility = View.GONE
        profileListScrollRight?.visibility = View.GONE
      }
    }

    profileListScrollLeft?.setOnClickListener {
      snapRecyclerView(layoutManager, snapHelper, true)
    }

    profileListScrollRight?.setOnClickListener {
      snapRecyclerView(layoutManager, snapHelper, false)
    }
  }

  private fun RecyclerView.shouldShowScrollArrows(): Boolean {
    val visibleItemCount = getVisibleItemCount(this)
    val totalItemCount = this.adapter?.itemCount
    return totalItemCount != null && totalItemCount > visibleItemCount
  }

  private fun getVisibleItemCount(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return 0

    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

    return if (firstVisiblePosition == RecyclerView.NO_POSITION ||
      lastVisiblePosition == RecyclerView.NO_POSITION
    ) {
      0 // No visible items
    } else {
      lastVisiblePosition - firstVisiblePosition + 1
    }
  }

  private fun snapRecyclerView(
    layoutManager: LinearLayoutManager?,
    snapHelper: StartSnapHelper,
    isLeft: Boolean
  ) {
    val newLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

    val targetView = snapHelper.findSnapView(layoutManager ?: newLayoutManager)
    targetView?.let { recyclerView ->
      val distance =
        snapHelper.calculateDistanceToFinalSnap(layoutManager ?: newLayoutManager, recyclerView)
      val scrollDistance = distance?.get(0) ?: 0
      val scrollableWidth = binding.profilesListLandscape?.let {
        it.width - (it.paddingStart + it.paddingEnd)
      } ?: 0

      val scrollLeftInRtl = isRtl && isLeft
      val scrollRightInRtl = isRtl && !isLeft
      val scrollLeftInLtr = !isRtl && isLeft
      val scrollRightInLtr = !isRtl && !isLeft

      // Adjust offset based on layout direction and intent.
      val offset = when {
        scrollLeftInRtl -> scrollableWidth / 2 - scrollDistance
        scrollRightInRtl -> scrollDistance - scrollableWidth / 2
        scrollLeftInLtr -> scrollDistance - scrollableWidth / 2
        scrollRightInLtr -> scrollableWidth / 2 - scrollDistance
        else -> 0 // Fallback, though this shouldn't occur.
      }

      binding.profilesListLandscape?.smoothScrollBy(offset, 0)
    }
  }

  private fun setListSpanCount() {
    chooserViewModel.profilesList.observe(
      fragment,
      { profilesList ->
        val spanCount = if (profilesList.size > 1) {
          activity.resources.getInteger(R.integer.profile_chooser_span_count)
        } else {
          activity.resources.getInteger(R.integer.profile_chooser_first_time_span_count)
        }
        val layoutManager = GridLayoutManager(activity, spanCount)
        binding.profilesList?.layoutManager = layoutManager
      }
    )
  }

  /** Randomly selects a color for the new profile that is not already in use. */
  private fun selectUniqueRandomColor(): Int {
    return COLORS_LIST.map {
      ContextCompat.getColor(context, it)
    }.minus(chooserViewModel.usedColors).random()
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileItemViewModel> {
    return singleTypeBuilderFactory.create<ProfileItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ProfileItemBinding::inflate,
        setViewModel = this::bindProfileView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileItemBinding,
    viewModel: ProfileItemViewModel
  ) {
    binding.viewModel = viewModel
    if (
      viewModel.profile.profileType == ProfileType.SUPERVISOR &&
      !hasRequestedAdminProfileSpotlight
    ) {
      hasRequestedAdminProfileSpotlight = true
      requestSpotlight(
        anchor = binding.root,
        hintId = R.string.profile_chooser_spotlight_admin_profile,
        feature = Spotlight.FeatureCase.PROFILE_ADMIN_ITEM
      )
    }
  }

  private fun requestSpotlight(
    anchor: View,
    hintId: Int,
    feature: Spotlight.FeatureCase
  ) {
    // This fragment is attached immediately before the spotlight host. Post the lookup so that
    // requests made during view creation are delivered after the host has been attached.
    anchor.post {
      getSpotlightManager()?.requestSpotlightViewWithDelayedLayout(
        SpotlightTarget(
          anchor = anchor,
          hint = resourceHandler.getStringInLocale(hintId),
          feature = feature
        )
      )
    }
  }

  private fun addProfileButtonClickListener() {
    if (chooserViewModel.canAddProfile.get() != true) {
      showMaximumProfilesReachedDialog()
    } else if (chooserViewModel.adminPin.isEmpty()) {
      handleAdminWithoutPin()
    } else if (
      chooserViewModel.adminProfile.profileType == ProfileType.SUPERVISOR &&
      chooserViewModel.adminProfile.hasSeenBecomeAdministratorNotice
    ) {
      launchCreateProfile()
    } else {
      activity.startActivity(
        ProfileLoginActivity.createProfileLoginForAddProfileIntent(
          activity,
          chooserViewModel.adminProfileId,
          ProfileType.ADDITIONAL_LEARNER,
          selectUniqueRandomColor()
        )
      )
    }
  }

  private fun handleAdminWithoutPin() {
    val adminProfile = chooserViewModel.adminProfile
    if (
      adminProfile.profileType == ProfileType.SOLE_LEARNER &&
      !adminProfile.hasSeenBecomeAdministratorNotice
    ) {
      showBecomeAdministratorNotice()
    } else {
      launchCreateAdminPin()
    }
  }

  private fun showBecomeAdministratorNotice() {
    if (isBecomeAdministratorNoticeShowing) return
    isBecomeAdministratorNoticeShowing = true
    profileManagementController.markBecomeAdministratorNoticeSeen(
      chooserViewModel.adminProfileId.toProfileIdPreservingZero()
    ).toLiveData().observe(fragment.viewLifecycleOwner) { result ->
      when (result) {
        is AsyncResult.Success -> showPersistedBecomeAdministratorNotice()
        is AsyncResult.Failure -> isBecomeAdministratorNoticeShowing = false
        is AsyncResult.Pending -> Unit
      }
    }
  }

  private fun showPersistedBecomeAdministratorNotice() {
    val dialogHost = activity.findViewById<ViewGroup>(android.R.id.content)
    val composeView = ComposeView(fragment.requireContext()).apply {
      ViewTreeLifecycleOwner.set(this, fragment.viewLifecycleOwner)
      ViewTreeViewModelStoreOwner.set(this, fragment)
      ViewTreeSavedStateRegistryOwner.set(this, fragment)
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }
    composeView.setContent {
      BecomeAdministratorNoticeDialog(
        appLanguageResourceHandler = resourceHandler,
        onContinue = {
          isBecomeAdministratorNoticeShowing = false
          composeView.disposeComposition()
          dialogHost.removeView(composeView)
          launchCreateAdminPin()
        }
      )
    }
    dialogHost.addView(composeView, ViewGroup.LayoutParams(0, 0))
  }

  private fun launchCreateAdminPin() {
    activity.startActivity(
      CreateAdminPinActivity.createAdminPinActivityIntent(
        activity,
        chooserViewModel.adminProfileId
      )
    )
  }

  private fun launchCreateProfile() {
    activity.startActivity(
      CreateProfileActivity.createProfileActivityIntent(
        activity,
        chooserViewModel.adminProfileId,
        ProfileType.ADDITIONAL_LEARNER,
        selectUniqueRandomColor()
      )
    )
  }

  private fun showMaximumProfilesReachedDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setMessage(R.string.profile_selection_maximum_profiles_reached_message)
      .setPositiveButton(R.string.profile_selection_maximum_profiles_reached_close) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  private fun getSpotlightManager(): SpotlightManager? {
    return activity.supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager
  }

  private fun logProfileChooserEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenProfileChooserContext(),
      profileId = null // There's no profile currently logged in.
    )
  }

  private fun updateLearnerIdIfAbsent(profile: Profile) {
    if (profile.learnerId.isNullOrEmpty()) {
      // TODO(#4345): Block on the following data provider before allowing the user to log in.
      profileManagementController.initializeLearnerId(profile.id.toProfileIdPreservingZero())
    }
  }

  private fun computeLoginRoute(profile: Profile) {
    when {
      !profile.completedProfileOnboarding && profile.profileType != ProfileType.SUPERVISOR ->
        routeToOnboardingOrPinScreen(profile)
      else -> routeToHomeOrPinScreen(profile)
    }
  }

  private fun routeToOnboardingOrPinScreen(profile: Profile) {
    if (profile.pin.isNullOrBlank()) {
      launchOnboardingScreen(profile.id, profile.name)
    } else {
      launchPinScreen(profile.id)
    }
  }

  private fun routeToHomeOrPinScreen(profile: Profile) {
    if (profile.pin.isNullOrBlank()) {
      launchHomeScreen(profile.id)
    } else {
      launchPinScreen(profile.id)
    }
  }

  private fun launchOnboardingScreen(profileId: LegacyProfileId, profileName: String) {
    val introActivityParams = IntroActivityParams.newBuilder()
      .setProfileNickname(profileName)
      .setParentScreen(IntroActivityParams.ParentScreen.PROFILE_CHOOSER_SCREEN)
      .build()

    val intent = IntroActivity.createIntroActivity(activity, introActivityParams, profileId)
    activity.startActivity(intent)
  }

  private fun launchHomeScreen(profileId: LegacyProfileId) {
    profileManagementController.loginToProfile(
      profileId.toProfileIdPreservingZero()
    ).toLiveData().observe(fragment) {
      if (it is AsyncResult.Success) {
        if (enableMultipleClassrooms.value) {
          activity.startActivity(
            ClassroomListActivity.createClassroomListActivity(activity, profileId)
          )
        } else {
          activity.startActivity(
            HomeActivity.createHomeActivity(activity, profileId)
          )
        }
      }
    }
  }

  private fun launchPinScreen(profileId: LegacyProfileId) {
    activity.startActivity(
      ProfileLoginActivity.createProfileLoginActivityIntent(
        context,
        profileId
      )
    )
  }

  /** Handles navigation to either the [AdministratorControlsActivity] or [AdminAuthActivity]. */
  fun routeToAdminPin() {
    if (chooserViewModel.adminPin.isEmpty()) {
      val profileId = LegacyProfileId.newBuilder()
        .setInternalId(chooserViewModel.adminProfileId.internalId).build()
      activity.startActivity(
        AdministratorControlsActivity.createAdministratorControlsActivityIntent(
          activity,
          profileId
        )
      )
    } else {
      activity.startActivity(
        AdminAuthActivity.createAdminAuthActivityIntent(
          activity,
          chooserViewModel.adminPin,
          chooserViewModel.adminProfileId.internalId,
          selectUniqueRandomColor(),
          AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
        )
      )
    }
  }

  /** Click listener for handling clicks to login to a profile. */
  fun onProfileClick(profile: Profile) {
    updateLearnerIdIfAbsent(profile)
    computeLoginRoute(profile)
  }
}
