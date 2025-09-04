package org.oppia.android.app.devoptions

/** Listener for handling clicks on the force download button. */
interface ForceDownloadsButtonClickListener {

  /** Initiates a force download of platform parameters and feature flags. */
  fun forceDownload()
}
