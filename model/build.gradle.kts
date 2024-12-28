import com.google.protobuf.gradle.*
import java.util.Properties

plugins {
  id("java-library")
  id("com.google.protobuf")
}

// See https://github.com/google/protobuf-gradle-plugin/issues/383 and
// https://github.com/google/protobuf-gradle-plugin/issues/518.
protobuf {
  protoc {
    // To build protoc in M1 mac. For context, see: #3912.
    artifact = if (osdetector.os == "osx") {
      val protobufPlatform = Properties().also {
        rootProject.file("local.properties").inputStream().use(it::load)
      }.getProperty("protobuf_platform", "")
      "com.google.protobuf:protoc:3.8.0:${protobufPlatform}"
    } else "com.google.protobuf:protoc:3.8.0"
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        getByName("java") {
          // This setup is per https://github.com/google/protobuf-gradle-plugin/issues/315.
          option("lite")
        }
      }
    }
  }
}

// Fix an issue that seems to arise in Gradle 7.2+ whereby the proto files are duplicated in the
// source set (only noticeable when building :model:processResources).
tasks.withType(Copy::class).all {
  // See https://github.com/google/protobuf-gradle-plugin/issues/522#issuecomment-1195266995.
  duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
}

dependencies {
  implementation("com.google.protobuf:protobuf-javalite:3.17.3")
}

sourceSets {
  getByName("main") {
    proto {
      srcDir("src/main/proto")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}
