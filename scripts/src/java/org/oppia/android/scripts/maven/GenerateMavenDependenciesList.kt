package org.oppia.android.scripts.maven

fun main(args: Array<String>) {

  MavenDependenciesListWriter.utilityProvider = UtilityProviderImpl()

  MavenDependenciesListWriter.main(args)
}
