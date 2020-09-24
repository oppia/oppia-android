package org.oppia.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

private const val KEY_APP_LANGUAGE_PREFERENCE_TITLE = "APP_LANGUAGE_PREFERENCE"
private const val KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE =
  "APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
private const val KEY_SELECTED_LANGUAGE = "SELECTED_LANGUAGE"

/** The fragment to change the language of the app. */
class AppLanguageFragment : InjectableFragment() {

  @Inject
  lateinit var appLanguageFragmentPresenter: AppLanguageFragmentPresenter

  companion object {
    fun newInstance(prefsKey: String, prefsSummaryValue: String): AppLanguageFragment {
      val fragment = AppLanguageFragment()
      val args = Bundle()
      args.putString(KEY_APP_LANGUAGE_PREFERENCE_TITLE, prefsKey)
      args.putString(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE, prefsSummaryValue)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to AppLanguageFragment" }
    val prefsKey = args.getString(KEY_APP_LANGUAGE_PREFERENCE_TITLE)
    val prefsSummaryValue = if (savedInstanceState == null) {
      args.getString(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    } else {
      savedInstanceState.get(KEY_SELECTED_LANGUAGE) as String
    }
    return appLanguageFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      prefsKey!!,
      prefsSummaryValue!!
    )
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_SELECTED_LANGUAGE, appLanguageFragmentPresenter.getLanguageSelected())
  }
}
