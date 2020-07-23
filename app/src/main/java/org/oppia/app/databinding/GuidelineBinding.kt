package org.oppia.app.databinding

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingAdapter

@BindingAdapter("app:layout_constraintGuide_percent")
fun setGuidelinePercentage(guideline: Guideline, percentage: Float) {
  val params = guideline.layoutParams as ConstraintLayout.LayoutParams
  params.guidePercent = percentage
  guideline.layoutParams = params
}
