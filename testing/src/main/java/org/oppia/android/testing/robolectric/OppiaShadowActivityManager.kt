package org.oppia.android.testing.robolectric

import android.app.ActivityManager
import android.os.Debug
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

/**
 * Shadows the Activity Manager to extend its testing capabilities.
 *
 * There is an existing robolectric shadow of Activity Manager but that doesn't provide us enough
 * control over MemoryInfo and hence MemoryUsage can't be tested using that.
 */
@Implements(ActivityManager::class)
class OppiaShadowActivityManager {

  private var processMemoryInfo: Array<Debug.MemoryInfo?>? =
    arrayOf(Debug.MemoryInfo().apply { this.otherPss = 2 })

  /**
   * Sets [memoryInfo] as part of [processMemoryInfo] which is then returned whenever
   * [getProcessMemoryInfo] is called.
   */
  fun setProcessMemoryInfo(memoryInfo: Debug.MemoryInfo) {
    processMemoryInfo?.set(0, memoryInfo)
  }

  /** Returns [processMemoryInfo] as the memory usage info of the application. */
  @Implementation
  fun getProcessMemoryInfo(pids: IntArray?): Array<Debug.MemoryInfo?>? {
    return processMemoryInfo
  }
}
