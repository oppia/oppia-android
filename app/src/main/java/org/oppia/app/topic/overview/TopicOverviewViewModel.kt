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

const val STATUS_NOT_DOWNLOADED = "STATUS_NOT_DOWNLOADED"
const val STATUS_DOWNLOADED = "STATUS_DOWNLOADED"
const val STATUS_DOWNLOADING = "STATUS_DOWNLOADING"
/** [ViewModel] for showing topic overview details. */
@FragmentScope
class TopicOverviewViewModel @Inject constructor() : ObservableViewModel() {
  companion object {

    @JvmStatic
    @BindingAdapter("downloadDrawable")
    fun setBackgroundResource(downloadImageView: ImageView, downloadStatus: String) {
      when (downloadStatus) {
        STATUS_NOT_DOWNLOADED -> downloadImageView.setImageResource(R.drawable.ic_file_download_primary_24dp)
        STATUS_DOWNLOADED -> downloadImageView.setImageResource(R.drawable.ic_check_circle_primary_24dp)
        STATUS_DOWNLOADING -> downloadImageView.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp)
      }

    }
  }

  val topic = ObservableField<Topic>(Topic.getDefaultInstance())

  var downloadStatus = ObservableField<String>(STATUS_NOT_DOWNLOADED)
}
