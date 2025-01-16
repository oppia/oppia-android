package org.oppia.android.scripts.xml

import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Script to ensure all TextView elements in layout XML files use centrally managed styles.
 *
 * Usage:
 *   bazel run //scripts:check_textview_styles -- <path_to_repository_root>
 *
 * Arguments:
 * - path_to_repository_root: The root path of the repository.
 *
 * Example:
 *   bazel run //scripts:check_textview_styles -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Usage: bazel run //scripts:check_textview_styles -- <path_to_repository_root>"
  }

  val repoRoot = File(args[0])
  require(repoRoot.exists()) { "Repository root path does not exist: ${args[0]}" }

  val resDir = File(repoRoot, "app/src/main/res")
  require(resDir.exists()) { "Resource directory does not exist: ${resDir.path}" }

  val xmlFiles = resDir.listFiles { file -> file.isDirectory && file.name.startsWith("layout") }
    ?.flatMap { dir -> dir.walkTopDown().filter { it.extension == "xml" } }
    ?: emptyList()

  val styleChecker = TextViewStyleCheck()
  styleChecker.checkFiles(xmlFiles)
}

private class TextViewStyleCheck {
  private val styleValidationIssues = mutableListOf<String>()
  private val directionalityWarnings = mutableListOf<String>()
  private val builderFactory = DocumentBuilderFactory.newInstance()

  /** Checks XML files for TextView elements to ensure compliance with style requirements. */
  fun checkFiles(xmlFiles: List<File>) {
    xmlFiles.forEach { file -> processXmlFile(file) }
    printResults()
  }

  private fun processXmlFile(file: File) {
    val document = builderFactory.newDocumentBuilder().parse(file)
    val textViewNodes = document.getElementsByTagName("TextView")
    val relativePath = file.path.substringAfter("main/res/")

    for (i in 0 until textViewNodes.length) {
      val element = textViewNodes.item(i) as Element
      validateTextViewElement(element, relativePath)
    }
  }

  private fun validateTextViewElement(element: Element, filePath: String) {
    val styleAttribute = element.attributes.getNamedItem("style")?.nodeValue
    val idAttribute = element.attributes.getNamedItem("android:id")?.nodeValue ?: "NO ID"

    if (idAttribute in attributeIds) return

    if (styleAttribute.isNullOrBlank()) {
      styleValidationIssues.add(
        "ERROR: Missing style attribute in file:" +
          " $filePath for TextView ($idAttribute)"
      )
    }

    checkForLegacyDirectionality(element, filePath, idAttribute)
  }

  private fun checkForLegacyDirectionality(
    element: Element,
    filePath: String,
    idAttribute: String
  ) {
    val legacyAttributes = mapOf(
      "android:paddingLeft" to "paddingStart",
      "android:paddingRight" to "paddingEnd",
      "android:layout_marginLeft" to "layout_marginStart",
      "android:layout_marginRight" to "layout_marginEnd",
      "android:layout_alignParentLeft" to "layout_alignParentStart",
      "android:layout_alignParentRight" to "layout_alignParentEnd",
      "android:layout_toLeftOf" to "layout_toStartOf",
      "android:layout_toRightOf" to "layout_toEndOf"
    )

    for ((legacyAttr, modernAttr) in legacyAttributes) {
      if (element.hasAttribute(legacyAttr)) {
        directionalityWarnings.add(
          "WARNING: Hardcoded left/right attribute '$legacyAttr' in file: $filePath " +
            "for TextView ($idAttribute). Consider using '$modernAttr' instead."
        )
      }
    }
  }

  private fun printResults() {
    directionalityWarnings.forEach { println(it) }

    if (styleValidationIssues.isNotEmpty()) {
      styleValidationIssues.forEach { println(it) }
      throw Exception("TEXTVIEW STYLE CHECK FAILED")
    } else if (directionalityWarnings.isEmpty()) {
      println("TEXTVIEW STYLE CHECK PASSED")
    }
  }
}

// Known exceptions that currently lack a style and are being tracked for fixes.
private val attributeIds = listOf(
  "@+id/developer_options_text_view",
  "@+id/onboarding_language_text_view",
  "@+id/walkthrough_final_no_text_view",
  "@+id/walkthrough_final_yes_text_view",
  "@+id/walkthrough_final_title_text_view",
  "@+id/chapter_index",
  "@+id/chapter_index",
  "@+id/test_text_view",
  "@+id/feedback_text_view",
  "@+id/item_selection_contents_text_view",
  "@+id/learner_analytics_sync_status_text_view",
  "@+id/text_view_for_int_no_data_binding",
  "@+id/walkthrough_topic_name_text_view",
  "@+id/walkthrough_lesson_count_text_view",
  "@+id/hint_bar_title",
  "@+id/coming_soon_text_view",
  "@+id/topic_name_text_view",
  "@+id/lesson_count_text_view",
  "@+id/multiple_choice_content_text_view",
  "@+id/language_text_view",
  "@+id/welcome_text_view",
  "@+id/app_version_text_view",
  "@+id/app_last_update_date_text_view",
  "@+id/test_margin_text_view",
  "@+id/content_text_view",
  "@+id/action_options",
  "@+id/action_help",
  "@+id/action_close",
  "@+id/continue_studying_text_view",
  "@+id/language_unavailable_notice",
  "@+id/story_progress_chapter_completed_text",
  "@+id/profile_id_view_profile_name",
  "@+id/profile_id_view_learner_id",
  "@+id/learner_events_waiting_upload_label",
  "@+id/learner_events_waiting_upload_count",
  "@+id/learner_events_uploaded_label",
  "@+id/learner_events_uploaded_count",
  "@+id/uncategorized_events_waiting_upload_label",
  "@+id/uncategorized_events_waiting_upload_count",
  "@+id/uncategorized_events_uploaded_label",
  "@+id/uncategorized_events_uploaded_count",
  "@+id/text_view_for_live_data_no_data_binding",
  "@+id/selection_interaction_textview",
  "@+id/onboarding_steps_count",
  "@+id/profile_picture_edit_dialog_view_picture",
  "@+id/profile_picture_edit_dialog_change_picture",
  "@+id/chapter_title",
  "@+id/walkthrough_welcome_title_text_view",
  "@+id/story_name_text_view",
  "@+id/topic_name_text_view",
  "@+id/chapter_index",
  "@+id/copyright_license_text_view",
  "@+id/multiple_choice_content_text_view",
  "@+id/ga_update_notice_dialog_message",
  "@+id/create_profile_picture_prompt",
  "@+id/profile_reset_pin_main",
  "@+id/submitted_answer_text_view",
  "@+id/language_text_view",
  "@+id/end_session_header_text_view",
  "@+id/end_session_body_text_view",
  "@+id/question_progress_text",
  "@+id/congratulations_text_view",
  "@+id/beta_notice_dialog_message",
  "@+id/chapter_index",
  "@+id/onboarding_language_explanation",
  "@+id/onboarding_steps_count",
  "@+id/create_profile_picture_prompt",
  "@+id/create_profile_title",
  "@+id/end_session_header_text_view",
  "@+id/end_session_body_text_view",
  "@+id/question_progress_text",
  "@+id/congratulations_text_view",
  "@+id/walkthrough_final_no_text_view",
  "@+id/walkthrough_final_yes_text_view",
  "@+id/walkthrough_final_title_text_view",
  "@+id/profile_name_text_view",
  "@+id/create_profile_picture_prompt",
  "@+id/create_profile_title",
  "@+id/end_session_header_text_view",
  "@+id/end_session_body_text_view",
  "@+id/question_progress_text",
  "@+id/congratulations_text_view",
  "@+id/resume_lesson_chapter_title_text_view",
  "@+id/topic_name_text_view",
  "@+id/story_count_text_view",
  "@+id/download_size_text_view",
  "@+id/onboarding_language_explanation",
  "@+id/onboarding_steps_count",
  "@+id/create_profile_picture_prompt",
  "@+id/create_profile_title",
  "@+id/end_session_header_text_view",
  "@+id/end_session_body_text_view",
  "@+id/question_progress_text",
  "@+id/options_activity_selected_options_title",
  "@+id/profile_select_text",
  "@+id/continue_studying_text_view",
  "@+id/extra_controls_title",
  "@+id/chapter_title",
  "@+id/options_activity_selected_options_title",
  "@+id/view_all_text_view"
)
