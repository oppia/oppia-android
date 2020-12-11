package org.oppia.android.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject

@FragmentScope
class HomeViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  val itemList: List<HomeItemViewModel>
) : HomeItemViewModel() {
}
