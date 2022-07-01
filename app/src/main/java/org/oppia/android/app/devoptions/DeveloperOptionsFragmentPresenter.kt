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
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsTestParsersViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.DeveloperOptionsFragmentBinding
import org.oppia.android.databinding.DeveloperOptionsModifyLessonProgressViewBinding
import org.oppia.android.databinding.DeveloperOptionsOverrideAppBehaviorsViewBinding
import org.oppia.android.databinding.DeveloperOptionsTestParsersViewBinding
import org.oppia.android.databinding.DeveloperOptionsViewLogsViewBinding
import javax.inject.Inject

/** The presenter for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
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
    return BindableAdapter.MultiTypeBuilder
      .Factory(fragment).create<DeveloperOptionsItemViewModel, ViewType> { viewModel ->
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
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_MODIFY_LESSON_PROGRESS,
    VIEW_TYPE_VIEW_LOGS,
    VIEW_TYPE_OVERRIDE_APP_BEHAVIORS,
    VIEW_TYPE_TEST_PARSERS
  }
}
