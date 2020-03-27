import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.model.InteractionObject
import org.oppia.app.parser.RichTextViewMatcher
import org.oppia.app.recyclerview.RecyclerViewMatcher
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.app.testing.InputInteractionViewTestActivity
import org.oppia.app.testing.ProfileChooserFragmentTestActivity
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.utility.OrientationChangeAction
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import javax.inject.Singleton

/** Tests for [ConceptCardFragment]. */
@RunWith(AndroidJUnit4::class)
class MyFragmentTest {
    lateinit var activity:ProfileChooserFragmentTestActivity

    private var context: Context?=null

    @Before
    @ExperimentalCoroutinesApi
    fun setUp() {
        Intents.init()
        activity = Robolectric.setupActivity(ProfileChooserFragmentTestActivity::class.java)
        activity.resources.configuration.orientation=2
        ((activity.getProfileChooserFragment()!!.view!!.findViewWithTag<View>("r") as RecyclerView).layoutManager as GridLayoutManager).orientation=0
    }


    // @Test => JUnit 4 annotation specifying this is a test to be run
    // The test simply checks that our TextView exists and has the text "Hello world!"
    @Test
    @Config(qualifiers = "xxxhdpi")
    public fun validateTextViewContent() {
        val fragment =  activity.getProfileChooserFragment()
       val layoutManager= (fragment!!.view!!.findViewWithTag<View>("r") as RecyclerView).layoutManager
    }

   }
