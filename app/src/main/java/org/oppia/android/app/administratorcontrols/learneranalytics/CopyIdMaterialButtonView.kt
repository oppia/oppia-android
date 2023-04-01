package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

/**
 * Custom [MaterialButton] with a darkened background tint mode (per [setBackgroundTintMode]) so
 * that it can have a white background without the ripple effect disappearing.
 *
 * Note that this view is currently only used for learner analytics admin page buttons to copy
 * various IDs to the device clipboard, and is intended only for that purpose.
 */
class CopyIdMaterialButtonView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
  init {
    // Ensure the tint mode is properly set (since it can't be set in XML).
    backgroundTintMode = PorterDuff.Mode.DARKEN
  }
}
