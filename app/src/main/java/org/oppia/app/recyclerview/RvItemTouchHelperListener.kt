package org.oppia.app.recyclerview

/** Listener for handling action of  [RvItemTouchHelperCallback]. */
interface RvItemTouchHelperListener {
  fun onMove(fromPosition: Int, toPosition: Int)
}
