package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import com.google.protobuf.MessageLite

/** Interface to create intent with generic proto. */
interface Route {
  /**
   * Called to create Intent from proto.
   *
   * @param context the activity context.
   * @param params the generic proto.
   * @return Intent created from given context and params.
   */
  fun <T : MessageLite> createIntent(context: Context, params: T): Intent
}
