package org.oppia.android.app.maven


import com.google.protobuf.MessageLite

import org.oppia.android.app.maven.proto.MavenLicense
import org.oppia.android.app.maven.proto.MavenLicenses
import java.io.File
import java.io.FileInputStream

fun main(args: Array<String>) {
  println("Hello world")
  val license = MavenLicense.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val license2 = MavenLicense.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val license3 = MavenLicense.newBuilder().setLicenseName("Abhay").setPrimaryLicenseLink("garg").build()
  val rep = MavenLicenses.newBuilder()
  rep.addLicenseList(license)
  rep.addLicenseList(license2)
  rep.addLicenseList(license3)
  val lic = rep.build()
//  rep.addLicenseList(license)
  println(license)
  println(lic)
  val list = retrieveLicenseList()
  list.forEach {
    println(it)
  }
}

private fun retrieveLicenseList(): List<MavenLicense> {
  return getProto(
    "maven_dependencies.pb",
    MavenLicenses.getDefaultInstance()
  ).getLicenseListList()
}

private fun <T : MessageLite> getProto(textProtoFileName: String, proto: T): T {
  val protoBinaryFile = File("app/assets/$textProtoFileName")
  val builder = proto.newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: T =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as T
  return protoObj
}