package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val OPTIONS_ITEM_VIEW_MODEL_APP_AUDIO_LANGUAGE_PROVIDER_ID =
  "OPTIONS_ITEM_VIEW_MODEL_APP_AUDIO_LANGUAGE_PROVIDER_ID"
private const val OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID =
  "OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID"

/** Options settings view model for the recycler view in [OptionsFragment]. */
@FragmentScope
class OptionControlsViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) : OptionsItemViewModel() {
  private lateinit var profileId: ProfileId
  private val routeToReadingTextSizeListener = activity as RouteToReadingTextSizeListener
  private val routeToAudioLanguageListListener = activity as RouteToAudioLanguageListListener
  private val routeToAppLanguageListListener = activity as RouteToAppLanguageListListener
  private val loadReadingTextSizeListener = activity as LoadReadingTextSizeListener
  private val loadAudioLanguageListListener = activity as LoadAudioLanguageListListener
  private val loadAppLanguageListListener = activity as LoadAppLanguageListListener

  private val optionsItemViewModelProvider by lazy { createOptionsItemViewModelProvider() }

  /** Holds [Boolean] value showing if UI is initialized. */
  val uiLiveData = MutableLiveData<Boolean>()
  /** Holds the index for the currently selected fragment. */
  val selectedFragmentIndex = ObservableField<Int>()

  /** Options List data shown to the user. */
  val optionsListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(optionsItemViewModelProvider.toLiveData(), ::processViewModelListsResult)
  }

  /**
   * Should be called with `false` when the UI starts to load, then with `true` after the UI
   * finishes loading.
   */
  fun isUIInitialized(isInitialized: Boolean) {
    uiLiveData.value = isInitialized
  }

  /** Sets the user's ProfileId value in this ViewModel. */
  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  private fun createOptionsItemViewModelProvider(): DataProvider<List<OptionsItemViewModel>> {
    val appAudioLangProvider =
      translationController.getAppLanguage(profileId).combineWith(
        profileManagementController.getAudioLanguage(profileId),
        OPTIONS_ITEM_VIEW_MODEL_APP_AUDIO_LANGUAGE_PROVIDER_ID
      ) { appLanguage, audioLanguage -> appLanguage to audioLanguage }
    return profileManagementController.getProfile(profileId).combineWith(
      appAudioLangProvider, OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID
    ) { profile, (appLang, audioLang) -> processViewModelList(profile, appLang, audioLang) }
  }

  private fun processViewModelListsResult(
    asyncOptionsResult: AsyncResult<List<OptionsItemViewModel>>
  ): List<OptionsItemViewModel> {
    return when (asyncOptionsResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "OptionControlViewModel",
          "Failed to process results list",
          asyncOptionsResult.error
        )
        emptyList()
      }
      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> asyncOptionsResult.value
    }
  }

  private fun processViewModelList(
    profile: Profile,
    appLanguage: OppiaLanguage,
    audioLanguage: AudioLanguage
  ): List<OptionsItemViewModel> {
    return listOfNotNull(
      createReadingTextSizeViewModel(profile),
      createAppLanguageViewModel(appLanguage),
      createAudioLanguageViewModel(audioLanguage)
    )
  }

  private fun createReadingTextSizeViewModel(profile: Profile): OptionsReadingTextSizeViewModel {
    return OptionsReadingTextSizeViewModel(
      routeToReadingTextSizeListener, loadReadingTextSizeListener, resourceHandler
    ).also { it.readingTextSize.set(profile.readingTextSize) }
  }

  private fun createAppLanguageViewModel(language: OppiaLanguage): OptionsAppLanguageViewModel? {
    return OptionsAppLanguageViewModel(
      routeToAppLanguageListListener,
      loadAppLanguageListListener,
      language,
      resourceHandler.computeLocalizedDisplayName(language)
    )
  }

  private fun createAudioLanguageViewModel(
    audioLanguage: AudioLanguage
  ): OptionsAudioLanguageViewModel {
    return OptionsAudioLanguageViewModel(
      routeToAudioLanguageListListener,
      loadAudioLanguageListListener,
      audioLanguage,
      resourceHandler.computeLocalizedDisplayName(audioLanguage)
    )
  }
}
