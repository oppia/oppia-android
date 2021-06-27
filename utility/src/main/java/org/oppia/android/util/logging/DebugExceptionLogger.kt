package org.oppia.android.util.logging

import javax.inject.Inject
import javax.inject.Singleton

/** A debug specific implementation for the exception logger. */
@Singleton
class DebugExceptionLogger @Inject constructor() : ExceptionLogger {

  private val exceptionList = ArrayList<Exception>()

  override fun logException(exception: Exception) {
    exceptionList.add(exception)
  }

  /** Returns eventList. */
  fun getExceptionList(): ArrayList<Exception> = exceptionList
}
