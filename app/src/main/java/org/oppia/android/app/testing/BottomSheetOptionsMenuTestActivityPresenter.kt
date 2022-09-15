package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.test_bottom_sheet_options_menu.*
import org.oppia.android.R
import org.oppia.android.app.help.HelpActivity
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.options.OptionsActivity
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import javax.inject.Inject

private const val profileId = 0

class BottomSheetOptionsMenuTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.test_bottom_sheet_options_menu)
    activity.action_bottom_sheet_options_menu.setOnClickListener {
      val bottomSheetOptionsMenu = BottomSheetOptionsMenu()
      bottomSheetOptionsMenu.showNow(activity.supportFragmentManager, bottomSheetOptionsMenu.tag)
    }
  }

  fun handleOnOptionsItemSelected(itemId: Int): Boolean {
    return when (itemId) {
      R.id.action_options -> {
        val intent = OptionsActivity.createOptionsActivity(
          activity,
          profileId,
          /* isFromNavigationDrawer= */ false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
        activity.startActivity(intent)
        true
      }
      R.id.action_help -> {
        val intent = HelpActivity.createHelpActivityIntent(
          activity, profileId,
          /* isFromNavigationDrawer= */false
        )
        fontScaleConfigurationUtil.adjustFontScale(activity, ReadingTextSize.MEDIUM_TEXT_SIZE)
        activity.startActivity(intent)
        true
      }
      else -> false
    }
  }
}
