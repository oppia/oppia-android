package org.oppia.app.drawer.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.R


class PreferencesFragment : Fragment() {

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    preferencesViewModel =
      ViewModelProviders.of(this).get(PreferencesViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_preferences, container, false)
    val textView: TextView = root.findViewById(R.id.text_gallery)
    preferencesViewModel.text.observe(this, Observer {
      textView.text = it
    })
    return root
  }
}