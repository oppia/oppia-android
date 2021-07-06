package org.oppia.android.app.devoptions.vieweventlogs

import org.oppia.android.app.model.EventLog
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.util.system.OppiaDateTimeFormatter
import javax.inject.Inject

/** [ViewModel] for displaying a event log. */
class EventLogItemViewModel @Inject constructor(
  val eventLog: EventLog,
  private val oppiaDateTimeFormatter: OppiaDateTimeFormatter
) : ObservableViewModel() {

  /** Returns the event log timestamp in a human readable format. */
  fun processDateAndTime(): String {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_hh_mm_aa,
      eventLog.timestamp
    )
  }

  /** Returns the event log priority in a human readable format. */
  fun formatPriorityString(): String? = eventLog.priority.name.toLowerCase().capitalize()

  /** Returns the event log context in a human readable format. */
  fun formatContextString(): String? =
    eventLog.context.activityContextCase.name.capitalizeWords().substringBeforeLast(" ")

  /** Returns the event log action name in a human readable format. */
  fun formatActionNameString(): String = eventLog.actionName.name.capitalizeWords()

  private fun String.capitalizeWords(): String =
    toLowerCase().split("_").joinToString(" ") { it.capitalize() }
}
