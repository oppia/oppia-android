package org.oppia.android.app.profile

import android.content.Context
import android.content.res.Resources
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
import org.oppia.android.app.R
import org.oppia.android.app.databinding.ProfileChooserAddViewBinding
import org.oppia.android.app.databinding.ProfileChooserFragmentBinding
import org.oppia.android.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.ProfileChooserUiModel
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileChooserAddViewBinding
import org.oppia.android.databinding.ProfileChooserFragmentBinding
import org.oppia.android.databinding.ProfileChooserProfileViewBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.statusbar.StatusBarColor
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

private val COLORS_LIST = listOf(
  R.color.avatar_background_1,
  R.color.avatar_background_2,
  R.color.avatar_background_3,
  R.color.avatar_background_4,
  R.color.avatar_background_5,
  R.color.avatar_background_6,
  R.color.avatar_background_7,
  R.color.avatar_background_8,
  R.color.avatar_background_9,
  R.color.avatar_background_10,
  R.color.avatar_background_11,
  R.color.avatar_background_12,
  R.color.avatar_background_13,
  R.color.avatar_background_14,
  R.color.avatar_background_15,
  R.color.avatar_background_16,
  R.color.avatar_background_17,
  R.color.avatar_background_18,
  R.color.avatar_background_19,
  R.color.avatar_background_20,
  R.color.avatar_background_21,
  R.color.avatar_background_22,
  R.color.avatar_background_23,
  R.color.avatar_background_24
)

/** The presenter for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val context: Context,
  private val logger: ConsoleLogger,
  private val viewModelProvider: ViewModelProvider<ProfileChooserViewModel>,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock
) {
  private lateinit var binding: ProfileChooserFragmentBinding
  private val orientation = Resources.getSystem().configuration.orientation

  val hasProfileEverBeenAddedValue = ObservableField<Boolean>(true)

  private val chooserViewModel: ProfileChooserViewModel by lazy {
    getProfileChooserViewModel()
  }

  /** Binds ViewModel and sets up RecyclerView Adapter. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    StatusBarColor.statusBarColorUpdate(R.color.profileStatusBar, activity, false)
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
    if (wasProfileEverBeenAddedResult.isFailure()) {
      logger.e(
        "ProfileChooserFragment",
        "Failed to retrieve the information on wasProfileEverBeenAdded",
        wasProfileEverBeenAddedResult.getErrorOrNull()!!
      )
    }
    return wasProfileEverBeenAddedResult.getOrDefault(/* defaultValue= */ false)
  }

  /** Randomly selects a color for the new profile that is not already in use. */
  private fun selectUniqueRandomColor(): Int {
    return COLORS_LIST.map {
      ContextCompat.getColor(context, it)
    }.minus(chooserViewModel.usedColors).random()
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileChooserViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserUiModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ProfileChooserUiModel, ProfileChooserUiModel.ModelTypeCase>(
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
      if (model.profile.pin.isEmpty()) {
        profileManagementController.loginToProfile(model.profile.id).toLiveData().observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              activity.startActivity(
                (
                  HomeActivity.createHomeActivity(
                    activity,
                    model.profile.id.internalId
                  )
                  )
              )
            }
          }
        )
      } else {
        val pinPasswordIntent = PinPasswordActivity.createPinPasswordActivityIntent(
          activity,
          chooserViewModel.adminPin,
          model.profile.id.internalId
        )
        activity.startActivity(pinPasswordIntent)
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
      activity.startActivity(
        AdminPinActivity.createAdminPinActivityIntent(
          activity,
          chooserViewModel.adminProfileId.internalId,
          selectUniqueRandomColor(),
          AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
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
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_PROFILE_CHOOSER,
      /* Event Context */ null
    )
  }
}
