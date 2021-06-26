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

  fun processDateAndTime(): String {
    return oppiaDateTimeFormatter.formatDateFromDateString(
      OppiaDateTimeFormatter.DD_MMM_hh_mm_aa,
      eventLog.timestamp
    )
  }
}
