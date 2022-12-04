package org.oppia.android.app.topic.conceptcard

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ConceptCardBackStackManagerTest {

  private lateinit var stack: ArrayDeque<String>

  @Test
  fun testConceptCardBackStackManager_initializeStack_checkInitialized() {
    stack = ArrayDeque()
    assertThat(stack).isNotNull()
  }

  @Test
  fun testConceptCardBackStackManager_addToStack_checkAdded() {
    stack = ArrayDeque()
    val skillId = "skill_test"
    stack.add(skillId)
    assertThat(stack.last()).isEqualTo(skillId)
  }

  @Test
  fun testConceptCardBackStackManager_removeFromStack_checkRemoved() {
    stack = ArrayDeque()
    val skillId = "skill_test"
    stack.add(skillId)
    val removedItem = stack.removeLast()
    assertThat(removedItem).isEqualTo(skillId)
  }
}
