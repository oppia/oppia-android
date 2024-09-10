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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The fragment to change the language of the app. */
class AppLanguageFragment : InjectableFragment(), AppLanguageRadioButtonListener {

  @Inject
  lateinit var appLanguageFragmentPresenter: AppLanguageFragmentPresenter
  private var profileId: Int? = -1

  companion object {
    private const val FRAGMENT_ARGUMENTS_KEY = "AppLanguageFragment.arguments"
    private const val FRAGMENT_SAVED_STATE_KEY = "AppLanguageFragment.saved_state"

    /** Returns a new [AppLanguageFragment] instance. */
    fun newInstance(oppiaLanguage: OppiaLanguage, internalProfileId: Int): AppLanguageFragment {
      val profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
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
      ) { "Expected arguments to be passed to AppLanguageFragment" }
    profileId = arguments?.extractCurrentUserProfileId()?.internalId ?: -1

    return appLanguageFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      oppiaLanguage,
      profileId!!
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AppLanguageFragmentStateBundle.newBuilder().apply {
      oppiaLanguage = appLanguageFragmentPresenter.getLanguageSelected()
    }.build()
    outState.putProto(FRAGMENT_SAVED_STATE_KEY, state)
  }

  override fun onLanguageSelected(appLanguage: OppiaLanguage) {
    appLanguageFragmentPresenter.onLanguageSelected(appLanguage)
  }
}
