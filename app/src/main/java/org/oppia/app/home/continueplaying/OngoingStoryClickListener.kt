package org.oppia.app.home.continueplaying

import org.oppia.app.model.StorySummary

/** Listener interface for when story summaries are clicked in the UI. */
interface StorySummaryClickListener {
  fun onStorySummaryClicked(storySummary: StorySummary)
}
