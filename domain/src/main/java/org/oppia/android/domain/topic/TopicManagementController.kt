package org.oppia.android.domain.topic

import javax.inject.Inject

class TopicManagementController @Inject constructor() {
  // API:
  // - startNewDownload: returns error codes, and accepts override constraints
  // - startUpdateDownload: returns error codes, and accepts override constraints
  // - updateStatus: pauses, resumes, or cancels a download or update
  // - getStatus: returns download(ed) status; by topic ID & can filter by status type
  // - deleteDownload

  // Start download responses:
  // - FAILED_ALREADY_DOWNLOADED
  // - FAILED_INSUFFICIENT_STORAGE
  // - FAILED_NEED_DOWNLOAD_PERMISSION_CONFIRMATION
  // - FAILED_NEED_CELLULAR_CONFIRMATION
  // - SUCCESS_DOWNLOAD_QUEUED

  // Update download responses:
  // - FAILED_ALREADY_UPDATED
  // - FAILED_INCOMPATIBLE_UPDATE
  // - FAILED_INSUFFICIENT_STORAGE
  // - FAILED_NEED_DOWNLOAD_PERMISSION_CONFIRMATION
  // - FAILED_NEED_CELLULAR_CONFIRMATION
  // - SUCCESS_UPDATE_QUEUED

  // Update status responses (maybe have oneof return since failures are request based):
  // - FAILED_STATUS_CHANGED
  // - FAILED_PAUSE_NOT_DOWNLOADING
  // - FAILED_RESUME_NOT_PAUSED
  // - FAILED_RESUME_NEED_DOWNLOAD_PERMISSION_CONFIRMATION
  // - FAILED_RESUME_NEED_CELLULAR_CONFIRMATION
  // - FAILED_CANCEL_NOT_PAUSED_OR_QUEUED
  // - SUCCESS_RESUMED
  // - SUCCESS_PAUSED
  // - SUCCESS_CANCELED

  // Status responses:
  // - NotDownloaded -> includes size to download
  // - DownloadQueued -> includes size to download
  // - Downloading -> includes progress
  // - DownloadPaused -> includes progress
  // - Downloaded -> includes space consumed
  // - HasUpdate -> includes size to download
  // - UpdateQueued -> includes size to download
  // - Updating -> includes progress
  // - UpdatePaused -> includes progress

  // Delete download responses (deletes are designed to be unlikely to fail):
  // - FAILED_NOT_DOWNLOADED
  // - SUCCESS_DELETED

  // -=======================================Testing plan==========================================-

  // Initial state based on topic list. Full "download" functionality will be simulated for now.

  // Fake controller will be introduced to orchestrate download data. For now, it will be faked at
  // the controller level, but later will be faked at the network level.
}
