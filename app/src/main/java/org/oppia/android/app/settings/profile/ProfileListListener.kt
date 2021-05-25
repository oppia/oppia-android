package org.oppia.android.app.settings.profile

interface ProfileListListener {
  fun updateToolbarTitle(title: String)
  fun toolbarListener(function: () -> Unit)
}
