package org.oppia.app.player.state.listener

/** This interface helps to know when a button has been clicked. */
interface StateKeyboardButtonListener {
  fun onEditorAction(actionCode: Int)
}
