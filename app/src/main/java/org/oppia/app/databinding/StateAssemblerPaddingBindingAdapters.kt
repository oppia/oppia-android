package org.oppia.app.databinding

import android.view.View
import androidx.databinding.BindingAdapter

/** Used to set a padding for exploration split-view. */
@BindingAdapter(
  "app:explorationSplitViewPaddingApplicable",
  "app:explorationSplitViewPaddingStart",
  "app:explorationSplitViewPaddingTop",
  "app:explorationSplitViewPaddingEnd",
  "app:explorationSplitViewPaddingBottom",
  requireAll = false
)
fun setExplorationSplitViewPadding(
  view: View,
  isApplicable: Boolean,
  paddingStart: Float,
  paddingTop: Float,
  paddingEnd: Float,
  paddingBottom: Float
) {
  setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom)
}

/** Used to set a padding for exploration view. */
@BindingAdapter(
  "app:explorationViewPaddingApplicable",
  "app:explorationViewPaddingStart",
  "app:explorationViewPaddingTop",
  "app:explorationViewPaddingEnd",
  "app:explorationViewPaddingBottom",
  requireAll = false
)
fun setExplorationViewPadding(
  view: View,
  isApplicable: Boolean,
  paddingStart: Float,
  paddingTop: Float,
  paddingEnd: Float,
  paddingBottom: Float
) {
  setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom)
}

/** Used to set a padding for question split-view. */
@BindingAdapter(
  "app:questionViewPaddingApplicable",
  "app:questionViewPaddingStart",
  "app:questionViewPaddingTop",
  "app:questionViewPaddingEnd",
  "app:questionViewPaddingBottom",
  requireAll = false
)
fun setQuestionViewPadding(
  view: View,
  isApplicable: Boolean,
  paddingStart: Float,
  paddingTop: Float,
  paddingEnd: Float,
  paddingBottom: Float
) {
  setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom)
}

/** Used to set a padding for question view. */
@BindingAdapter(
  "app:questionSplitViewPaddingApplicable",
  "app:questionSplitViewPaddingStart",
  "app:questionSplitViewPaddingTop",
  "app:questionSplitViewPaddingEnd",
  "app:questionSplitViewPaddingBottom",
  requireAll = false
)
fun setQuestionSplitViewPadding(
  view: View,
  isApplicable: Boolean,
  paddingStart: Float,
  paddingTop: Float,
  paddingEnd: Float,
  paddingBottom: Float
) {
  setPaddingIfApplicable(view, isApplicable, paddingStart, paddingTop, paddingEnd, paddingBottom)
}

private fun setPaddingIfApplicable(
  view: View,
  isApplicable: Boolean,
  paddingStart: Float,
  paddingTop: Float,
  paddingEnd: Float,
  paddingBottom: Float
) {
  if (isApplicable) {
    view.setPadding(
      paddingStart.toInt(),
      paddingTop.toInt(),
      paddingEnd.toInt(),
      paddingBottom.toInt()
    )
    view.requestLayout()
  }
}
