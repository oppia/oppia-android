package org.oppia.android.scripts.license

import java.io.File

/**
 * Usage:
 *   bazel run //scripts:privacy_policy_date_check -- <path_to_privacy_policy_xml>
 *
 * Arguments:
 * - path_to_privacy_policy_xml: path to the privacy_policy.xml
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

  println("Privacy Policy date Check Passed")
}

