package org.oppia.app.player.content

import android.app.Application
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.util.parser.UrlImageParser
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import javax.inject.Singleton
import org.oppia.util.parser.ImageLoader



/** Tests for [UrlImageParser]. */
@RunWith(AndroidJUnit4::class)
class UrlImageParserTest {

  val imageParser: UrlImageParser?=null
  private val entity_type: String = "exploration"
  private val entity_id: String = "umPkwp0L1M0-"
  var imageLoader: ImageLoader?=null
  var activity:ExplorationActivity?=null
  @Test
  fun testUrlImageParser_loadHtmlContent_isDisplayed() {
    ActivityScenario.launch(ExplorationActivity::class.java).use {
     onView(withId(org.oppia.app.R.id.recyclerview)).check(matches(isDisplayed()))
//      imageLoader = Mockito.mock(ImageLoader::class.java)
//
//      activity = Robolectric.setupActivity(ExplorationActivity::class.java!!)
//      val root = activity!!.findViewById(R.id.rl_container) as ViewGroup
//
//      val inflated = activity!!.getLayoutInflater().inflate(R.layout.content_card_items, root)
//      holder = Con(inflated, imageLoader)
    }
  }

  fun pauseTestFor(milliseconds: Long) {
    try {
      Thread.sleep(milliseconds)
    } catch (e: InterruptedException) {
      e.printStackTrace();
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
