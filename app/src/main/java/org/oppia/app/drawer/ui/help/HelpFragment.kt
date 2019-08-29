package org.oppia.app.drawer.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.R

class HelpFragment : Fragment() {

  private lateinit var helpViewModel: HelpViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    helpViewModel =
      ViewModelProviders.of(this).get(HelpViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_help, container, false)
    val textView: TextView = root.findViewById(R.id.text_slideshow)
    helpViewModel.text.observe(this, Observer {
      textView.text = it
    })
    return root
  }
}