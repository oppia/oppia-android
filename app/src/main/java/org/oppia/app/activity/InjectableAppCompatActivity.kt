package org.oppia.app.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.fragment.FragmentComponent

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent fragments that extend
 * [org.oppia.app.fragment.InjectableFragment].
 */
abstract class InjectableAppCompatActivity : AppCompatActivity() {
  /**
   * The [ActivityComponent] corresponding to this activity. This cannot be used before [onCreate] is called, and can be
   * used to inject lateinit fields in child activities during activity creation (which is recommended to be done in an
   * override of [onCreate]).
   */
  lateinit var activityComponent: ActivityComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    // Note that the activity component must be initialized before onCreate() since it's possible for onCreate() to
    // synchronously attach fragments (e.g. during a configuration change), which requires the activity component for
    // createFragmentComponent(). This means downstream dependencies should not perform any major operations to the
    // injected activity since it's not yet fully created.
    initializeActivityComponent()
    super.onCreate(savedInstanceState)
  }

  override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    super.onCreate(savedInstanceState, persistentState)
    initializeActivityComponent()
  }

  private fun initializeActivityComponent() {
    activityComponent = (application as ActivityComponentFactory).createActivityComponent(this)
  }

  fun createFragmentComponent(fragment: Fragment): FragmentComponent {
    return activityComponent.getFragmentComponentBuilderProvider().get().setFragment(fragment)
      .build()
  }
}
