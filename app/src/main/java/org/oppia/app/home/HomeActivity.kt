package org.oppia.app.home

import android.os.Bundle
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.oppia.app.activity.InjectableAppCompatActivity
import java.io.FileReader
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity :  InjectableAppCompatActivity()  {
  @Inject lateinit var homeActivityController: HomeActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

       activityComponent.inject(this)
    homeActivityController.handleOnCreate()

  }
}
