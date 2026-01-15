package org.oppia.android.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.AppLanguageFragmentArguments
import org.oppia.android.app.model.AppLanguageFragmentStateBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The fragment to change the language of the app. */
class AppLanguageFragment : InjectableFragment(), AppLanguageRadioButtonListener {
  @Inject lateinit var appLanguageFragmentPresenterV1: AppLanguageFragmentPresenterV1

  @Inject lateinit var appLanguageFragmentPresenter: AppLanguageFragmentPresenter

  @Inject
  @field:EnableOnboardingFlowV2
  lateinit var enableOnboardingFlowV2: PlatformParameterValue<Boolean>

  private var profileId: LegacyProfileId = LegacyProfileId.getDefaultInstance()

  companion object {
    private const val FRAGMENT_ARGUMENTS_KEY = "AppLanguageFragment.arguments"
    private const val FRAGMENT_SAVED_STATE_KEY = "AppLanguageFragment.saved_state"

    /** Returns a new [AppLanguageFragment] instance. */
    fun newInstance(oppiaLanguage: OppiaLanguage, internalProfileId: Int): AppLanguageFragment {
      val profileId = LegacyProfileId.newBuilder().setInternalId(internalProfileId).build()
      return AppLanguageFragment().apply {
        arguments = Bundle().apply {
          val args = AppLanguageFragmentArguments.newBuilder().apply {
            this.oppiaLanguage = oppiaLanguage
          }.build()
          putProto(FRAGMENT_ARGUMENTS_KEY, args)
          decorateWithUserProfileId(profileId)
        }
      }
    }

    /** Returns the [OppiaLanguage] stored in the fragment's arguments. */
    fun Bundle.retrieveLanguageFromArguments(): OppiaLanguage {
      return getProto(
        FRAGMENT_ARGUMENTS_KEY, AppLanguageFragmentArguments.getDefaultInstance()
      ).oppiaLanguage
    }

    private fun Bundle.retrieveLanguageFromSavedState(): OppiaLanguage {
      return getProto(
        FRAGMENT_SAVED_STATE_KEY, AppLanguageFragmentStateBundle.getDefaultInstance()
      ).oppiaLanguage
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val oppiaLanguage =
      checkNotNull(
        savedInstanceState?.retrieveLanguageFromSavedState()
          ?: arguments?.retrieveLanguageFromArguments()
      ) { "Expected an oppiaLanguage argument to be passed to AppLanguageFragment" }

    profileId = checkNotNull(arguments?.extractCurrentUserProfileId()) {
      "Expected a profileId argument to be passed to AppLanguageFragment"
    }

    return if (enableOnboardingFlowV2.value) {
      appLanguageFragmentPresenter.handleCreateView(
        inflater,
        container,
        savedInstanceState,
        oppiaLanguage,
        profileId
      )
    } else {
      appLanguageFragmentPresenterV1.handleOnCreateView(
        inflater,
        container,
        oppiaLanguage,
        profileId.internalId
      )
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (enableOnboardingFlowV2.value) {
      appLanguageFragmentPresenter.saveToSavedInstanceState(outState)
    } else {
      val state = AppLanguageFragmentStateBundle.newBuilder().apply {
        oppiaLanguage = appLanguageFragmentPresenterV1.getLanguageSelected()
      }.build()
      outState.putProto(FRAGMENT_SAVED_STATE_KEY, state)
    }
  }

  override fun onLanguageSelected(appLanguage: OppiaLanguage) {
    appLanguageFragmentPresenterV1.onLanguageSelected(appLanguage)
  }
}
