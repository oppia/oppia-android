package org.oppia.android.app.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.IntroActivityParams
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileChooserUiModel
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.onboarding.IntroActivity
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.ProfileChooserAddViewBinding
import org.oppia.android.databinding.ProfileChooserFragmentBinding
import org.oppia.android.databinding.ProfileChooserProfileViewBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.statusbar.StatusBarColor
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
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  @EnableMultipleClassrooms private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
  @EnableOnboardingFlowV2 private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>,
) {
  private lateinit var binding: ProfileChooserFragmentBinding
  val hasProfileEverBeenAddedValue = ObservableField<Boolean>(true)

  /** Binds ViewModel and sets up RecyclerView Adapter. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    StatusBarColor.statusBarColorUpdate(
      R.color.component_color_shared_profile_status_bar_color, activity, false
    )
    binding = ProfileChooserFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      viewModel = chooserViewModel
      lifecycleOwner = fragment
    }
    logProfileChooserEvent()
    binding.profileRecyclerView.isNestedScrollingEnabled = false
    binding.hasProfileEverBeenAddedValue = hasProfileEverBeenAddedValue
    subscribeToWasProfileEverBeenAdded()
    binding.profileRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun subscribeToWasProfileEverBeenAdded() {
    wasProfileEverBeenAdded.observe(
      activity,
      Observer<Boolean> {
        hasProfileEverBeenAddedValue.set(it)
        val spanCount = if (it) {
          activity.resources.getInteger(R.integer.profile_chooser_span_count)
        } else {
          activity.resources.getInteger(R.integer.profile_chooser_first_time_span_count)
        }
        val layoutManager = GridLayoutManager(activity, spanCount)
        binding.profileRecyclerView.layoutManager = layoutManager
      }
    )
  }

  private val wasProfileEverBeenAdded: LiveData<Boolean> by lazy {
    Transformations.map(
      profileManagementController.getWasProfileEverAdded().toLiveData(),
      ::processWasProfileEverBeenAddedResult
    )
  }

  private fun processWasProfileEverBeenAddedResult(
    wasProfileEverBeenAddedResult: AsyncResult<Boolean>
  ): Boolean {
    return when (wasProfileEverBeenAddedResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileChooserFragment",
          "Failed to retrieve the information on wasProfileEverBeenAdded",
          wasProfileEverBeenAddedResult.error
        )
        false
      }
      is AsyncResult.Pending -> false
      is AsyncResult.Success -> wasProfileEverBeenAddedResult.value
    }
  }

  /** Randomly selects a color for the new profile that is not already in use. */
  private fun selectUniqueRandomColor(): Int {
    return COLORS_LIST.map {
      ContextCompat.getColor(context, it)
    }.minus(chooserViewModel.usedColors).random()
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserUiModel> {
    return multiTypeBuilderFactory.create<ProfileChooserUiModel,
      ProfileChooserUiModel.ModelTypeCase>(
      ProfileChooserUiModel::getModelTypeCase
    )
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.PROFILE,
        inflateDataBinding = ProfileChooserProfileViewBinding::inflate,
        setViewModel = this::bindProfileView
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.ADD_PROFILE,
        inflateDataBinding = ProfileChooserAddViewBinding::inflate,
        setViewModel = this::bindAddView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileChooserProfileViewBinding,
    model: ProfileChooserUiModel
  ) {
    binding.viewModel = model
    binding.hasProfileEverBeenAddedValue = hasProfileEverBeenAddedValue
    binding.profileChooserItem.setOnClickListener {
      updateLearnerIdIfAbsent(model.profile)
      if (enableOnboardingFlowV2.value) {
        ensureProfileOnboarded(model.profile)
      } else {
        loginToProfile(model.profile)
      }
    }
  }

  private fun bindAddView(
    binding: ProfileChooserAddViewBinding,
    @Suppress("UNUSED_PARAMETER") model: ProfileChooserUiModel
  ) {
    binding.hasProfileEverBeenAddedValue = hasProfileEverBeenAddedValue
    binding.addProfileItem.setOnClickListener {
      if (chooserViewModel.adminPin.isEmpty()) {
        activity.startActivity(
          AdminPinActivity.createAdminPinActivityIntent(
            activity,
            chooserViewModel.adminProfileId.internalId,
            selectUniqueRandomColor(),
            AdminAuthEnum.PROFILE_ADD_PROFILE.value
          )
        )
      } else {
        activity.startActivity(
          AdminAuthActivity.createAdminAuthActivityIntent(
            activity,
            chooserViewModel.adminPin,
            -1,
            selectUniqueRandomColor(),
            AdminAuthEnum.PROFILE_ADD_PROFILE.value
          )
        )
      }
    }
  }

  fun routeToAdminPin() {
    if (chooserViewModel.adminPin.isEmpty()) {
      val profileId =
        ProfileId.newBuilder().setInternalId(chooserViewModel.adminProfileId.internalId).build()
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

  private fun logProfileChooserEvent() {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenProfileChooserContext(),
      profileId = null // There's no profile currently logged in.
    )
  }

  private fun updateLearnerIdIfAbsent(profile: Profile) {
    if (profile.learnerId.isNullOrEmpty()) {
      // TODO(#4345): Block on the following data provider before allowing the user to log in.
      profileManagementController.initializeLearnerId(profile.id)
    }
  }

  private fun ensureProfileOnboarded(profile: Profile) {
    if (profile.profileType == ProfileType.SUPERVISOR || profile.completedProfileOboarding) {
      loginToProfile(profile)
    } else {
      launchOnboardingScreen(profile.id, profile.name)
    }
  }

  private fun launchOnboardingScreen(profileId: ProfileId, profileName: String) {
    val introActivityParams = IntroActivityParams.newBuilder()
      .setProfileNickname(profileName)
      .build()

    val intent = IntroActivity.createIntroActivity(activity)
    intent.apply {
      putProtoExtra(IntroActivity.PARAMS_KEY, introActivityParams)
      decorateWithUserProfileId(profileId)
    }

    activity.startActivity(intent)
  }

  private fun loginToProfile(profile: Profile) {
    if (profile.pin.isNullOrBlank()) {
      profileManagementController.loginToProfile(profile.id).toLiveData().observe(
        fragment,
        {
          if (it is AsyncResult.Success) {
            if (enableMultipleClassrooms.value) {
              activity.startActivity(
                ClassroomListActivity.createClassroomListActivity(activity, profile.id)
              )
            } else {
              activity.startActivity(
                HomeActivity.createHomeActivity(activity, profile.id)
              )
            }
          }
        }
      )
    } else {
      val pinPasswordIntent = PinPasswordActivity.createPinPasswordActivityIntent(
        activity,
        chooserViewModel.adminPin,
        profile.id.internalId
      )
      activity.startActivity(pinPasswordIntent)
    }
  }
}
