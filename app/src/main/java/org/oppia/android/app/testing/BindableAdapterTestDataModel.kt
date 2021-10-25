package org.oppia.android.app.testing

import androidx.lifecycle.LiveData

/** Test data model for bindable adapter tests. */
sealed class BindableAdapterTestDataModel {
  val boundStringValue get() = (this as StringModel).stringValue
  val boundIntValue get() = (this as IntModel).intValue
  val boundLiveDataValue get() = (this as LiveDataModel).liveData

  data class StringModel(val stringValue: String) : BindableAdapterTestDataModel()

  data class IntModel(val intValue: Int) : BindableAdapterTestDataModel()

  data class LiveDataModel(val liveData: LiveData<String>) : BindableAdapterTestDataModel()
}
