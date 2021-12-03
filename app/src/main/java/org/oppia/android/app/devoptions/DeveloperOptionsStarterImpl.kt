package org.oppia.android.app.devoptions

import android.content.Context
import android.content.Intent
import javax.inject.Inject

/** Binds implementation of DeveloperOptionsStarter. */
class DeveloperOptionsStarterImpl @Inject constructor() : DeveloperOptionsStarter {
  override fun createIntent(context: Context, internalProfileId: Int): Intent =
    DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, internalProfileId)
}
