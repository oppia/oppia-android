package org.oppia.app.player.audio

/** Interface to check the preference regarding alert for [CellularDataDialogFragment] */
interface CellularDataInterface {
  fun cellularDataUserPreference(doNotShowAlert: Boolean, playAudio: Boolean)
}
