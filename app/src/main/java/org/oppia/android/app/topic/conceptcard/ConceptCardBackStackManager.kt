package org.oppia.android.app.topic.conceptcard

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptCardBackStackManager @Inject constructor() {

  companion object {
    const val DEFAULT_STACK_SIZE = -1
  }

  private var conceptCardBackStack: ArrayDeque<String>? = null
  val stackSize: MutableLiveData<Int> = MutableLiveData(DEFAULT_STACK_SIZE)

  fun initBackStack() {
    conceptCardBackStack = ArrayDeque()
  }

  fun addToStack(skillId: String) {
    conceptCardBackStack?.add(skillId)
    stackSize.value = conceptCardBackStack?.size
  }

  fun peek(): String? {
    return conceptCardBackStack?.last()
  }

  fun remove() {
    conceptCardBackStack?.removeLast()
    stackSize.value = conceptCardBackStack?.size
  }

  fun destroyBackStack() {
    conceptCardBackStack = null
    stackSize.value = DEFAULT_STACK_SIZE
  }
}
