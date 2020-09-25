package org.oppia.android.app.testing

import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.TestModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

// TODO(#59): Make this view model only included in relevant tests instead of all prod builds.
/** A [ObservableViewModel] for testing the bindable RecyclerView adapter. */
@FragmentScope
class BindableAdapterTestViewModel @Inject constructor() : ObservableViewModel() {
  val dataListLiveData = MutableLiveData<List<TestModel>>()
  val dataList: List<TestModel>? = null
}
