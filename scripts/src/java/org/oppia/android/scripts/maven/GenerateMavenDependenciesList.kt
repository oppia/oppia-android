package org.oppia.android.scripts.maven

/** Runs the [MavenDependenciesListWriter.kt] to updated maven_dependencies.textproto. */
fun main(args: Array<String>) {

  MavenDependenciesListWriter.networkAndBazelUtils = NetworkAndBazelUtilsImpl()

  MavenDependenciesListWriter.main(args)
}
