package org.oppia.android.app.lint

import android.annotation.SuppressLint
import android.widget.TextView

// Lint bucket verification (file 1 of 2): triggers SetTextI18n (checksForIncrementalSources).
// SetTextI18n fires when a string literal is passed directly to TextView.setText().
// Suppressed here so full mode passes; fast mode output (pre-suppression) is the actual proof.
/** Lint bucket verification trigger: deliberately passes a string literal to [TextView.setText]. */
@SuppressLint("SetTextI18n")
fun triggerSetTextI18nFile1(tv: TextView) {
  tv.text = "hardcoded string"
}
