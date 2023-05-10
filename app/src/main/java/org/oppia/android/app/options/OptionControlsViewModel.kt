package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** [ViewModel] for [OptionsFragment]. */
private const val OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID =
  "OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID"
/** Options settings view model for the recycler view in [OptionsFragment]. */
@FragmentScope
class OptionControlsViewModel @Inject constructor(
  val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  @EnableLanguageSelectionUi private val enableLanguageSelectionUi: PlatformParameterValue<Boolean>,
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
  private var isFirstOpen = true
  /** Holds [Boolean] value showing if UI is initialized  */
  val uiLiveData = MutableLiveData<Boolean>()
  /** Holds the index for the currently selected fragment. */
  val selectedFragmentIndex = ObservableField<Int>()

  /**
   * Should be called with `false` when the UI starts to load, then with `true` after the UI
   * finishes loading.
   */
  fun isUIInitialized(isInitialized: Boolean) {
    uiLiveData.value = isInitialized
  }

  private val optionsItemViewModelProvider: DataProvider<List<OptionsItemViewModel>> by lazy {
    createOptionsItemViewModelProvider()
  }

  private fun createOptionsItemViewModelProvider(): DataProvider<List<OptionsItemViewModel>> {
    val profileProvider = profileManagementController.getProfile(profileId)
    val appLanguageProvider = translationController.getAppLanguage(profileId)
    return profileProvider.combineWith(
      appLanguageProvider, OPTIONS_ITEM_VIEW_MODEL_LIST_PROVIDER_ID
    ) { profile, oppiaLanguage ->

      val itemsList = arrayListOf<OptionsItemViewModel>()

      val optionsReadingTextSizeViewModel =
        OptionsReadingTextSizeViewModel(
          routeToReadingTextSizeListener, loadReadingTextSizeListener, resourceHandler
        )

      optionsReadingTextSizeViewModel.readingTextSize.set(profile.readingTextSize)

      val optionAudioViewViewModel =
        OptionsAudioLanguageViewModel(
          routeToAudioLanguageListListener,
          loadAudioLanguageListListener,
          profile.audioLanguage,
          resourceHandler.computeLocalizedDisplayName(profile.audioLanguage)
        )

      itemsList.add(optionsReadingTextSizeViewModel as OptionsItemViewModel)
      itemsList.add(optionAudioViewViewModel as OptionsItemViewModel)

      val optionsAppLanguageViewModel =
        OptionsAppLanguageViewModel(
          routeToAppLanguageListListener,
          loadAppLanguageListListener, oppiaLanguage,
          resourceHandler.computeLocalizedDisplayName(oppiaLanguage)
        )

      if (enableLanguageSelectionUi.value) {
        itemsList.add(1, optionsAppLanguageViewModel as OptionsItemViewModel)
      }

      // Loading the initial options in the sub-options container
      if (isMultipane.get()!! && isFirstOpen) {
        optionsReadingTextSizeViewModel.loadReadingTextSizeFragment()
        isFirstOpen = false
      }

      itemsList
    }
  }

  /**Sets the user ProfileId*/
  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  /**Options List data shown to the user.*/
  val optionsListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(optionsItemViewModelProvider.toLiveData(), ::processViewModelListsResult)
  }

  private fun processViewModelListsResult(
    asyncOptionsResult: AsyncResult<List<OptionsItemViewModel>>
  ): List<OptionsItemViewModel> {
    return when (asyncOptionsResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "OptionControlViewModel",
          "Failed to process results list", asyncOptionsResult.error
        )
        emptyList()
      }
      is AsyncResult.Pending -> {
        emptyList()
      }
      is AsyncResult.Success -> {
        asyncOptionsResult.value
      }
    }
  }

  /**
   * Used to set [isFirstOpen] value which controls the loading of the initial extra-option fragment
   * in the case of multipane.
   */
  fun isFirstOpen(isFirstOpen: Boolean) {
    this.isFirstOpen = isFirstOpen
  }
}
