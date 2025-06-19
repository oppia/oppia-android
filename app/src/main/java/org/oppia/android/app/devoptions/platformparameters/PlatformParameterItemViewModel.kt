package org.oppia.android.app.devoptions.platformparameters

import org.oppia.android.app.model.PlatformParameterValue
import org.oppia.android.app.viewmodel.ObservableViewModel

class PlatformParameterItemViewModel(
  val platformParameterName: String,
  val syncStatus: String,
  val isResetAvailable: Boolean,
  val currentValue: PlatformParameterValue,
  val syncStatusBackground: Int
) : ObservableViewModel()
