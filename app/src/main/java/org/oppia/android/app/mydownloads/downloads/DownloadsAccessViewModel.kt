package org.oppia.android.app.mydownloads.downloads

import androidx.databinding.ObservableField
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** The ViewModel for [DownloadsAccessDialogFragment]. */
@FragmentScope
class DownloadsAccessViewModel @Inject constructor() : ObservableViewModel() {
  val inputPin = ObservableField("")
  val errorMessage = ObservableField("")
}
