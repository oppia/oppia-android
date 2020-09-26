package org.oppia.android.app.help.faq

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains FAQ list in the app. */
class FAQListFragment : InjectableFragment() {
  @Inject
  lateinit var faqListFragmentPresenter: FAQListFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return faqListFragmentPresenter.handleCreateView(inflater, container)
  }
}
