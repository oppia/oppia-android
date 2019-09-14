package org.oppia.domain

interface SeekBarListener {
  fun onDurationChanged(duration: Int)
  fun onPositionChanged(position: Int)
  fun onCompleted()
}