package org.oppia.app.help

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.viewmodel.ObservableViewModel
import java.text.FieldPosition
import javax.inject.Inject

/** [ObservableViewModel] for the recycler view of HelpActivity. */
class HelpViewModel @Inject constructor(
  private var activity: AppCompatActivity
) : ObservableViewModel(), HelpNavigator {
  public var title = ""
  public var position: Int = -1

  constructor(category: String, position: Int, activity: AppCompatActivity) : this(activity) {
    this.title = category
    this.position = position
  }

  fun onHelpItemClick() {
    onItemClick(position)
  }

  override fun onItemClick(position: Int) {
    when (HelpItems.getHelpItemForPosition(position)) {
      HelpItems.FAQ -> {
        val routeToFAQListener = activity as RoutetoFAQ
        routeToFAQListener.onRouteToFAQ()
      }
    }
  }
}
