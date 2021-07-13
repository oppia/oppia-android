package org.oppia.android.scripts.maven

fun main(args: Array<String>) {

  MavenDependenciesListWriter.networkAndBazelUtils = NetworkAndBazelUtilsImpl()

  MavenDependenciesListWriter.main(args)
}
