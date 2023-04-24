package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableLanguageSelectionUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** [ViewModel] for [OptionsFragment]. */
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
  val uiLiveData = MutableLiveData<Boolean>()
  val selectedFragmentIndex = ObservableField<Int>()

  private val itemViewModelList: ObservableList<OptionsItemViewModel> = ObservableArrayList()
  private val _optionList = MutableLiveData<List<OptionsItemViewModel>>()
  private val optionList: LiveData<List<OptionsItemViewModel>>
    get() = _optionList

  /**
   * Should be called with `false` when the UI starts to load, then with `true` after the UI
   * finishes loading.
   */
  fun isUIInitialized(isInitialized: Boolean) {
    uiLiveData.value = isInitialized
  }

  val optionListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(), ::processProfileResult
    )
  }

  val languageListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(
      translationController.getAppLanguage(profileId).toLiveData(), ::processAppLanguageResult
    )
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  fun computeOptionsItemList(
    optionListData: LiveData<List<OptionsItemViewModel>>,
    languageListData: LiveData<List<OptionsItemViewModel>>
  ): LiveData<List<OptionsItemViewModel>> {
    if (itemViewModelList.isEmpty()) {
      val mergedDataList = combine(optionListData, languageListData)
      mergedDataList.value?.first?.let { itemViewModelList.addAll(it) }
      if (enableLanguageSelectionUi.value) {
        mergedDataList.value?.second?.let { itemViewModelList.add(1, it[0]) }
      }
      _optionList.value = itemViewModelList
    }

    return optionList
  }

  /**
   * This Combines the 2 LiveData objects into a Pair, emitting only when both sources are non-null.
   */
  private fun <A, B> combine(a: LiveData<A>, b: LiveData<B>): LiveData<Pair<A, B>> {
    return MediatorLiveData<Pair<A, B>>().apply {
      fun combine() {
        val aValue = a.value
        val bValue = b.value
        if (aValue != null && bValue != null) {
          this.value = (Pair(aValue, bValue))
        }
      }

      addSource(a) { combine() }
      addSource(b) { combine() }

      combine()
    }
  }

  private fun processAppLanguageResult(
    oppiaLanguage: AsyncResult<OppiaLanguage>
  ): List<OptionsItemViewModel> {
    return when (oppiaLanguage) {
      is AsyncResult.Failure -> {
        emptyList()
      }
      is AsyncResult.Pending -> {
        emptyList()
      }
      is AsyncResult.Success -> {
        val appLanguageSelection =
          AppLanguageSelection.newBuilder().apply { selectedLanguage = oppiaLanguage.value }.build()

        val itemsList = arrayListOf<OptionsItemViewModel>()
        val optionsAppLanguageViewModel =
          OptionsAppLanguageViewModel(
            routeToAppLanguageListListener,
            loadAppLanguageListListener, appLanguageSelection.selectedLanguage,
            resourceHandler.computeLocalizedDisplayName(appLanguageSelection.selectedLanguage)
          )

        if (enableLanguageSelectionUi.value) {
          itemsList.add(optionsAppLanguageViewModel as OptionsItemViewModel)
        }

        itemsList
      }
    }
  }

  private fun processProfileResult(profile: AsyncResult<Profile>): List<OptionsItemViewModel> {
    return when (profile) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("OptionsFragment", "Failed to retrieve profile", profile.error)
        emptyList()
      }
      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> {
        val itemsList = arrayListOf<OptionsItemViewModel>()

        val optionsReadingTextSizeViewModel =
          OptionsReadingTextSizeViewModel(
            routeToReadingTextSizeListener, loadReadingTextSizeListener, resourceHandler
          )

        val optionAudioViewViewModel =
          OptionsAudioLanguageViewModel(
            routeToAudioLanguageListListener,
            loadAudioLanguageListListener,
            profile.value.audioLanguage,
            resourceHandler.computeLocalizedDisplayName(profile.value.audioLanguage)
          )

        optionsReadingTextSizeViewModel.readingTextSize.set(profile.value.readingTextSize)

        itemsList.add(optionsReadingTextSizeViewModel as OptionsItemViewModel)
        itemsList.add(optionAudioViewViewModel as OptionsItemViewModel)

        // Loading the initial options in the sub-options container
        if (isMultipane.get()!! && isFirstOpen) {
          optionsReadingTextSizeViewModel.loadReadingTextSizeFragment()
          isFirstOpen = false
        }

        return itemsList
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

  companion object {
    var isResuming: Boolean = false
  }
}
