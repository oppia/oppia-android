package org.oppia.android.app.maven


import com.google.protobuf.MessageLite

import org.oppia.android.app.maven.proto.License
import org.oppia.android.app.maven.proto.Licenses
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>) {
  println("Hello world")
  val license = License.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val license2 = License.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val license3 = License.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val rep = Licenses.newBuilder()
  rep.addLicenseList(license)
  rep.addLicenseList(license2)
  rep.addLicenseList(license3)
  val lic = rep.build()
//  rep.addLicenseList(license)
  println(license)
  println(lic)
}