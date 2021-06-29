package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent

/** Interface to create intent for [DeveloperOptionsActivity]. */
interface DeveloperOptionsStarter {
  fun createIntent(context: Context, internalProfileId: Int): Intent
}
