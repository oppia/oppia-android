package org.oppia.app.topic.overview

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Topic
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for showing topic overview details. */
@FragmentScope
class TopicOverviewViewModel @Inject constructor() : ObservableViewModel() {
  companion object {
    @JvmStatic
    @BindingAdapter("android:src")
    fun setBackgroundResource(downloadStatus: ImageView, resource: Int) {
      downloadStatus.setImageResource(resource)
    }
  }

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())

  var downloadStatus = ObservableField<Int>(R.drawable.ic_file_download_primary_24dp)
}
