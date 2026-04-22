package org.oppia.android.app.lint

import android.annotation.SuppressLint
import android.widget.TextView

// Lint bucket verification (file 2 of 2): triggers SetTextI18n (checksForIncrementalSources).
// This second file proves incremental mode scans ALL changed files, not just one.
// Suppressed here so full mode passes; fast mode output (pre-suppression) is the actual proof.
/** Lint bucket verification trigger (file 2): deliberately passes a string literal to [TextView.setText]. */
@SuppressLint("SetTextI18n")
fun triggerSetTextI18nFile2(tv: TextView) {
  tv.text = "another hardcoded string"
}
