package org.oppia.android.app.recyclerview

import android.content.res.Resources
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

// Reference Link: https://github.com/dannyroa/espresso-samples/blob/master/RecyclerView/app/src/androidTest/java/com/dannyroa/espresso_samples/recyclerview/RecyclerViewMatcher.java
class RecyclerViewMatcher {

  companion object {

    /**
     * This function returns a Matcher for an item inside RecyclerView from a specified position.
     */
    fun atPosition(recyclerViewId: Int, position: Int): Matcher<View> {
      return atPositionOnView(recyclerViewId, position, -1)
    }

    /**
     * This function returns a Matcher for a specific view within the item inside RecyclerView from a specified position.
     */
    fun atPositionOnView(recyclerViewId: Int, position: Int, targetViewId: Int): Matcher<View> {
      return object : TypeSafeMatcher<View>() {
        var resources: Resources? = null
        var childView: View? = null

        override fun describeTo(description: Description) {
          var idDescription = recyclerViewId.toString()
          if (this.resources != null) {
            idDescription = try {
              this.resources!!.getResourceName(recyclerViewId)
            } catch (var4: Resources.NotFoundException) {
              "$recyclerViewId (resource name not found)"
            }
          }
          description.appendText("with id: $idDescription")
        }

        public override fun matchesSafely(view: View): Boolean {
          this.resources = view.resources
          if (childView == null) {
            val recyclerView = view.rootView.findViewById<View>(recyclerViewId) as? RecyclerView
            if (recyclerView?.id == recyclerViewId) {
              childView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            } else return false
          }
          return if (targetViewId == -1) {
            view === childView
          } else {
            view === childView?.findViewById<View>(targetViewId)
          }
        }
      }
    }

    /** Returns item count ViewAssertion for a recycler view. */
    fun hasItemCount(count: Int): ViewAssertion {
      return RecyclerViewItemCountAssertion(count)
    }

    /** Returns span count ViewAssertion for a recycler view that use GridLayoutManager. */
    fun hasGridItemCount(spanCount: Int, position: Int): ViewAssertion {
      return RecyclerViewGridItemCountAssertion(spanCount, position)
    }

    fun hasGridColumnCount(expectedColumnCount: Int): ViewAssertion {
      return GridLayoutManagerColumnCountAssertion(expectedColumnCount)
    }

    /**
     * Verifies if an item at a specified position in a RecyclerView with the given ID contains a target view that is displayed.
     *
     * @param recyclerViewId The resource ID of the RecyclerView.
     * @param itemPosition The position of the item in the RecyclerView.
     * @param targetView The resource ID of the target view within the item.
     */
    fun verifyItemDisplayedOnListItem(
      recyclerViewId: Int,
      itemPosition: Int,
      targetView: Int
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = recyclerViewId,
          position = itemPosition,
          targetViewId = targetView
        )
      ).check(matches(isDisplayed()))
    }

    /**
     * Verifies if an item at a specified position in a RecyclerView with the given ID does not contain a target view.
     *
     * @param recyclerViewId The resource ID of the RecyclerView.
     * @param itemPosition The position of the item in the RecyclerView.
     * @param targetView The resource ID of the target view within the item.
     */
    fun verifyItemDisplayedOnListItemDoesNotExist(
      recyclerViewId: Int,
      itemPosition: Int,
      targetView: Int
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = recyclerViewId,
          position = itemPosition,
          targetViewId = targetView
        )
      ).check(doesNotExist())
    }

    /**
     * Verifies if the text of a target view within an item at a specified position in a RecyclerView with the given ID matches the provided string resource.
     *
     * @param recyclerViewId The resource ID of the RecyclerView.
     * @param itemPosition The position of the item in the RecyclerView.
     * @param targetViewId The resource ID of the target view within the item.
     * @param stringIdToMatch The resource ID of the string to match against the target view's text.
     */
    fun verifyTextOnListItemAtPosition(
      recyclerViewId: Int,
      itemPosition: Int,
      targetViewId: Int,
      @StringRes stringIdToMatch: Int,
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = recyclerViewId,
          position = itemPosition,
          targetViewId = targetViewId
        )
      ).check(matches(withText(stringIdToMatch)))
    }

    /**
     * Verifies if a target view within an item at a specified position in a RecyclerView with the given ID does not exist.
     *
     * @param recyclerViewId The resource ID of the RecyclerView.
     * @param itemPosition The position of the item in the RecyclerView.
     * @param targetViewId The resource ID of the target view within the item.
     */
    fun verifyTextViewOnListItemAtPositionDoesNotExist(
      recyclerViewId: Int,
      itemPosition: Int,
      targetViewId: Int
    ) {
      onView(
        atPositionOnView(
          recyclerViewId = recyclerViewId,
          position = itemPosition,
          targetViewId = targetViewId
        )
      ).check(doesNotExist())
    }

    /**
     * Verifies if the provided text, referenced by its String resource ID, is displayed in a dialog.
     *
     * @param textInDialogId The resource ID of the text to verify in the dialog.
     */
    fun verifyTextInDialog(@StringRes textInDialogId: Int) {
      onView(withText(textInDialogId))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }

    /**
     * Scrolls to the specified position within a RecyclerView.
     *
     * @param position The position to scroll to within the RecyclerView.
     * @param recyclerViewId The resource ID of the RecyclerView to perform scrolling on.
     */
    fun scrollToPosition(position: Int, recyclerViewId: Int) {
      onView(withId(recyclerViewId))
        .perform(scrollToPosition<RecyclerView.ViewHolder>(position))
    }
  }

  private class RecyclerViewItemCountAssertion(private val count: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      check(view is RecyclerView) { "The asserted view is not RecyclerView" }
      checkNotNull(view.adapter) { "No adapter is assigned to RecyclerView" }
      assertThat("RecyclerView item count", view.adapter!!.itemCount, equalTo(count))
    }
  }

  /** Custom class to check number of spans occupied by an item at a given position. */
  private class RecyclerViewGridItemCountAssertion(
    private val count: Int,
    private val position: Int
  ) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      check(view is RecyclerView) { "The asserted view is not RecyclerView" }
      check(view.layoutManager is GridLayoutManager) { "RecyclerView must use GridLayoutManager" }
      val spanCount = (view.layoutManager as GridLayoutManager).spanSizeLookup.getSpanSize(position)
      assertThat("RecyclerViewGrid span count", spanCount, equalTo(count))
    }
  }

  private class GridLayoutManagerColumnCountAssertion(expectedColumnCount: Int) : ViewAssertion {
    private var expectedColumnCount: Int = 0

    init {
      this.expectedColumnCount = expectedColumnCount
    }

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
      if (noViewFoundException != null) {
        throw noViewFoundException
      }
      val recyclerView = view as RecyclerView
      if (recyclerView.layoutManager is GridLayoutManager) {
        val gridLayoutManager = recyclerView.layoutManager as GridLayoutManager
        val spanCount = gridLayoutManager.spanCount
        if (spanCount != expectedColumnCount) {
          val errorMessage =
            ("expected column count $expectedColumnCount but was $spanCount")
          throw AssertionError(errorMessage)
        }
      } else {
        throw IllegalStateException("no grid layout manager")
      }
    }
  }
}
