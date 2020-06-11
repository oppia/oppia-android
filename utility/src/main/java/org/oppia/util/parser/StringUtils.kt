package org.oppia.util.parser

/**
 * StringUtils to handle html tag strings.
 * [Html.TagHandler] is an interface with a single method: handleTag, which is called
 * when the parser fails to interpret a HTML tag. Since our HTML now has unknown tags
 * such as <ordered> and </ordered>, <unordered> and </unordered> and  <listitem> and </listitem>
 * the handleTag method in the CustomTagHandler() class gets called. We’re going to override handleTag
 * and tailor it to our use case.
 */
object StringUtils {
  /** Custom tag name for creating ordered lists. This replaces <ol>. */
  const val OL_TAG = "ordered"
  /** Custom tag name for creating ordered lists. This replaces <ul>. */
  const val UL_TAG = "unordered"
  /** Custom tag name for creating ordered lists. This replaces <li>. */
  const val LI_TAG = "listitem"
}
