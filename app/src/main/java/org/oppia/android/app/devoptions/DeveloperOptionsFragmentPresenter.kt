package org.oppia.android.app.devoptions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DeveloperOptionsFragmentBinding
import org.oppia.android.databinding.DeveloperOptionsModifyLessonProgressViewBinding
import org.oppia.android.databinding.DeveloperOptionsOverrideAppBehaviorsViewBinding
import org.oppia.android.databinding.DeveloperOptionsViewLogsViewBinding
import java.security.InvalidParameterException
import javax.inject.Inject

/** The presenter for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {

  private lateinit var binding: DeveloperOptionsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private var internalProfileId: Int = -1

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

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    developerOptionsViewModel.setInternalProfileId(internalProfileId)

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

  private fun createRecyclerViewAdapter():
    BindableAdapter<DeveloperOptionsItemViewModel> {
      return BindableAdapter.MultiTypeBuilder
        .newBuilder<DeveloperOptionsItemViewModel, ViewType> { viewModel ->
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
        .build()
    }

  fun setSelectedFragment(selectedFragment: String) {
    developerOptionsViewModel.selectedFragmentIndex.set(
      getSelectedFragmentIndex(
        selectedFragment
      )
    )
  }

  private fun getSelectedFragmentIndex(selectedFragment: String): Int {
    return when (selectedFragment) {
      MARK_CHAPTERS_COMPLETED_FRAGMENT -> 0
      MARK_STORIES_COMPLETED_FRAGMENT -> 1
      MARK_TOPICS_COMPLETED_FRAGMENT -> 2
      EVENT_LOGS_FRAGMENT -> 3
      FORCE_NETWORK_TYPE_FRAGMENT -> 4
      else -> throw InvalidParameterException("Not a valid fragment in getSelectedFragmentIndex.")
    }
  }

  private enum class ViewType {
    VIEW_TYPE_MODIFY_LESSON_PROGRESS,
    VIEW_TYPE_VIEW_LOGS,
    VIEW_TYPE_OVERRIDE_APP_BEHAVIORS
  }
}
