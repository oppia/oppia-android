package org.oppia.app.options

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.databinding.OptionAppLanguageBinding
import org.oppia.app.databinding.OptionAudioLanguageBinding
import org.oppia.app.databinding.OptionStoryTextSizeBinding
import org.oppia.app.databinding.OptionsFragmentBinding
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.ProfileId
import org.oppia.app.model.ReadingTextSize
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

const val READING_TEXT_SIZE = "READING_TEXT_SIZE"
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
  private val consoleLogger: ConsoleLogger
) {
  private lateinit var binding: OptionsFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private var readingTextSize = ReadingTextSize.SMALL_TEXT_SIZE
  private var appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
  private var audioLanguage = AudioLanguage.NO_AUDIO
  private val viewModel = getOptionControlsItemViewModel()

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    isFirstOpen: Boolean
  ): View? {
    viewModel.isUIInitialized(false)
    viewModel.isFirstOpen(isFirstOpen)
    viewModel.isMultipane.set(isMultipane)
    binding = OptionsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
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
    viewModel.isUIInitialized(true)
    return binding.root
  }

  private fun createRecyclerViewAdapter(isMultipane: Boolean): BindableAdapter<OptionsItemViewModel> { // ktlint-disable max-line-length
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<OptionsItemViewModel, ViewType> { viewModel ->
        viewModel.isMultipane.set(isMultipane)
        when (viewModel) {
          is OptionsReadingTextSizeViewModel -> ViewType.VIEW_TYPE_READING_TEXT_SIZE
          is OptionsAppLanguageViewModel -> ViewType.VIEW_TYPE_APP_LANGUAGE
          is OptionsAudioLanguageViewModel -> ViewType.VIEW_TYPE_AUDIO_LANGUAGE
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
    binding.viewModel = model
  }

  private fun bindAppLanguage(
    binding: OptionAppLanguageBinding,
    model: OptionsAppLanguageViewModel
  ) {
    binding.viewModel = model
  }

  private fun bindAudioLanguage(
    binding: OptionAudioLanguageBinding,
    model: OptionsAudioLanguageViewModel
  ) {
    binding.viewModel = model
  }

  private fun getOptionControlsItemViewModel(): OptionControlsViewModel {
    return viewModelProvider.getForFragment(fragment, OptionControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_READING_TEXT_SIZE,
    VIEW_TYPE_APP_LANGUAGE,
    VIEW_TYPE_AUDIO_LANGUAGE
  }

  fun updateReadingTextSize(textSize: String) {
    when (textSize) {
      getOptionControlsItemViewModel().getReadingTextSize(ReadingTextSize.SMALL_TEXT_SIZE) -> {
        profileManagementController.updateReadingTextSize(
          profileId,
          ReadingTextSize.SMALL_TEXT_SIZE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              readingTextSize = ReadingTextSize.SMALL_TEXT_SIZE
            } else {
              consoleLogger.e(
                READING_TEXT_SIZE_TAG,
                "$READING_TEXT_SIZE_ERROR: small text size",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getReadingTextSize(ReadingTextSize.MEDIUM_TEXT_SIZE) -> {
        profileManagementController.updateReadingTextSize(
          profileId,
          ReadingTextSize.MEDIUM_TEXT_SIZE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              readingTextSize = ReadingTextSize.MEDIUM_TEXT_SIZE
            } else {
              consoleLogger.e(
                READING_TEXT_SIZE_TAG,
                "$READING_TEXT_SIZE_ERROR: medium text size",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getReadingTextSize(ReadingTextSize.LARGE_TEXT_SIZE) -> {
        profileManagementController.updateReadingTextSize(
          profileId,
          ReadingTextSize.LARGE_TEXT_SIZE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              readingTextSize = ReadingTextSize.LARGE_TEXT_SIZE
            } else {
              consoleLogger.e(
                READING_TEXT_SIZE_TAG,
                "$READING_TEXT_SIZE_ERROR: large text size",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel()
        .getReadingTextSize(ReadingTextSize.EXTRA_LARGE_TEXT_SIZE) -> {
        profileManagementController.updateReadingTextSize(
          profileId,
          ReadingTextSize.EXTRA_LARGE_TEXT_SIZE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              readingTextSize = ReadingTextSize.EXTRA_LARGE_TEXT_SIZE
            } else {
              consoleLogger.e(
                READING_TEXT_SIZE_TAG,
                "$READING_TEXT_SIZE_ERROR: extra large text size",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
    }
    recyclerViewAdapter.notifyItemChanged(0)
  }

  fun updateAppLanguage(language: String) {
    when (language) {
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.ENGLISH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.ENGLISH_APP_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE
            } else {
              consoleLogger.e(
                APP_LANGUAGE_TAG,
                "$APP_LANGUAGE_ERROR: English",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.HINDI_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.HINDI_APP_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              appLanguage = AppLanguage.HINDI_APP_LANGUAGE
            } else {
              consoleLogger.e(
                APP_LANGUAGE_TAG,
                "$APP_LANGUAGE_ERROR: Hindi",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.CHINESE_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.CHINESE_APP_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              appLanguage = AppLanguage.CHINESE_APP_LANGUAGE
            } else {
              consoleLogger.e(
                APP_LANGUAGE_TAG,
                "$APP_LANGUAGE_ERROR: Chinese",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAppLanguage(AppLanguage.FRENCH_APP_LANGUAGE) -> {
        profileManagementController.updateAppLanguage(
          profileId,
          AppLanguage.FRENCH_APP_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              appLanguage = AppLanguage.FRENCH_APP_LANGUAGE
            } else {
              consoleLogger.e(
                APP_LANGUAGE_TAG,
                "$APP_LANGUAGE_ERROR: French",
                it.getErrorOrNull()
              )
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
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              audioLanguage = AudioLanguage.NO_AUDIO
            } else {
              consoleLogger.e(
                AUDIO_LANGUAGE_TAG,
                "$AUDIO_LANGUAGE_ERROR: No Audio",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.ENGLISH_AUDIO_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
            } else {
              consoleLogger.e(
                AUDIO_LANGUAGE_TAG,
                "$AUDIO_LANGUAGE_ERROR: English",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.HINDI_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.HINDI_AUDIO_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              audioLanguage = AudioLanguage.HINDI_AUDIO_LANGUAGE
            } else {
              consoleLogger.e(
                AUDIO_LANGUAGE_TAG,
                "$AUDIO_LANGUAGE_ERROR: Hindi",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.CHINESE_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.CHINESE_AUDIO_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              audioLanguage = AudioLanguage.CHINESE_AUDIO_LANGUAGE
            } else {
              consoleLogger.e(
                AUDIO_LANGUAGE_TAG,
                "$AUDIO_LANGUAGE_ERROR: Chinese",
                it.getErrorOrNull()
              )
            }
          }
        )
      }
      getOptionControlsItemViewModel().getAudioLanguage(AudioLanguage.FRENCH_AUDIO_LANGUAGE) -> {
        profileManagementController.updateAudioLanguage(
          profileId,
          AudioLanguage.FRENCH_AUDIO_LANGUAGE
        ).observe(
          fragment,
          Observer {
            if (it.isSuccess()) {
              audioLanguage = AudioLanguage.FRENCH_AUDIO_LANGUAGE
            } else {
              consoleLogger.e(
                AUDIO_LANGUAGE_TAG,
                "$AUDIO_LANGUAGE_ERROR: French",
                it.getErrorOrNull()
              )
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
