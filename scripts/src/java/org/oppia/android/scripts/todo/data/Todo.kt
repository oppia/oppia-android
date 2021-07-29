package org.oppia.android.scripts.todo.data

/**
 * Represents the structure of TODO.
 * @property filePath the path of the file
 * @property lineNumber the line number of the line of code
 * @property lineContent the content of the line of code
 */
data class Todo(val filePath: String, val lineNumber: Int, val lineContent: String)
