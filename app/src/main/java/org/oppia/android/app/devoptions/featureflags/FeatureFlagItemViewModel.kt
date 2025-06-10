  package org.oppia.android.app.devoptions.featureflags

  import androidx.databinding.ObservableField
  import javax.inject.Inject
  import org.oppia.android.app.viewmodel.ObservableViewModel
  import org.oppia.android.util.networking.ConnectionStatus

  class FeatureFlagItemViewModel @Inject constructor(
     val featureFlagName : String,
     val syncStatus : String,
     val isResetAvailable : Boolean,
     val currentValue : ObservableField<Boolean>,
     var statusBackground :Int
  ) : ObservableViewModel() {

    init {
        statusBackground = when (syncStatus) {
          "Server" -> org.oppia.android.app.R.drawable.
        }
    }
    fun onToggleValue() {
      currentValue.set(!currentValue.get()!!)
    }
  }