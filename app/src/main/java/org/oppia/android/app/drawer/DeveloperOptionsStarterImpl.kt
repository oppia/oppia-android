package org.oppia.android.app.drawer

import android.content.Context
import android.content.Intent
import org.oppia.android.app.devoptions.DeveloperOptionsActivity
import javax.inject.Inject

/** Binds implementation of DeveloperOptionsStarter. */
class DeveloperOptionsStarterImpl @Inject constructor() : DeveloperOptionsStarter {
  override fun createIntent(context: Context, internalProfileId: Int): Intent =
    DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, internalProfileId)
}
