package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.OptionAppLanguageBinding
import org.oppia.android.databinding.OptionAudioLanguageBinding
import org.oppia.android.databinding.OptionStoryTextSizeBinding
import org.oppia.android.databinding.OptionsFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import java.security.InvalidParameterException
import javax.inject.Inject

private const val READING_TEXT_SIZE_TAG = "ReadingTextSize"
private const val APP_LANGUAGE_TAG = "OptionsFragmentPresenter"
private const val AUDIO_LANGUAGE_TAG = "AudioLanguage"
private const val READING_TEXT_SIZE_ERROR =
  "Something went wrong while updating the reading text size"
private const val APP_LANGUAGE_ERROR =
  "Something went wrong while updating the app language"
private const val AUDIO_LANGUAGE_ERROR =
  "Something went wrong while updating the audio language"

/** The presenter for [OptionsFragment]. */
@FragmentScope
class OptionsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileManagementController: ProfileManagementController,
  private val optionControlsViewModel: OptionControlsViewModel,
  private val oppiaLogger: OppiaLogger,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
  private val translationController: TranslationController
) {
  private lateinit var binding: OptionsFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var appLanguage = OppiaLanguage.ENGLISH
  private var audioLanguage = AudioLanguage.NO_AUDIO

  /** Initializes and creates the views for [OptionsFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    isFirstOpen: Boolean,
    selectedFragment: String
  ): View? {
    optionControlsViewModel.isUIInitialized(false)
    optionControlsViewModel.isMultipane.set(isMultipane)
    binding = OptionsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    internalProfileId = activity.intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: -1
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    optionControlsViewModel.setProfileId(profileId)

    val optionsRecyclerViewAdapter = createRecyclerViewAdapter(isMultipane)
    binding.optionsRecyclerview.apply {
      adapter = optionsRecyclerViewAdapter
    }
    recyclerViewAdapter = optionsRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = optionControlsViewModel
    }
    setSelectedFragment(selectedFragment)
    optionControlsViewModel.isUIInitialized(true)

    var hasDefaultInitializedFragment = false
    optionControlsViewModel.optionsListLiveData.observe(fragment) { viewModels ->
      if (!hasDefaultInitializedFragment) {
        viewModels.filterIsInstance<OptionsReadingTextSizeViewModel>().singleOrNull()?.let {
          if (isMultipane && isFirstOpen) {
            it.loadReadingTextSizeFragment()
          }
          hasDefaultInitializedFragment = true
        }
      }
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(
    isMultipane: Boolean
  ): BindableAdapter<OptionsItemViewModel> {
    return multiTypeBuilderFactory.create<OptionsItemViewModel, ViewType> { viewModel ->
      viewModel.isMultipane.set(isMultipane)
      when (viewModel) {
        is OptionsReadingTextSizeViewModel -> {
          viewModel.itemIndex.set(0)
          ViewType.VIEW_TYPE_READING_TEXT_SIZE
        }
        is OptionsAppLanguageViewModel -> {
          viewModel.itemIndex.set(1)
          ViewType.VIEW_TYPE_APP_LANGUAGE
        }
        is OptionsAudioLanguageViewModel -> {
          viewModel.itemIndex.set(2)
          ViewType.VIEW_TYPE_AUDIO_LANGUAGE
        }
        else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
      }
    }.registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_READING_TEXT_SIZE,
      inflateDataBinding = OptionStoryTextSizeBinding::inflate,
      setViewModel = this::bindReadingTextSize,
      transformViewModel = { it as OptionsReadingTextSizeViewModel }
    ).registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_APP_LANGUAGE,
      inflateDataBinding = OptionAppLanguageBinding::inflate,
      setViewModel = this::bindAppLanguage,
      transformViewModel = { it as OptionsAppLanguageViewModel }
    ).registerViewDataBinder(
      viewType = ViewType.VIEW_TYPE_AUDIO_LANGUAGE,
      inflateDataBinding = OptionAudioLanguageBinding::inflate,
      setViewModel = this::bindAudioLanguage,
      transformViewModel = { it as OptionsAudioLanguageViewModel }
    ).build()
  }

  private fun bindReadingTextSize(
    binding: OptionStoryTextSizeBinding,
    model: OptionsReadingTextSizeViewModel
  ) {
    binding.commonViewModel = optionControlsViewModel
    binding.viewModel = model
  }

  private fun bindAppLanguage(
    binding: OptionAppLanguageBinding,
    model: OptionsAppLanguageViewModel
  ) {
    binding.commonViewModel = optionControlsViewModel
    binding.viewModel = model
  }

  private fun bindAudioLanguage(
    binding: OptionAudioLanguageBinding,
    model: OptionsAudioLanguageViewModel
  ) {
    binding.commonViewModel = optionControlsViewModel
    binding.viewModel = model
  }

  /** Sets the selected fragment index in [OptionsControlViewModel]. */
  fun setSelectedFragment(selectedFragment: String) {
    optionControlsViewModel.selectedFragmentIndex.set(
      getSelectedFragmentIndex(
        selectedFragment
      )
    )
  }

  private fun getSelectedFragmentIndex(selectedFragment: String): Int {
    return when (selectedFragment) {
      READING_TEXT_SIZE_FRAGMENT -> 0
      APP_LANGUAGE_FRAGMENT -> 1
      AUDIO_LANGUAGE_FRAGMENT -> 2
      else -> throw InvalidParameterException("Not a valid fragment in getSelectedFragmentIndex.")
    }
  }

  private enum class ViewType {
    /** Represents view type for displaying [ReadingTextSize]. */
    VIEW_TYPE_READING_TEXT_SIZE,

    /** Represents view type for displaying [OppiaLanguage]. */
    VIEW_TYPE_APP_LANGUAGE,

    /** Represents view type for displaying [AudioLanguage]. */
    VIEW_TYPE_AUDIO_LANGUAGE
  }

  /**
   * Updates [ReadingTextSize] value in [OptionsFragment] when user selects new value and notifies
   * the adapter to refresh after the changes.
   *
   * @param textSize new textSize to be set as current
   */
  fun updateReadingTextSize(textSize: ReadingTextSize) {
    val sizeUpdateResult = profileManagementController.updateReadingTextSize(profileId, textSize)
    sizeUpdateResult.toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            READING_TEXT_SIZE_TAG, "$READING_TEXT_SIZE_ERROR: updating to $textSize", it.error
          )
        }
        else -> {} // Nothing needs to be done unless the update failed.
      }
    }
    recyclerViewAdapter.notifyItemChanged(0)
  }

  /**
   * Updates [OppiaLanguage] value in [OptionsFragment] when user selects new value and notifies the
   * adapter to refresh after the changes.
   *
   * @param oppiaLanguage new oppiaLanguage to be set as current
   */
  fun updateAppLanguage(oppiaLanguage: OppiaLanguage) {
    val selection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = oppiaLanguage
      selectedLanguageValue = oppiaLanguage.number
    }.build()

    translationController.updateAppLanguage(profileId, selection).toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Success -> appLanguage = oppiaLanguage
        is AsyncResult.Failure ->
          oppiaLogger.e(APP_LANGUAGE_TAG, "$APP_LANGUAGE_ERROR: $oppiaLanguage.", it.error)
        is AsyncResult.Pending -> {} // Wait for a result.
      }
    }

    recyclerViewAdapter.notifyItemChanged(1)
  }

  /**
   * Updates [AudioLanguage] value in [OptionsFragment] when user selects new value and notifies the
   * adapter to refresh after the changes.
   *
   * @param audioLanguage new audioLanguage to be set as current
   */
  fun updateAudioLanguage(audioLanguage: AudioLanguage) {
    val updateLanguageResult =
      profileManagementController.updateAudioLanguage(profileId, audioLanguage)
    updateLanguageResult.toLiveData().observe(fragment) {
      when (it) {
        is AsyncResult.Success -> this.audioLanguage = audioLanguage
        is AsyncResult.Failure ->
          oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: $audioLanguage", it.error)
        is AsyncResult.Pending -> {} // Wait for a result.
      }
    }

    recyclerViewAdapter.notifyItemChanged(2)
  }

  /**
   * Used to fix the race condition that happens when the presenter tries to call a function before
   * [handleCreateView] is completely executed.
   *
   * @param action what to execute after the UI is initialized.
   */
  fun runAfterUIInitialization(action: () -> Unit) {
    optionControlsViewModel.uiLiveData.observe(fragment) {
      if (it) {
        action.invoke()
      }
    }
  }
}
