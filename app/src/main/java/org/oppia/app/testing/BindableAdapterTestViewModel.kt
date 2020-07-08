package org.oppia.app.testing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.TestModel
import javax.inject.Inject

// TODO(#59): Make this view model only included in relevant tests instead of all prod builds.
/** A [ViewModel] for testing the bindable RecyclerView adapter. */
@FragmentScope
class BindableAdapterTestViewModel @Inject constructor() : ViewModel() {
  val dataListLiveData = MutableLiveData<List<TestModel>>()
  val dataList: List<TestModel>? = null
}
