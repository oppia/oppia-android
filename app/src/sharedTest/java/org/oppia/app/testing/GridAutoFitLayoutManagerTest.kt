package org.oppia.app.testing

import android.content.Context
import android.content.res.Configuration
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.recyclerview.GridAutoFitLayoutManager

/**
 * It tests GridAutoFitTestAdapter and GridAutoFitLayoutManager
 * For reference https://github.com/codepath/android-robolectric-espresso-demo/blob/master/app/src/test/java/com.codepath.testingdemo/adapters/PostsAdapterTest.java
 */
@RunWith(AndroidJUnit4::class)
class GridAutoFitLayoutManagerTest {

  private var context: Context? = null
  @Before
  @ExperimentalCoroutinesApi
  fun setUp() {
    ApplicationProvider.getApplicationContext<Context>().resources.configuration.orientation =
      Configuration.ORIENTATION_LANDSCAPE
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testGridAutoFitLayoutManager_spanCountThreeVerifiedSuccessfully() {
    val adapter = GridAutoFitTestAdapter()
    val layoutManager = GridAutoFitLayoutManager(context!!, 400)
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    rvParent.measure(1200, 2000)
    ViewMatchers.assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(3)
    )
  }

  @Test
  fun testGridAutoFitLayoutManager_spanCountFourVerifiedSuccessfully() {
    val adapter = GridAutoFitTestAdapter()
    val layoutManager = GridAutoFitLayoutManager(context!!, 400)
    val rvParent = RecyclerView(context!!)
    rvParent.adapter = adapter
    rvParent.layoutManager = layoutManager
    rvParent.measure(1600, 2000)
    ViewMatchers.assertThat(
      "RecyclerViewGrid span count",
      layoutManager.spanCount,
      CoreMatchers.equalTo(4)
    )
  }
}
