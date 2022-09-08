package org.oppia.android.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguage
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.OptionAppLanguageBinding
import org.oppia.android.databinding.OptionAudioLanguageBinding
import org.oppia.android.databinding.OptionStoryTextSizeBinding
import org.oppia.android.databinding.OptionsFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import java.security.InvalidParameterException
import javax.inject.Inject

const val APP_LANGUAGE = "APP_LANGUAGE"
const val AUDIO_LANGUAGE = "AUDIO_LANGUAGE"
private const val READING_TEXT_SIZE_TAG = "ReadingTextSize"
private const val APP_LANGUAGE_TAG = "AppLanguage"
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
  private val viewModelProvider: ViewModelProvider<OptionControlsViewModel>,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var binding: OptionsFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO
  private val viewModel = getOptionControlsItemViewModel()

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    isFirstOpen: Boolean,
    selectedFragment: String
  ): View? {
    viewModel.isUIInitialized(false)
    viewModel.isFirstOpen(isFirstOpen)
    viewModel.isMultipane.set(isMultipane)
    binding = OptionsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    internalProfileId = activity.intent.getIntExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    viewModel.setProfileId(profileId)

    val optionsRecyclerViewAdapter = createRecyclerViewAdapter(isMultipane)
    binding.optionsRecyclerview.apply {
      adapter = optionsRecyclerViewAdapter
    }
    recyclerViewAdapter = optionsRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = viewModel
    }
    setSelectedFragment(selectedFragment)
    viewModel.isUIInitialized(true)
    return binding.root
  }

  private fun createRecyclerViewAdapter(
    isMultipane: Boolean
  ): BindableAdapter<OptionsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<OptionsItemViewModel, ViewType> { viewModel ->
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
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_READING_TEXT_SIZE,
        inflateDataBinding = OptionStoryTextSizeBinding::inflate,
        setViewModel = this::bindReadingTextSize,
        transformViewModel = { it as OptionsReadingTextSizeViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_APP_LANGUAGE,
        inflateDataBinding = OptionAppLanguageBinding::inflate,
        setViewModel = this::bindAppLanguage,
        transformViewModel = { it as OptionsAppLanguageViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_AUDIO_LANGUAGE,
        inflateDataBinding = OptionAudioLanguageBinding::inflate,
        setViewModel = this::bindAudioLanguage,
        transformViewModel = { it as OptionsAudioLanguageViewModel }
      )
      .build()
  }

  private fun bindReadingTextSize(
    binding: OptionStoryTextSizeBinding,
    model: OptionsReadingTextSizeViewModel
  ) {
    binding.commonViewModel = viewModel
    binding.viewModel = model
  }

  private fun bindAppLanguage(
    binding: OptionAppLanguageBinding,
    model: OptionsAppLanguageViewModel
  ) {
    binding.commonViewModel = viewModel
    binding.viewModel = model
  }

  private fun bindAudioLanguage(
    binding: OptionAudioLanguageBinding,
    model: OptionsAudioLanguageViewModel
  ) {
    binding.commonViewModel = viewModel
    binding.viewModel = model
  }

  fun setSelectedFragment(selectedFragment: String) {
    viewModel.selectedFragmentIndex.set(
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

  private fun getOptionControlsItemViewModel(): OptionControlsViewModel {
    return viewModelProvider.getForFragment(fragment, OptionControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_READING_TEXT_SIZE,
    VIEW_TYPE_APP_LANGUAGE,
    VIEW_TYPE_AUDIO_LANGUAGE
  }

  fun updateReadingTextSize(textSize: ReadingTextSize) {
    profileManagementController.updateReadingTextSize(profileId, textSize).toLiveData().observe(
      fragment,
      {
        when (it) {
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              READING_TEXT_SIZE_TAG, "$READING_TEXT_SIZE_ERROR: updating to $textSize", it.error
            )
          }
          else -> {} // Nothing needs to be done unless the update failed.
        }
      }
    )
    recyclerViewAdapter.notifyItemChanged(0)
  }

  fun updateAppLanguage(language: String) {
    when (language) {
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.ENGLISH_APP_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(APP_LANGUAGE_TAG, "$APP_LANGUAGE_ERROR: English", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.HINDI_APP_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> appLanguage = AppLanguage.HINDI_APP_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(APP_LANGUAGE_TAG, "$APP_LANGUAGE_ERROR: Hindi", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.CHINESE_APP_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> appLanguage = AppLanguage.CHINESE_APP_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(APP_LANGUAGE_TAG, "$APP_LANGUAGE_ERROR: Chinese", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.FRENCH_APP_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> appLanguage = AppLanguage.FRENCH_APP_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(APP_LANGUAGE_TAG, "$APP_LANGUAGE_ERROR: French", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
    }

    recyclerViewAdapter.notifyItemChanged(1)
  }

  fun updateAudioLanguage(language: String) {
    when (language) {
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.NO_AUDIO) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.NO_AUDIO
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> audioLanguage = AudioLanguage.NO_AUDIO
              is AsyncResult.Failure ->
                oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: No Audio", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.ENGLISH_AUDIO_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: English", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.HINDI_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.HINDI_AUDIO_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> audioLanguage = AudioLanguage.HINDI_AUDIO_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: Hindi", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.CHINESE_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.CHINESE_AUDIO_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> audioLanguage = AudioLanguage.CHINESE_AUDIO_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: Chinese", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.FRENCH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.FRENCH_AUDIO_LANGUAGE
        ).toLiveData().observe(
          fragment,
          Observer {
            when (it) {
              is AsyncResult.Success -> audioLanguage = AudioLanguage.FRENCH_AUDIO_LANGUAGE
              is AsyncResult.Failure ->
                oppiaLogger.e(AUDIO_LANGUAGE_TAG, "$AUDIO_LANGUAGE_ERROR: French", it.error)
              is AsyncResult.Pending -> {} // Wait for a result.
            }
          }
        )
      }
    }

    recyclerViewAdapter.notifyItemChanged(2)
  }

  /**
   * Used to fix the race condition that happens when the presenter tries to call a function before
   * [handleCreateView] is completely executed.
   * @param action what to execute after the UI is initialized.
   */
  fun runAfterUIInitialization(action: () -> Unit) {
    viewModel.uiLiveData.observe(
      fragment,
      Observer {
        if (it) {
          action.invoke()
        }
      }
    )
  }
}
