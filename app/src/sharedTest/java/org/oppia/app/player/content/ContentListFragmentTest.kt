package org.oppia.app.player.content

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton
import org.oppia.data.backends.gae.model.GaeSubtitledHtml
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import org.oppia.app.player.exploration.ExplorationActivity
import android.widget.LinearLayout
import android.widget.TextView
import junit.framework.TestCase.assertTrue
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import android.R
import org.junit.Rule
import android.R





// TODO(#116): Write test-cases when the user enables/disables on cellular with/without saving the setting.
/** Tests for [ContentListFragment]. */
@RunWith(AndroidJUnit4::class)
class ContentListFragmentTest {

  var contentListFragment : ContentListFragmentPresenter? = null
  var contentList: MutableList<GaeSubtitledHtml> =ArrayList()
  val entity_type: String = "exploration"
  val entity_id: String = "umPkwp0L1M0-"

  @Test
  fun testContentListFragment_loadHtmlContent_isDisplayed() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
      contentListFragment = ContentListFragmentPresenter(ApplicationProvider.getApplicationContext(), Fragment())

      contentList = contentListFragment!!.fetchDummyExplorations()

      val adapter = ContentCardAdapter(ApplicationProvider.getApplicationContext(),entity_type,entity_id,contentList)

      onView(withId(org.oppia.app.R.id.recyclerView)).check(matches(isDisplayed()))
      val parent = LinearLayout(ApplicationProvider.getApplicationContext())

      // Content view holder
      val childViewHolder = adapter.onCreateViewHolder(parent, VIEW_TYPE_CONTENT)
      assertTrue(childViewHolder is ContentCardAdapter.ContentViewHolder)

      // Learners view holder
      val groupViewHolder = adapter.onCreateViewHolder(parent, VIEW_TYPE_INTERACTION)
      assertTrue(groupViewHolder is ContentCardAdapter.LearnersViewHolder)


      adapter.parseHtml(contentList.get(0).html,childViewHolder.itemView as TextView)

    }
  }


  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#89): Introduce a proper IdlingResource for background dispatchers to ensure they all complete before
    //  proceeding in an Espresso test. This solution should also be interoperative with Robolectric contexts by using a
    //  test coroutine dispatcher.

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@BlockingDispatcher blockingDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return blockingDispatcher
    }
  }

  @Singleton
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }
  }
}
