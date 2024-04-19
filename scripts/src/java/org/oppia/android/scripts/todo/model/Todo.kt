package org.oppia.android.scripts.todo.model

import java.io.File

/**
 * Represents the structure of TODO.
 *
 * @property file the file containing a TODO
 * @property lineNumber the line number of the line of code
 * @property lineContent the content of the line of code
 */
data class Todo(val file: File, val lineNumber: Int, val lineContent: String)
