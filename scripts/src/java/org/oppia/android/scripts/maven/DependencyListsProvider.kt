package org.oppia.android.scripts.maven

import java.io.File
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.proto.MavenDependencyList

/** Provides dependency Lists to class for executing commands on the local filesystem. */
interface DependencyListsProvider {
  /**
   * Executes the specified [command] in the specified working directory [workingDir] with the
   * provided arguments being passed as arguments to the command.
   *
   * Any exceptions thrown when trying to execute the application will be thrown by this method.
   * Any failures in the underlying process should not result in an exception.
   *
   * @param includeErrorOutput whether to include error output in the returned [CommandResult],
   *     otherwise it's discarded
   * @return a [CommandResult] that includes the error code & application output
   */

  fun provideDependencyListFromPom(finalDependenciesList: List<MavenListDependency>): MavenDependencyList

  fun provideBazelQueryDependencyList(
    rootPath: String
  ): List<String>

  fun getDependencyListFromMavenInstall(
    pathToMavenInstall: String,
    bazelQueryDepsNames: List<String>
  ): List<MavenListDependency>

}
