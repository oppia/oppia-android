package org.oppia.android.scripts.regex

import com.google.protobuf.MessageLite

import org.oppia.android.scripts.proto.License
import org.oppia.android.scripts.proto.Licenses
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
//  val licensem = license {
//    this.licenseName = "LicenseName"
//    this.licenseLink = "LicenseLink"
//  }
//  println(licensem)
  val lic = rep.build()
//  rep.addLicenseList(license)
  println(license)
  println(lic)
}