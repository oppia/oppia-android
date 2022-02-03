package org.oppia.android.app.devoptions.vieweventlogs

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.locale.OppiaLocale

/** [ViewModel] for displaying a event log. */
class EventLogItemViewModel(
  val eventLog: EventLog,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  /** Returns the event log timestamp in a human readable format. */
  fun processDateAndTime(): String = resourceHandler.computeDateTimeString(eventLog.timestamp)

  /** Returns the event log priority in a human readable format. */
  fun formatPriorityString(): String = machineLocale.run {
    // Use the machine locale for capitalization/case changes since this string is only used by
    // developers.
    eventLog.priority.name.toMachineLowerCase().capitalizeForMachines()
  }

  /** Returns the event log context in a human readable format. */
  fun formatContextString(): String =
    eventLog.context.activityContextCase.name.capitalizeWords()

  /** Returns the event log action name in a human readable format. */
  fun formatActionNameString(): String = eventLog.context.activityContextCase.name.capitalizeWords()

  private fun String.capitalizeWords(): String = machineLocale.run {
    // Use the machine locale for capitalization/case changes since this string is only used by
    // developers.
    this@capitalizeWords.toMachineLowerCase().split("_").joinToString(" ") {
      it.capitalizeForMachines()
    }
  }
}
