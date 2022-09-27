package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import com.google.protobuf.MessageLite

interface Route {
  fun <T: MessageLite> createIntent(context: Context, params: T): Intent
}