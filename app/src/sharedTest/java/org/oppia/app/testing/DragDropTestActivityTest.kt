package org.oppia.app.testing

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class DragDropTestActivityTest {

  @Rule
  @JvmField
  var mActivityTestRule = ActivityTestRule(DragDropTestActivity::class.java)

  @Test
  fun dragDropTestActivityTest() {
    val textView = onView(
      allOf(
        withId(R.id.text_view_for_string_no_data_binding), withText("Item 1"),
        childAtPosition(
          allOf(
            withId(R.id.drag_drop_recycler_View),
            childAtPosition(
              withId(android.R.id.content),
              0
            )
          ),
          0
        ),
        isDisplayed()
      )
    )
    textView.check(matches(withText("Item 1")))

    val textView2 = onView(
      allOf(
        withId(R.id.text_view_for_string_no_data_binding), withText("Item 2"),
        childAtPosition(
          allOf(
            withId(R.id.drag_drop_recycler_View),
            childAtPosition(
              withId(android.R.id.content),
              0
            )
          ),
          2
        ),
        isDisplayed()
      )
    )
    textView2.check(matches(withText("Item 2")))

    val textView3 = onView(
      allOf(
        withId(R.id.text_view_for_string_no_data_binding), withText("Item 3"),
        childAtPosition(
          allOf(
            withId(R.id.drag_drop_recycler_View),
            childAtPosition(
              withId(android.R.id.content),
              0
            )
          ),
          1
        ),
        isDisplayed()
      )
    )
    textView3.check(matches(withText("Item 3")))
  }

  private fun childAtPosition(
    parentMatcher: Matcher<View>, position: Int
  ): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
      override fun describeTo(description: Description) {
        description.appendText("Child at position $position in parent ")
        parentMatcher.describeTo(description)
      }

      public override fun matchesSafely(view: View): Boolean {
        val parent = view.parent
        return parent is ViewGroup && parentMatcher.matches(parent)
          && view == parent.getChildAt(position)
      }
    }
  }
}
