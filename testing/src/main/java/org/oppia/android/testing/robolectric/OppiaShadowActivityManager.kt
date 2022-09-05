package org.oppia.android.testing.robolectric

import android.app.ActivityManager
import android.os.Debug
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowActivityManager

/**
 * Shadows the Activity Manager to extend its testing capabilities.
 *
 * There is an existing robolectric shadow of Activity Manager but that doesn't provide us enough
 * control over Debug.MemoryInfo and hence memory usage can't be tested using that.
 */
@Implements(ActivityManager::class)
class OppiaShadowActivityManager : ShadowActivityManager() {

  private var processMemoryInfo: Array<Debug.MemoryInfo?>? = arrayOf(Debug.MemoryInfo())

  private var memoryInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()

  /**
   * Sets [memoryInfo] as part of [processMemoryInfo] which is then returned whenever
   * [getProcessMemoryInfo] is called.
   */
  fun setProcessMemoryInfo(memoryInfo: Debug.MemoryInfo) {
    processMemoryInfo?.set(0, memoryInfo)
  }

  /** Sets [memoryInfo] as equal to [memoryInfoValue]. */
  override fun setMemoryInfo(memoryInfoValue: ActivityManager.MemoryInfo) {
    this.memoryInfo = memoryInfoValue
  }

  /**
   * Robolectric shadow override of [ActivityManager.getProcessMemoryInfo]. Note that the value of
   * [pids] isn't taken into account in this implementation unlike the actual one.
   */
  @Implementation
  fun getProcessMemoryInfo(pids: IntArray?): Array<Debug.MemoryInfo?>? {
    return processMemoryInfo
  }

  /** Robolectric shadow override of [ActivityManager.getMemoryInfo]. */
  @Implementation
  public override fun getMemoryInfo(outInfo: ActivityManager.MemoryInfo) {
    outInfo.apply {
      availMem = memoryInfo.availMem
      outInfo.lowMemory = memoryInfo.lowMemory
      outInfo.threshold = memoryInfo.threshold
      outInfo.totalMem = memoryInfo.totalMem
    }
  }
}
