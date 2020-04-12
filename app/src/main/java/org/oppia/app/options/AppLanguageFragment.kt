package org.oppia.app.options

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

class AppLanguageFragment(private val prefsKey: String, private val prefsSummaryValue: String) : InjectableFragment() {
  @Inject
  lateinit var appLanguageFragmentPresenter: AppLanguageFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
    appLanguageFragmentPresenter.setFragment(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return appLanguageFragmentPresenter.handleCreateView(inflater, container, prefsKey, prefsSummaryValue)
  }
}