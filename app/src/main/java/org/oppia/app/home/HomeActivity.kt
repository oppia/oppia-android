package org.oppia.app.home

import android.os.Bundle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.utility.Logger
import org.oppia.util.data.InMemoryBlockingCache
import org.oppia.util.data.InMemoryBlockingCache_Factory_Factory
import javax.inject.Inject
import javax.inject.Provider

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity() {
  @Inject lateinit var homeActivityController: HomeActivityController
  var blockingDispatcher: CoroutineDispatcher? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityController.handleOnCreate()

    blockingDispatcher = Dispatchers.Default

   Logger(this@HomeActivity, blockingDispatcher!!).e("Debug","Test456")
  }
}
