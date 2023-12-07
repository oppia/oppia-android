package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import org.oppia.android.app.model.ProfileId

/** Binds implementation of DeveloperOptionsStarter. */
class DeveloperOptionsStarterImpl @Inject constructor() : DeveloperOptionsStarter {
  override fun createIntent(context: Context, internalProfileId: ProfileId): Intent =
    DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, internalProfileId)
}
