package org.oppia.app.recyclerview

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
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
          var idDescription = Integer.toString(recyclerViewId)
          if (this.resources != null) {
            idDescription = try {
              this.resources!!.getResourceName(recyclerViewId)
            } catch (var4: Resources.NotFoundException) {
              String.format(
                "%s (resource name not found)",
                recyclerViewId
              )
            }
          }
          description.appendText("with id: $idDescription")
        }

        public override fun matchesSafely(view: View): Boolean {
          this.resources = view.resources
          if (childView == null) {
            val recyclerView = view.rootView.findViewById<View>(recyclerViewId) as RecyclerView
            if (recyclerView.id == recyclerViewId) {
              childView = recyclerView.findViewHolderForAdapterPosition(position)!!.itemView
            } else {
              return false
            }
          }
          return if (targetViewId == -1) {
            view === childView
          } else {
            val targetView = childView!!.findViewById<View>(targetViewId)
            view === targetView
          }
        }
      }
    }
  }
}
