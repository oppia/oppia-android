package org.oppia.app.help.faq

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains FAQ in the app. */
class FAQFragment : InjectableFragment() {
  @Inject lateinit var faqFragmentPresenter: FAQFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return faqFragmentPresenter.handleCreateView(inflater, container)
  }
}
