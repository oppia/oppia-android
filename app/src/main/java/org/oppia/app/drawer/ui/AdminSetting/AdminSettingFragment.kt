package org.oppia.app.drawer.ui.AdminSetting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.R

class AdminSettingFragment : Fragment() {

  private lateinit var adminSettingViewModel: AdminSettingViewModel

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    adminSettingViewModel =
      ViewModelProviders.of(this).get(AdminSettingViewModel::class.java)
    val root = inflater.inflate(R.layout.fragment_send, container, false)
    val textView: TextView = root.findViewById(R.id.text_send)
    adminSettingViewModel.text.observe(this, Observer {
      textView.text = it
    })
    return root
  }
}
