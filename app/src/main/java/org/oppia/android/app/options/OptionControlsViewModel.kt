package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
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
  private val resourceHandler: AppLanguageResourceHandler
) : OptionsItemViewModel() {
  private val itemViewModelList: ObservableList<OptionsItemViewModel> = ObservableArrayList()
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

  /**
   * Should be called with `false` when the UI starts to load, then with `true` after the UI
   * finishes loading.
   */
  fun isUIInitialized(isInitialized: Boolean) {
    uiLiveData.value = isInitialized
  }

  private val profileResultLiveData: LiveData<AsyncResult<Profile>> by lazy {
    profileManagementController.getProfile(profileId).toLiveData()
  }

  private val profileLiveData: LiveData<Profile> by lazy { getProfileData() }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileResultLiveData, ::processProfileResult)
  }

  val optionListLiveData: LiveData<List<OptionsItemViewModel>> by lazy {
    Transformations.map(profileLiveData, ::processProfileList)
  }

  fun setProfileId(profileId: ProfileId) {
    this.profileId = profileId
  }

  fun getProfileId(): ProfileId {
    return this.profileId
  }

  private fun processProfileResult(profile: AsyncResult<Profile>): Profile {
    return when (profile) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("OptionsFragment", "Failed to retrieve profile", profile.error)
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profile.value
    }
  }

  private fun processProfileList(profile: Profile): List<OptionsItemViewModel> {
    itemViewModelList.clear()

    val optionsReadingTextSizeViewModel =
      OptionsReadingTextSizeViewModel(
        routeToReadingTextSizeListener, loadReadingTextSizeListener, resourceHandler
      )
    val optionsAppLanguageViewModel =
      OptionsAppLanguageViewModel(
        routeToAppLanguageListListener, loadAppLanguageListListener,
        profile.oppiaLanguage, resourceHandler.computeLocalizedDisplayName(profile.oppiaLanguage)
      )
    val optionAudioViewViewModel =
      OptionsAudioLanguageViewModel(
        routeToAudioLanguageListListener,
        loadAudioLanguageListListener,
        profile.audioLanguage,
        resourceHandler.computeLocalizedDisplayName(profile.audioLanguage)
      )

    optionsReadingTextSizeViewModel.readingTextSize.set(profile.readingTextSize)

    itemViewModelList.add(optionsReadingTextSizeViewModel as OptionsItemViewModel)

    if (enableLanguageSelectionUi.value) {
      itemViewModelList.add(optionsAppLanguageViewModel as OptionsItemViewModel)
    }

    itemViewModelList.add(optionAudioViewViewModel as OptionsItemViewModel)

    // Loading the initial options in the sub-options container
    if (isMultipane.get()!! && isFirstOpen) {
      optionsReadingTextSizeViewModel.loadReadingTextSizeFragment()
      isFirstOpen = false
    }

    return itemViewModelList
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
