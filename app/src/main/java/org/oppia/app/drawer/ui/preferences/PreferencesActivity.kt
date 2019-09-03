package org.oppia.app.drawer.ui.preferences

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import org.oppia.app.ParentActivity
import org.oppia.app.R


class PreferencesActivity : ParentActivity() {

  private lateinit var preferencesViewModel: PreferencesViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_preferences)
    preferencesViewModel =
      ViewModelProviders.of(this).get(PreferencesViewModel::class.java)
//    val textView: TextView = findViewById(R.id.text_gallery)
//    preferencesViewModel.text.observe(this, Observer {
//      textView.text = it
//    })
    init(resources.getString(R.string.menu_preferences))

  }
}