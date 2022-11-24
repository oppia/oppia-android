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
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** The fragment to change the language of the app. */
class AppLanguageFragment : InjectableFragment(), AppLanguageRadioButtonListener {

  @Inject
  lateinit var appLanguageFragmentPresenter: AppLanguageFragmentPresenter

  companion object {
    private const val FRAGMENT_ARGUMENTS_KEY = "AppLanguageFragment.arguments"
    private const val FRAGMENT_SAVED_STATE_KEY = "AppLanguageFragment.saved_state"

    fun newInstance(prefsSummaryValue: OppiaLanguage): AppLanguageFragment {
      return AppLanguageFragment().apply {
        arguments = Bundle().apply {
          val args = AppLanguageFragmentArguments.newBuilder().apply {
            this.oppiaLanguage = prefsSummaryValue
          }.build()
          putProto(FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }

    private fun Bundle.retrieveLanguageFromArguments(): OppiaLanguage {
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
      ) { "Expected arguments to be passed to AudioLanguageFragment" }

    return appLanguageFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      oppiaLanguage!!
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AppLanguageFragmentStateBundle.newBuilder().apply {
      oppiaLanguage = appLanguageFragmentPresenter.getLanguageSelected()
    }.build()
    outState.putProto(FRAGMENT_SAVED_STATE_KEY, state)
  }

  override fun onLanguageSelected(selectedLanguage: OppiaLanguage) {
    appLanguageFragmentPresenter.onLanguageSelected(selectedLanguage)
  }
}
