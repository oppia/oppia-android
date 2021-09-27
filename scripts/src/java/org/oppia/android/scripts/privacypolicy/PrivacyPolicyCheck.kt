package org.oppia.android.scripts.license

import java.io.File

/**
 * Usage:
 *   bazel run //scripts:privacy_policy_date_check -- <path_to_third_party_deps_xml>
 *
 * Arguments:
 * - path_to_third_party_deps_xml: path to the third_party_dependencies.xml
 *
 * Example:
 *   bazel run //scripts:privacy_policy_date_check -- $(pwd)/app/src/main/res/values/privacy_policy.xml
 */
fun main(args: Array<String>) {
  if (args.size < 1) {
    throw Exception("Too few arguments passed")
  }
  val pathToPrivacyPolicyXml = args[0]
  val privacyPolicyXml = File(pathToPrivacyPolicyXml)
  check(privacyPolicyXml.exists()) { "File does not exist: $privacyPolicyXml" }

  val xmlContent = privacyPolicyXml.readText()

  checkIfCommentIsPresent(xmlContent = xmlContent, comment = WARNING_COMMENT)

  println("License texts Check Passed")
}

private fun checkIfCommentIsPresent(xmlContent: String, comment: String) {
  if (comment !in xmlContent) {
    println("Please revert the changes in privacy_policy.xml")
    throw Exception("Privacy policy potentially checked into VCS")
  }
}
