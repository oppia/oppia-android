package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import org.oppia.android.app.model.LegacyProfileId
import javax.inject.Inject

/** Binds implementation of DeveloperOptionsStarter. */
class DeveloperOptionsStarterImpl @Inject constructor() : DeveloperOptionsStarter {
  override fun createIntent(context: Context, profileId: LegacyProfileId): Intent =
    DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, profileId)
}
