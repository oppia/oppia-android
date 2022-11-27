package org.oppia.android.app.topic.conceptcard

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptCardBackStackManager @Inject constructor() {

  companion object {
    /**
     * The Default size is use to check stack size in
     * ConceptCardFragment and ConceptCardFragmentPresenter.
     */
    const val DEFAULT_STACK_SIZE = 0
  }

  private var conceptCardBackStack: ArrayDeque<String>? = null

  /** This function is for initialization of stack by
   * ConceptCardFragment. */
  fun initBackStack() {
    conceptCardBackStack = ArrayDeque()
  }

  /** This function is for adding item in stack by
   * ConceptCardFragment. */
  fun addToStack(skillId: String) {
    conceptCardBackStack?.add(skillId)
  }

  /** This function is for peeking in stack by
   * ConceptCardFragment. */
  fun peek(): String? {
    conceptCardBackStack?.let {
      return it.last()
    }
    return null
  }

  /** This function is for removing last item from stack by
   * ConceptCardFragmentPresenter. */
  fun remove() {
    conceptCardBackStack?.removeLast()
  }

  /** This function is for destroying the stack by
   * ConceptCardFragmentPresenter. */
  fun destroyBackStack() {
    conceptCardBackStack?.clear()
    conceptCardBackStack = null
  }

  /** This function is to get size of the stack by
   * ConceptCardFragment and ConceptCardFragmentPresenter. */
  fun getSize(): Int {
    conceptCardBackStack?.let {
      return it.size
    }
    return DEFAULT_STACK_SIZE
  }
}
