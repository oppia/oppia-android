package org.oppia.android.app.administratorcontrols

/** Listener for indicating when the admin wishes to view learner analytics on tablets. */
interface LoadLearnerAnalyticsListener {
  /** Called when the activity should load and show the learner analytics screen. */
  fun loadLearnerAnalyticsData()
}
