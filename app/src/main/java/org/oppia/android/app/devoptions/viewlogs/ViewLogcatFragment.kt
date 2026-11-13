package org.oppia.android.app.devoptions.viewlogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that displays searchable Logcat entries. */
class ViewLogcatFragment : InjectableFragment() {
  @Inject lateinit var presenter: ViewLogcatFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
    presenter.handleCreateView(inflater, container, state)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.handleViewCreated()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    presenter.saveState(outState)
    super.onSaveInstanceState(outState)
  }

  companion object {
    fun newInstance() = ViewLogcatFragment()
  }
}
