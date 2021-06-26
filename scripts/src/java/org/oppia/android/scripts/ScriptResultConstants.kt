package org.oppia.android.scripts

/** object containing failure and pass constants for all the scripts. */
object ScriptResultConstants {
  /** RegexPatternValidationCheck output message constants. **/
  val REGEX_CHECKS_PASSED: String = "REGEX PATTERN CHECKS PASSED"
  val REGEX_CHECKS_FAILED: String = "REGEX PATTERN CHECKS FAILED"

  /** XMLSyntaxCheck output message constants **/
  val XML_SYNTAX_CHECK_PASSED: String = "XML SYNTAX CHECK PASSED"
  val XML_SYNTAX_CHECK_FAILED: String = "XML SYNTAX CHECK FAILED"

  /** TestFileCheck output message constants **/
  val TEST_FILE_CHECK_PASSED: String = "TEST FILE CHECK PASSED"
  val TEST_FILE_CHECK_FAILED: String = "TEST FILE CHECK FAILED"

  /** AccessibilityLabelCheck output message constants **/
  val ACCESSIBILITY_LABEL_CHECK_PASSED: String = "ACCESSIBILITY LABEL CHECK PASSED"
  val ACCESSIBILITY_LABEL_CHECK_FAILED: String = "ACCESSIBILITY LABEL CHECK FAILED"
}
