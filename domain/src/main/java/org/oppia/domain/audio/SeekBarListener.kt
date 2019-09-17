package org.oppia.domain.audio

interface SeekBarListener {
  fun onDurationChanged(duration: Int)
  fun onPositionChanged(position: Int)
  fun onCompleted()
}