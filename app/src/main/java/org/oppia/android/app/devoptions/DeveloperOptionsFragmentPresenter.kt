package org.oppia.android.app.devoptions

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.DeveloperOptionsAddAndDeleteProfilesBinding
import org.oppia.android.app.databinding.databinding.DeveloperOptionsFragmentBinding
import org.oppia.android.app.databinding.databinding.DeveloperOptionsModifyLessonProgressViewBinding
import org.oppia.android.app.databinding.databinding.DeveloperOptionsOverrideAppBehaviorsViewBinding
import org.oppia.android.app.databinding.databinding.DeveloperOptionsTestParsersViewBinding
import org.oppia.android.app.databinding.databinding.DeveloperOptionsViewLogsViewBinding
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsAddAndDeleteProfilesViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsTestParsersViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.ui.R
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
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

private val PRE_DEFINED_NAMES_LIST = listOf(
  "Ben", "Adhiambo", "Sean", "Saptak", "Vishwajit", "Subhajit", "Tanish", "Ayush", "Jinshu",
  "Eduard", "Kenneth", "Manas", "Mohit", "RD", "Oyindamola", "Sandeep", "Yash"
)

/** The presenter for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) {

  private lateinit var binding: DeveloperOptionsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager

  @Inject
  lateinit var developerOptionsViewModel: DeveloperOptionsViewModel

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    binding = DeveloperOptionsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    binding.developerOptionsList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    binding.apply {
      this.viewModel = developerOptionsViewModel
      this.lifecycleOwner = fragment
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<DeveloperOptionsItemViewModel> {
    return multiTypeBuilderFactory.create<DeveloperOptionsItemViewModel, ViewType> { viewModel ->
      when (viewModel) {
        is DeveloperOptionsModifyLessonProgressViewModel -> {
          viewModel.itemIndex.set(0)
          ViewType.VIEW_TYPE_MODIFY_LESSON_PROGRESS
        }
        is DeveloperOptionsViewLogsViewModel -> {
          viewModel.itemIndex.set(1)
          ViewType.VIEW_TYPE_VIEW_LOGS
        }
        is DeveloperOptionsOverrideAppBehaviorsViewModel -> {
          viewModel.itemIndex.set(2)
          ViewType.VIEW_TYPE_OVERRIDE_APP_BEHAVIORS
        }
        is DeveloperOptionsTestParsersViewModel -> {
          viewModel.itemIndex.set(3)
          ViewType.VIEW_TYPE_TEST_PARSERS
        }
        is DeveloperOptionsAddAndDeleteProfilesViewModel -> {
          viewModel.itemIndex.set(4)
          ViewType.VIEW_TYPE_ADD_AND_DELETE_PROFILES
        }
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_MODIFY_LESSON_PROGRESS,
        inflateDataBinding = DeveloperOptionsModifyLessonProgressViewBinding::inflate,
        setViewModel = DeveloperOptionsModifyLessonProgressViewBinding::setViewModel,
        transformViewModel = { it as DeveloperOptionsModifyLessonProgressViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_VIEW_LOGS,
        inflateDataBinding = DeveloperOptionsViewLogsViewBinding::inflate,
        setViewModel = DeveloperOptionsViewLogsViewBinding::setViewModel,
        transformViewModel = { it as DeveloperOptionsViewLogsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_OVERRIDE_APP_BEHAVIORS,
        inflateDataBinding = DeveloperOptionsOverrideAppBehaviorsViewBinding::inflate,
        setViewModel = DeveloperOptionsOverrideAppBehaviorsViewBinding::setViewModel,
        transformViewModel = { it as DeveloperOptionsOverrideAppBehaviorsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TEST_PARSERS,
        inflateDataBinding = DeveloperOptionsTestParsersViewBinding::inflate,
        setViewModel = DeveloperOptionsTestParsersViewBinding::setViewModel,
        transformViewModel = { it as DeveloperOptionsTestParsersViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_ADD_AND_DELETE_PROFILES,
        inflateDataBinding = DeveloperOptionsAddAndDeleteProfilesBinding::inflate,
        setViewModel = DeveloperOptionsAddAndDeleteProfilesBinding::setViewModel,
        transformViewModel = { it as DeveloperOptionsAddAndDeleteProfilesViewModel }
      )
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_MODIFY_LESSON_PROGRESS,
    VIEW_TYPE_VIEW_LOGS,
    VIEW_TYPE_OVERRIDE_APP_BEHAVIORS,
    VIEW_TYPE_TEST_PARSERS,
    VIEW_TYPE_ADD_AND_DELETE_PROFILES
  }

  /**
   * Called when the user clicks the 'delete all non-admin profiles' button.
   * This function deletes all non-admin profiles and navigates to [ProfileChooserActivity].
   */
  fun deleteAllNonAdminProfiles() {
    val deleteLiveData = profileManagementController.deleteAllNonAdminProfiles().toLiveData()
    deleteLiveData.observe(
      fragment,
      object : Observer<AsyncResult<Any?>> {
        override fun onChanged(result: AsyncResult<Any?>) {
          if (result is AsyncResult.Success) {
            deleteLiveData.removeObserver(this)
            val intent =
              Intent(fragment.requireContext(), ProfileChooserActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
              }
            fragment.startActivity(intent)
          } else if (result is AsyncResult.Failure) {
            deleteLiveData.removeObserver(this)
            oppiaLogger.e(
              "DeveloperOptionsFragment",
              "Failed to delete all non-admin profiles",
              result.error
            )
          }
        }
      }
    )
  }

  /**
   * Called when the user clicks the 'create one profile' or 'create three profiles' button.
   * This function invokes profile creation for the specified count and navigates to
   * [ProfileChooserActivity].
   *
   * @param count The number of profiles to create (either 1 or 3).
   */
  fun createProfile(count: Int) {
    val profilesLiveData = profileManagementController.getProfiles().toLiveData()
    profilesLiveData.observe(
      fragment,
      object : Observer<AsyncResult<List<Profile>>> {
        override fun onChanged(profilesResult: AsyncResult<List<Profile>>) {
          if (profilesResult is AsyncResult.Success) {
            profilesLiveData.removeObserver(this)
            val existingProfileNames = profilesResult.value.map { it.name }.toSet()
            val newNames = PRE_DEFINED_NAMES_LIST.filter {
              it !in existingProfileNames
            }.shuffled().take(count)
            newNames.forEach { newName ->
              val rgbColor = selectRandomColor()
              profileManagementController.addProfile(
                name = newName,
                pin = "",
                avatarImagePath = null,
                allowDownloadAccess = true,
                colorRgb = rgbColor,
                isAdmin = false
              )
            }
            val intent =
              Intent(fragment.requireContext(), ProfileChooserActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
              }
            fragment.startActivity(intent)
          } else if (profilesResult is AsyncResult.Failure) {
            profilesLiveData.removeObserver(this)
            oppiaLogger.e(
              "DeveloperOptionsFragment",
              "Failed to retrieve the list of profiles",
              profilesResult.error
            )
          }
        }
      }
    )
  }

  private fun selectRandomColor(): Int {
    return ContextCompat.getColor(activity, COLORS_LIST.random())
  }
}
