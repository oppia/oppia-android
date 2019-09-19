package org.oppia.app.topic.conceptcard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.conceptcard_fragment.view.*
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

@FragmentScope
class ConceptCardPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ConceptCardViewModel>
  ){
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val view = inflater.inflate(R.layout.conceptcard_fragment, container,  false)
    view.toolbar.title = "Concept Card"
    view.toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
    view.toolbar.setNavigationOnClickListener {
      (fragment as? DialogFragment)?.dismiss()
    }
    return view
  }
}
