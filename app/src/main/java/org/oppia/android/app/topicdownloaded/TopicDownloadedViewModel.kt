package org.oppia.android.app.topicdownloaded

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PlatformParameter
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.MY_DOWNLOADS_FLAG
import org.oppia.android.util.platformparameter.MY_DOWNLOADS_IS_DISABLE
import javax.inject.Inject

/** [ViewModel] for showing topic info details. */
@FragmentScope
class TopicDownloadedViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private var platformParameterController: PlatformParameterController
) : ObservableViewModel() {

  /** Name of the downloaded topic */
  val topicName = ObservableField<String>("")

  /** Id of the profile which downloaded a topic */
  var internalProfileId: Int = -1

  /** Id of the downloaded topic */
  lateinit var topicId: String

  private val platformParameterList by lazy {
    val disableMyDownloadsFeature = PlatformParameter.newBuilder()
      .setName(MY_DOWNLOADS_FLAG)
      .setBoolean(MY_DOWNLOADS_IS_DISABLE)
      .build()
    listOf<PlatformParameter>(
      disableMyDownloadsFeature
    )
  }

  /** Starts TopicActivity with downloaded topic */
  fun viewDownloadedTopic(@Suppress("UNUSED_PARAMETER") v: View) {
    platformParameterController.updatePlatformParameterDatabase(platformParameterList)
    platformParameterController.getParameterDatabase().toLiveData().observeForever {
      activity.startActivity(
        TopicActivity.createTopicActivityIntent(
          activity,
          internalProfileId,
          topicId
        )
      )
      activity.finish()
    }
  }
}
