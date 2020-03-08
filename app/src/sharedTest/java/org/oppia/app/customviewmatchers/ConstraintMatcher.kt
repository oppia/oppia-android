package org.oppia.app.customviewmatchers

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

/**
 * A custom ViewMatcher that checks the constraints of the view
 * @param constraint the type of the constraint to be checked.
 * @param id the id of the target view
 */
class ConstraintMatcher(private val constraint: Constraint, private val id: Int) :
  TypeSafeMatcher<View>() {

  companion object {
    const val PARENT = 0
  }

  sealed class Constraint {
    object StartToStart : Constraint()
    object StartToEnd : Constraint()
    object EndToEnd : Constraint()
    object EndToStart : Constraint()
    object LeftToLeft : Constraint()
    object LeftToRight : Constraint()
    object RightToLeft : Constraint()
    object RightToRight : Constraint()
    object TopToTop : Constraint()
    object TopToBottom : Constraint()
    object BottomToTop : Constraint()
    object BottomToBottom : Constraint()
  }

  override fun describeTo(description: Description?) {
    description?.appendText("with ConstraintMatcher: ")
    description?.appendValue("$id")
  }

  override fun matchesSafely(item: View?): Boolean {
    val params = item?.layoutParams as ConstraintLayout.LayoutParams
    val actual: Int
    actual = when (constraint) {
      Constraint.StartToStart -> params.startToStart
      Constraint.StartToEnd -> params.startToEnd
      Constraint.EndToEnd -> params.endToEnd
      Constraint.EndToStart -> params.endToStart
      Constraint.LeftToLeft -> params.leftToLeft
      Constraint.LeftToRight -> params.leftToRight
      Constraint.RightToLeft -> params.rightToLeft
      Constraint.RightToRight -> params.rightToRight
      Constraint.TopToTop -> params.topToTop
      Constraint.TopToBottom -> params.topToBottom
      Constraint.BottomToTop -> params.bottomToTop
      Constraint.BottomToBottom -> params.bottomToBottom
    }
    return actual == id
  }
}