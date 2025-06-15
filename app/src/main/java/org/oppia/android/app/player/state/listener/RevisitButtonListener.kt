package org.oppia.android.app.player.state.listener

/** Listener for when the 'Revisit Previous Question' button is clicked. */ //subha 1.4 final
interface RevisitButtonListener {
  fun onFlashbackButtonClicked(stateName: String)
}