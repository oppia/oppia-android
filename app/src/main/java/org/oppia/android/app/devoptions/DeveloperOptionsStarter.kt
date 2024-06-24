package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import org.oppia.android.app.model.ProfileId

/** Interface to create intent for [DeveloperOptionsActivity]. */
interface DeveloperOptionsStarter {
  fun createIntent(context: Context, profileId: ProfileId): Intent
}
