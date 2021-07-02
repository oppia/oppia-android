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

  /**
   * It processes timestamp and converts it to a proper date and time string format to show in the
   * event log item.
   */
  fun processDateAndTime(): String {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_hh_mm_aa,
      eventLog.timestamp
    )
  }

  /** It formats the event log priority to a proper string format to show in the event log item. */
  fun formatPriorityString(): String? = eventLog.priority.name.toLowerCase().capitalize()

  /** It formats the event log context to a proper string format to show in the event log item. */
  fun formatContextString(): String? =
    eventLog.context.activityContextCase.name.capitalizeWords().substringBeforeLast(" ")

  /** It formats the event log action name to a proper string format to show in the event log item. */
  fun formatActionNameString(): String = eventLog.actionName.name.capitalizeWords()

  private fun String.capitalizeWords(): String =
    toLowerCase().split("_").joinToString(" ") { it.capitalize() }
}
