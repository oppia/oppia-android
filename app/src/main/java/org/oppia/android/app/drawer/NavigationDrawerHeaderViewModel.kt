package org.oppia.android.app.drawer

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for displaying User profile details in navigation header. */
class NavigationDrawerHeaderViewModel @Inject constructor(
  fragment: Fragment
) : ObservableViewModel() {
  private var routeToProfileProgressListener = fragment as RouteToProfileProgressListener

  val profile = ObservableField<Profile>(Profile.getDefaultInstance())
  val ongoingTopicCount = ObservableField<Int>(0)
  val completedStoryCount = ObservableField<Int>(0)

  fun onHeaderClicked() {
    routeToProfileProgressListener.routeToProfileProgress(profile.get()!!.id.internalId)
  }
}
