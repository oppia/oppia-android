package org.oppia.android.app.devoptions.featureflags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.databinding.databinding.FeatureFlagsFragmentBinding
import org.oppia.android.app.databinding.databinding.FeatureFlagsItemBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.FeatureFlagId
import org.oppia.android.app.model.OverriddenFeatureFlag
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.platformparameter.PlatformParameterControllerDebugImpl
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [FeatureFlagsFragment]. */
@FragmentScope
class FeatureFlagsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val featureFlagsViewModel: FeatureFlagsViewModel,
  private val oppiaLogger: OppiaLogger,
  private val platformParameterControllerDebugImpl: PlatformParameterControllerDebugImpl,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {
  private lateinit var binding: FeatureFlagsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var bindingAdapter: BindableAdapter<FeatureFlagItemViewModel>

  /** List of feature flag switch states to be used in the fragment. */
  var featureFlagStates: MutableMap<FeatureFlagId, Boolean> = mutableMapOf()

  /** Called when [FeatureFlagsFragment] is created. Handles UI for the fragment. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    featureFlagStates: Map<FeatureFlagId, Boolean>
  ): View {
    binding = FeatureFlagsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.featureFlagsToolbar.setNavigationOnClickListener {
      onBackNavigation()
    }

    activity.onBackPressedDispatcher.addCallback(
      fragment,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          onBackNavigation()
          // The dispatcher can hold a reference to the host
          // so we need to null it out to prevent memory leaks.
          this.remove()
        }
      }
    )

    if (featureFlagStates.isNotEmpty()) {
      this.featureFlagStates = featureFlagStates.toMutableMap()
    }
    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = featureFlagsViewModel
    }
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    bindingAdapter = createRecyclerViewAdapter()
    binding.featureFlagsRecyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = bindingAdapter
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<FeatureFlagItemViewModel> {
    return singleTypeBuilderFactory.create<FeatureFlagItemViewModel>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = FeatureFlagsItemBinding::inflate,
        setViewModel = this::bindFeatureFlagItem
      )
      .build()
  }

  private fun onBackNavigation() {
    val overriddenFeatureFlags = featureFlagStates.map { (id, value) ->
      OverriddenFeatureFlag.newBuilder()
        .setId(id)
        .setOverriddenValue(value)
        .build()
    }

    platformParameterControllerDebugImpl
      .updateOverriddenFeatureFlags(overriddenFeatureFlags).toLiveData().observe(fragment) {
        when (it) {
          is AsyncResult.Success -> (activity as FeatureFlagsActivity).finish()
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "PlatformParametersFragmentPresenter",
              "Failed to override feature flags: ",
              it.error
            )
          }
          is AsyncResult.Pending -> {} // Wait for a result.
        }
      }
  }

  private fun bindFeatureFlagItem(
    binding: FeatureFlagsItemBinding,
    model: FeatureFlagItemViewModel
  ) {
    binding.viewModel = model

    if (featureFlagStates.containsKey(model.featureFlagId)) {
      model.isChecked.set(featureFlagStates[model.featureFlagId])
    }
    model.onFeatureFlagToggleCallback = { id, value ->
      if (model.currentValue == value) {
        featureFlagStates.remove(id)
      } else {
        featureFlagStates[id] = value
      }
    }
  }

  private fun handleBackPress() {
  }
}
