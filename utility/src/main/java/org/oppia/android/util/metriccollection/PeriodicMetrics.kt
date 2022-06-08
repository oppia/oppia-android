package org.oppia.android.util.metriccollection

interface PeriodicMetrics {
  fun getTotalTransmittedBytes(): Long
  fun getTotalReceivedBytes(): Long
}