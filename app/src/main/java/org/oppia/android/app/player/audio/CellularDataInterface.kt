package org.oppia.android.app.player.audio

/** Interface to check the preference regarding alert for [CellularAudioDialogFragment]. */
interface CellularDataInterface {
  /**
   * If saveUserChoice is true, show audio-player and save preference do not show dialog again.
   * If saveUserChoice is false, show audio-player and do not save preference and show this dialog next time too.
   */
  fun enableAudioWhileOnCellular(saveUserChoice: Boolean)

  /**
   * If saveUserChoice is true, do not show audio-player on cellular network and save preference.
   * If saveUserChoice is false, do not show audio-player and do not save preference.
   */
  fun disableAudioWhileOnCellular(saveUserChoice: Boolean)
}
