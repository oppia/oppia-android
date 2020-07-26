package org.oppia.app.databinding

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.databinding.BindingAdapter

/** Used to set a margin for exploration split-view. */
@BindingAdapter(
  "app:explorationSplitViewMarginApplicable",
  "app:explorationSplitViewMarginStart",
  "app:explorationSplitViewMarginTop",
  "app:explorationSplitViewMarginEnd",
  "app:explorationSplitViewMarginBottom",
  requireAll = false
)
fun setExplorationSplitViewMargin(
  view: View,
  isApplicable: Boolean,
  marginStart: Float,
  marginTop: Float,
  marginEnd: Float,
  marginBottom: Float
) {
  setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom)
}

/** Used to set a margin for exploration view. */
@BindingAdapter(
  "app:explorationViewMarginApplicable",
  "app:explorationViewMarginStart",
  "app:explorationViewMarginTop",
  "app:explorationViewMarginEnd",
  "app:explorationViewMarginBottom",
  requireAll = false
)
fun setExplorationViewMargin(
  view: View,
  isApplicable: Boolean,
  marginStart: Float,
  marginTop: Float,
  marginEnd: Float,
  marginBottom: Float
) {
  setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom)
}

/** Used to set a margin for question split-view. */
@BindingAdapter(
  "app:questionViewMarginApplicable",
  "app:questionViewMarginStart",
  "app:questionViewMarginTop",
  "app:questionViewMarginEnd",
  "app:questionViewMarginBottom",
  requireAll = false
)
fun setQuestionViewMargin(
  view: View,
  isApplicable: Boolean,
  marginStart: Float,
  marginTop: Float,
  marginEnd: Float,
  marginBottom: Float
) {
  setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom)
}

/** Used to set a margin for question view. */
@BindingAdapter(
  "app:questionSplitViewMarginApplicable",
  "app:questionSplitViewMarginStart",
  "app:questionSplitViewMarginTop",
  "app:questionSplitViewMarginEnd",
  "app:questionSplitViewMarginBottom",
  requireAll = false
)
fun setQuestionSplitViewMargin(
  view: View,
  isApplicable: Boolean,
  marginStart: Float,
  marginTop: Float,
  marginEnd: Float,
  marginBottom: Float
) {
  setMarginIfApplicable(view, isApplicable, marginStart, marginTop, marginEnd, marginBottom)
}

private fun setMarginIfApplicable(
  view: View,
  isApplicable: Boolean,
  marginStart: Float,
  marginTop: Float,
  marginEnd: Float,
  marginBottom: Float
) {
  if (isApplicable && view.layoutParams is MarginLayoutParams) {
    val params = view.layoutParams as MarginLayoutParams
    params.setMargins(
      marginStart.toInt(),
      marginTop.toInt(),
      marginEnd.toInt(),
      marginBottom.toInt()
    )
    view.requestLayout()
  }
}
