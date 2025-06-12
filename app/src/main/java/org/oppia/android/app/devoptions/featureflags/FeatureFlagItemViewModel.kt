package org.oppia.android.app.devoptions.featureflags

import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

class FeatureFlagItemViewModel @Inject constructor(
  val featureFlagName: String,
  val syncStatus: String,
  val isResetAvailable: Boolean,
  val currentValue: Boolean,
  val syncStatusBackground: Int
) : ObservableViewModel()
