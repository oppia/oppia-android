package org.oppia.android.app.parser

/** Represents errors that can occur when parsing a text. */
enum class TextParsingError {

  /** Indicates that the considered string is a valid. */
  VALID,

  /** Indicates that the input text was empty. */
  EMPTY_INPUT
}
