package org.oppia.app.audioplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.PopupMenu
import android.widget.Toast
import kotlinx.android.synthetic.main.audioplayer_fragment.*
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

class AudioPlayerFragment : InjectableFragment(), PopupMenu.OnMenuItemClickListener {
  @Inject lateinit var audioPlayerFragmentController : AudioPlayerFragmentController

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return audioPlayerFragmentController.handleCreateView(inflater, container)
  }

  override fun onStop() {
    super.onStop()
    audioPlayerFragmentController.handleOnStop()
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    lang_btn.text = item.toString()
    return true
  }
}