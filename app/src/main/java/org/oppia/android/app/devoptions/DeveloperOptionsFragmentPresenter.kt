package org.oppia.android.app.devoptions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject

// TODO(#3295): Introduce UI for Developer Options Menu.
/** The presenter for [DeveloperOptionsFragment]. */
@FragmentScope
class DeveloperOptionsFragmentPresenter @Inject constructor() {

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?
  ): View? {
    return inflater.inflate(
      R.layout.developer_options_fragment,
      container,
      /* attachToRoot= */ false
    )
  }
}
