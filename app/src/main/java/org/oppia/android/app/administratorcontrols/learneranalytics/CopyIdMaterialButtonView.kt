package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class CopyIdMaterialButtonView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {
  init {
    // Ensure the tint mode is properly set (since it can't be set in XML).
    backgroundTintMode = PorterDuff.Mode.DARKEN
  }
}
