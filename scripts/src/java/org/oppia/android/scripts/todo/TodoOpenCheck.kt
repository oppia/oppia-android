package org.oppia.android.scripts.todo

import com.google.protobuf.TextFormat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.oppia.android.scripts.common.BinaryProtoResourceLoader
import org.oppia.android.scripts.common.BinaryProtoResourceLoader.Companion.loadProto
import org.oppia.android.scripts.common.BinaryProtoResourceLoaderImpl
import org.oppia.android.scripts.proto.TodoOpenExemption
import org.oppia.android.scripts.proto.TodoOpenExemptions
import org.oppia.android.scripts.todo.model.Issue
import org.oppia.android.scripts.todo.model.Todo
import java.io.File

/**
 * Script for ensuring that all TODOs present in the repository are correctly formatted and
 * corresponds to open issues on GitHub.
 *
 * Usage:
 *   bazel run //scripts:todo_open_check -- <path_to_directory_root> <path_to_json_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_json_file: path to the json file containing the list of all open issues on Github
 *
 * Example:
 *   bazel run //scripts:todo_open_check -- $(pwd) open_issues.json
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment. The CI workflow creates a
 * json file from the GitHub api which contains a list of all open issues of the
 * oppia/oppia-android repository. To execute it without the CI, please create open issues json
 * file and provide its path to the script in the format as stated above.
 *
 * Instructions to create the open_issues.json file:
 * 1. Set up Github CLI Tools locally.
 * 2. cd to the oppia-android repository.
 * 3. Run the command:
 *   gh issue list --limit 2000 --repo oppia/oppia-android --json number > $(pwd)/open_issues.json
 */
fun main(vararg args: String) {
  // The first argument is the path of the repo to be analyzed.
  val repoRoot = File("${args[0]}/").absoluteFile.normalize()

  // Path to the JSON file containing the list of open issues.
  val openIssuesJsonFile = File(repoRoot, args[1])
  check(openIssuesJsonFile.exists()) {
    "${openIssuesJsonFile.toRelativeString(repoRoot)}: No such file exists"
  }

  val checker = TodoOpenCheck(repoRoot)
  checker.checkForOpenTodos(openIssuesJsonFile, regenerateFile = args.getOrNull(2).toBoolean())
}

/**
 * Utility for checking the formatting of TODOs in the project at [repoRoot], and ensuring that
 * these TODOs are still open on GitHub.
 *
 * @param repoRoot the root directory of the repository
 * @param binaryProtoResourceLoader the resource loader to use when loading binary proto resources
 */
class TodoOpenCheck(
  private val repoRoot: File,
  private val binaryProtoResourceLoader: BinaryProtoResourceLoader = BinaryProtoResourceLoaderImpl()
) {
  /**
   * Checks for valid and open TODOs throughout all Kotlin files in [repoRoot].
   *
   * @param openIssuesJsonFile the GitHub CLI-generated list of open issues in the project's remote
   *     GitHub repository (in the tool's JSON format)
   * @param regenerateFile whether to regenerate the project's exemptions file and output it as a
   *     textproto
   */
  fun checkForOpenTodos(openIssuesJsonFile: File, regenerateFile: Boolean) {
    // List of all the open issues on GitHub of this repository.
    val openIssueList = retrieveOpenIssueList(openIssuesJsonFile)
    val todoExemptionList =
      binaryProtoResourceLoader.loadProto(
        javaClass, "assets/todo_open_exemptions.pb", TodoOpenExemptions.getDefaultInstance()
      ).todoOpenExemptionList

    val allTodos = TodoCollector.collectTodos(repoRoot.path)
    val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(allTodos)
    val correctlyFormattedTodos =
      TodoCollector.collectCorrectlyFormattedTodos(allTodos - poorlyFormattedTodos.toSet())

    val openIssueFailureTodos = correctlyFormattedTodos.filter { todo ->
      checkIfIssueDoesNotMatchOpenIssue(codeLine = todo.lineContent, openIssueList = openIssueList)
    }
    val redundantExemptions = retrieveRedundantExemptions(
      todos = poorlyFormattedTodos + openIssueFailureTodos, todoExemptionList, repoRoot
    )

    val poorlyFormattedTodosAfterExemption =
      retrieveTodosAfterExemption(todos = poorlyFormattedTodos, todoExemptionList, repoRoot)
    val openIssueFailureTodosAfterExemption =
      retrieveTodosAfterExemption(todos = openIssueFailureTodos, todoExemptionList, repoRoot)

    logRedundantExemptions(redundantExemptions)
    logFailures(
      invalidTodos = poorlyFormattedTodosAfterExemption,
      repoRoot,
      failureMessage = "TODOs not in correct format:",
    )
    logFailures(
      invalidTodos = openIssueFailureTodosAfterExemption,
      repoRoot,
      failureMessage = "TODOs not corresponding to open issues on GitHub:",
    )

    if (poorlyFormattedTodosAfterExemption.isNotEmpty() ||
      openIssueFailureTodosAfterExemption.isNotEmpty()
    ) {
      println(
        "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
          "#todo-open-checks for more details on how to fix this.\n"
      )
    }
    if (
      redundantExemptions.isNotEmpty() ||
      poorlyFormattedTodosAfterExemption.isNotEmpty() ||
      openIssueFailureTodosAfterExemption.isNotEmpty()
    ) {
      if (regenerateFile) {
        println("Regenerated exemptions:")
        println()
        val allProblematicTodos = poorlyFormattedTodos + openIssueFailureTodos
        val newExemptions = allProblematicTodos.convertToExemptions(repoRoot)
        println(newExemptions.convertToExemptionTextProto())
      } else {
        println(
          "There were failures. Re-run the command with \"true\" at the end to regenerate the" +
            " exemption file with all failures as exempted."
        )
      }
      println()
      throw Exception("TODO CHECK FAILED")
    } else {
      println("TODO CHECK PASSED")
    }
  }

  /**
   * Retrieves the TODO open check failures list after filtering them from the exemptions.
   *
   * @param todos the list of all the failure causing TODOs
   * @param todoExemptionList the list contating the TODO exemptions
   * @param repoRoot the root directory of the repository
   * @return list obtained after filtering the exemptions
   */
  private fun retrieveTodosAfterExemption(
    todos: List<Todo>,
    todoExemptionList: List<TodoOpenExemption>,
    repoRoot: File
  ): List<Todo> {
    return todos.filter { todo ->
      todoExemptionList.none {
        it.exemptedFilePath == todo.file.toRelativeString(repoRoot) &&
          todo.lineNumber in it.lineNumberList
      }
    }
  }

  /**
   * Retrieves a list of redundant exemptions.
   *
   * @param todos the list of all the failure causing TODOs
   * @param todoExemptionList the list contating the TODO exemptions
   * @param repoRoot the root directory of the repository
   * @return a list of all the redundant exemptions
   */
  private fun retrieveRedundantExemptions(
    todos: List<Todo>,
    todoExemptionList: List<TodoOpenExemption>,
    repoRoot: File
  ): List<Pair<String, Int>> {
    return todoExemptionList.flatMap { exemption ->
      exemption.lineNumberList.mapNotNull { exemptedLineNumber ->
        val isRedundantExemption = todos.none {
          it.file.toRelativeString(repoRoot) == exemption.exemptedFilePath &&
            it.lineNumber == exemptedLineNumber
        }
        if (isRedundantExemption) {
          Pair(exemption.exemptedFilePath, exemptedLineNumber)
        } else {
          null
        }
      }
    }
  }

  /**
   * Checks whether a TODO does not corresponds to open issues on GitHub.
   *
   * @param codeLine the line of code to be checked
   * @param openIssueList the list of all the open issues of this repository on GitHub
   * @return whether the TODO does not corresponds to open issues on GitHub
   */
  private fun checkIfIssueDoesNotMatchOpenIssue(
    codeLine: String,
    openIssueList: List<Issue>,
  ): Boolean {
    val parsedIssueNumberFromTodo = TodoCollector.parseIssueNumberFromTodo(codeLine)
    return openIssueList.none { it.issueNumber == parsedIssueNumberFromTodo }
  }

  /**
   * Logs the redundant exemptions.
   *
   * @param redundantExemptions list of redundant exemptions
   */
  private fun logRedundantExemptions(redundantExemptions: List<Pair<String, Int>>) {
    if (redundantExemptions.isNotEmpty()) {
      println("Redundant exemptions (there are no TODOs corresponding to these lines):")
      redundantExemptions.sortedWith(compareBy({ it.first }, { it.second })).forEach { exemption ->
        println("- ${exemption.first}:${exemption.second}")
      }
      println("Please remove them from todo_open_exemptions.textproto")
      println()
    }
  }

  /**
   * Logs the TODO open check failures.
   *
   * @param invalidTodos a list of all the invalid TODOs present in the repository. A TODO is
   *     considered to be invalid if it is poorly formatted or if it does not corresponds to open
   *     issues on GitHub.
   * @param repoRoot the root directory of the repository
   * @param failureMessage the failure message to be logged
   */
  private fun logFailures(invalidTodos: List<Todo>, repoRoot: File, failureMessage: String) {
    if (invalidTodos.isNotEmpty()) {
      println(failureMessage)
      invalidTodos.sortedWith(compareBy({ it.file.path }, { it.lineNumber })).forEach {
        println("- ${it.file.toRelativeString(repoRoot)}:${it.lineNumber}")
      }
      println()
    }
  }

  private fun List<Todo>.convertToExemptions(repoRoot: File): List<TodoOpenExemption> {
    return groupBy { it.file.path }.map { (_, todos) ->
      TodoOpenExemption.newBuilder().apply {
        exemptedFilePath = todos.first().file.toRelativeString(repoRoot)
        addAllLineNumber(todos.map { it.lineNumber }.sorted())
      }.build()
    }.sortedBy { it.exemptedFilePath }
  }

  private fun List<TodoOpenExemption>.convertToExemptionTextProto(): String {
    val baseProto = TodoOpenExemptions.newBuilder().apply {
      addAllTodoOpenExemption(this@convertToExemptionTextProto)
    }.build()
    return TextFormat.printer().printToString(baseProto)
  }

  /**
   * Retrieves the list of all open issues on GitHub by parsing the JSON file generated by the GitHub
   * API.
   *
   * @param openIssuesJsonFile file containing all the open issues of the repository
   * @return list of all open issues
   */
  private fun retrieveOpenIssueList(openIssuesJsonFile: File): List<Issue> {
    val openIssuesJsonText = openIssuesJsonFile
      .inputStream()
      .bufferedReader()
      .use { it.readText() }
    val moshi = Moshi.Builder().build()
    val listType = Types.newParameterizedType(List::class.java, Issue::class.java)
    val adapter: JsonAdapter<List<Issue>> = moshi.adapter(listType)
    return adapter.fromJson(openIssuesJsonText)
      ?: throw Exception("Failed to parse $openIssuesJsonFile")
  }
}
