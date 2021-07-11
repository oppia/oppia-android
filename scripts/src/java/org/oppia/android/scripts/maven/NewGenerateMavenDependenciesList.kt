package org.oppia.android.scripts.maven

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.maven.maveninstall.MavenListDependency
import org.oppia.android.scripts.maven.maveninstall.MavenListDependencyTree
import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.MavenDependency
import org.oppia.android.scripts.proto.MavenDependencyList
import org.oppia.android.scripts.proto.OriginOfLicenses
import org.oppia.android.scripts.proto.PrimaryLinkType
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {

  val pathToRoot = args[0]
  val pathToMavenInstall = "$pathToRoot/${args[1]}"
  val pathToMavenDependenciesTextProto = "$pathToRoot/${args[2]}"

  val dependencyListProvider = DependencyListsProviderImpl()

  val bazelQueryDepsList = dependencyListProvider.provideBazelQueryDependencyList(pathToRoot)
  val mavenInstallDepsList = dependencyListProvider.getDependencyListFromMavenInstall(
    pathToMavenInstall,
    bazelQueryDepsList
  )
  val pomDepsList = dependencyListProvider.provideDependencyListFromPom(mavenInstallDepsList)
  MavenDependenciesListWriter.setCPathToMavenDependenciesTextProto(pathToMavenDependenciesTextProto)
  MavenDependenciesListWriter.setCDependenciesListFromPom(pomDepsList.mavenDependencyList)

  val mavenDependenciesListWriter = MavenDependenciesListWriter.main(
    arrayOf()
  )
}