package org.oppia.app.topic_index;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.oppia.app.R

/** This activity will display all the topics summary. */
class TopicIndexActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home_activity)
    supportFragmentManager.beginTransaction().add(R.id.home_fragment_placeholder, TopicIndexFragment()).commitNow()
  }
}
