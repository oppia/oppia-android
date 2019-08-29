package org.oppia.app.drawer.ui.mydownloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.R

class MyDownloadsFragment : Fragment() {

  private lateinit var myDownloads: MyDownloadsViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    myDownloads =
      ViewModelProviders.of(this).get(MyDownloadsViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_my_downloads, container, false)
    val textView: TextView = root.findViewById(R.id.text_tools)
    myDownloads.text.observe(this, Observer {
      textView.text = it
    })
    return root
  }
}