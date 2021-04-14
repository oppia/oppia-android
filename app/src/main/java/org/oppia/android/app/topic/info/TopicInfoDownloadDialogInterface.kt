package org.oppia.android.app.topic.info

import org.oppia.android.app.player.audio.CellularAudioDialogFragment

/** Interface to check the preference regarding alert for [CellularAudioDialogFragment]. */
interface TopicInfoDownloadDialogInterface {
  /**
   * If saveUserChoice is true, download topic and save preference do not show dialog again.
   * If saveUserChoice is false, download topic and do not save preference and show this dialog next time too.
   */
  fun downloadNowWhileOnCellular(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, do not download topic on cellular network and save preference.
   * If saveUserChoice is false, do not download topic and do not save preference.
   */
  fun noDownloadWhileOnCellular(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, resume download and save preference do not show dialog again.
   * If saveUserChoice is false, resume download and do not save preference and show this dialog next time too.
   */
  fun resumeDownloadWhileOnNetwork(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, do not resume download and save preference.
   * If saveUserChoice is false, do not resume download and do not save preference.
   */
  fun noResumeDownloadWhileOnNetwork(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, add topic to download queue and save preference do not show dialog again.
   * If saveUserChoice is false, add topic to download queue and do not save preference and show this dialog next time too.
   */
  fun downloadUseCellularData(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, do not resume download and save preference.
   * If saveUserChoice is false, do not resume download and do not save preference.
   */
  fun downloadNotUseCellularData(saveUserChoice: Boolean)
}
