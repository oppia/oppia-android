import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.proto
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
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}
