package org.oppia.app.audioplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import javax.inject.Inject
import kotlinx.android.synthetic.main.audioplayer_fragment.view.*
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.R
import org.oppia.domain.audio.AudioPlayerController

@FragmentScope
class AudioPlayerFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val audioPlayerController: AudioPlayerController
) {
  private var userIsSeeking : Boolean = false
  private lateinit var seekBar: SeekBar
  private lateinit var controlButton: Button

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val view = inflater.inflate(R.layout.audioplayer_fragment, container, false)

    view.lang_btn.setOnClickListener {
      val popupMenu = PopupMenu(fragment.context, view.lang_btn)
      popupMenu.setOnMenuItemClickListener(fragment as PopupMenu.OnMenuItemClickListener)
      popupMenu.inflate(R.menu.audio_lang_menu)
      popupMenu.show()
    }

    audioPlayerController.initializeMediaPlayer("https://ccrma.stanford.edu/~jos/mp3/bachfugue.mp3", SeekBarListener())
    audioPlayerController.getPlayState().observe(fragment, Observer { playStatus ->
      when (playStatus.type) {
        "DURATION" -> seekBar.max = playStatus.value
        "POSITION" -> if (!userIsSeeking) seekBar.progress = playStatus.value
        "COMPLETE" -> controlButton.text = fragment.getString(R.string.audio_player_play)
      }
    })


    controlButton = view.control_btn
    controlButton.setOnClickListener {
      if (audioPlayerController.isPrepared()) {
        if (audioPlayerController.isPlaying()) {
          audioPlayerController.pause()
          controlButton.text = fragment.getString(R.string.audio_player_play)
        } else {
          audioPlayerController.play()
          controlButton.text = fragment.getString(R.string.audio_player_pause)
        }
      }
    }

    seekBar = view.seek_bar
    seekBar.setOnSeekBarChangeListener (object: SeekBar.OnSeekBarChangeListener {
      private var userProgress = 0
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) userProgress = progress
      }
      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        userIsSeeking = true
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        userIsSeeking = false
        audioPlayerController.seekTo(userProgress)
      }
    })
    return view
  }

  fun handleOnStop() = audioPlayerController.release()

  inner class SeekBarListener : org.oppia.domain.audio.SeekBarListener {
    override fun onDurationChanged(duration: Int) {
      seekBar.max = duration
    }

    override fun onPositionChanged(position: Int) {
      if (!userIsSeeking) seekBar.progress = position
    }

    override fun onCompleted() {
      controlButton.text = fragment.getString(R.string.audio_player_play)
    }
  }
}
