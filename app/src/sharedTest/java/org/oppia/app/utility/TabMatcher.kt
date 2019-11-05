package org.oppia.app.utility

import android.view.View
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import androidx.test.espresso.PerformException
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher

/**
 * @see <a href="https://stackoverflow.com/questions/49626315/how-to-select-a-specific-tab-position-in-tab-layout-using-espresso-testing">TabMatcher</a>
 * This class mainly provides a custom matcher to test whether the current tab title is correctly selected in TabLayout. */
class TabMatcher {
  companion object {
    fun matchCurrentTabTitleCheck(tabTitle: String): Matcher<View> {
      return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description?) {
          description?.appendText("unable to match title of current selected tab with $tabTitle")
        }

        override fun matchesSafely(item: View?): Boolean {
          val tabLayout = item as TabLayout
          val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabLayout.selectedTabPosition)
            ?: throw PerformException.Builder()
              .withCause(Throwable("No tab at index ${tabLayout.selectedTabPosition}"))
              .build()
          return tabAtIndex.text.toString().contains(tabTitle, true)
        }
      }
    }
  }
}
