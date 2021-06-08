package org.oppia.android.app.drawer

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import org.oppia.android.app.devoptions.DeveloperOptionsActivity

class DeveloperOptionsStarterImpl @Inject constructor() : DeveloperOptionsStarter {
  override fun createIntent(context: Context, internalProfileId: Int): Intent =
    DeveloperOptionsActivity.createDeveloperOptionsActivityIntent(context, internalProfileId)
}
