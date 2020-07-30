package org.oppia.util.logging

import org.oppia.app.model.ExceptionLog

/** Helper class for conversion of exceptionLog proto object to an Exception. */
class ExceptionsHelper {

  /** Converts the [exceptionLog] into an appropriate exception. */
  fun convertExceptionLogToException(exceptionLog: ExceptionLog): Exception {
    val exceptionMessage = if (exceptionLog.message != "") {
      exceptionLog.message
    } else {
      null
    }
    val exceptionCause: Throwable? =
      if (exceptionLog.cause.message == "" && exceptionLog.cause.stacktraceElementCount == 0) {
        null
      } else if (
        exceptionLog.cause.message == "" && exceptionLog.cause.stacktraceElementCount != 0
      ) {
        Throwable()
      } else {
        Throwable(exceptionLog.cause.message)
      }
    exceptionCause?.let {
      it.stackTrace = createErrorStackTrace(exceptionLog.cause)
    }
    val exception = Exception(exceptionMessage, exceptionCause)
    exception.stackTrace = createErrorStackTrace(exceptionLog)
    return exception
  }

  /** Returns an array of [StackTraceElement] for an [exceptionLog]. */
  private fun createErrorStackTrace(exceptionLog: ExceptionLog): Array<StackTraceElement> {
    return Array(
      exceptionLog.stacktraceElementCount,
      init = { i: Int ->
        StackTraceElement(
          exceptionLog.stacktraceElementList[i].declaringClass,
          exceptionLog.stacktraceElementList[i].methodName,
          exceptionLog.stacktraceElementList[i].fileName,
          exceptionLog.stacktraceElementList[i].lineNumber
        )
      }
    )
  }
}
