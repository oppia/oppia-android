package org.oppia.android.scripts.instrumentation

import com.android.dexdeps.DexData
import com.android.dexdeps.InputStreamDataSource
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.PrintStream
import java.util.zip.ZipFile
import org.oppia.android.scripts.common.FileUtils.openFile
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedFile

fun main(vararg args: String) {
  require(args.size == 2) {
    "Usage: bazel run //scripts:generate_instrumentation_manifest --" +
      " <path_to_instrumentation_binary_apk_file> <path_to_output_manifest_file>"
  }
  GenerateInstrumentationManifest().generateManifest(
    instrumentationBinaryApkPath = args[0], manifestFilePath = args[1]
  )
}

class GenerateInstrumentationManifest {
  fun generateManifest(instrumentationBinaryApkPath: String, manifestFilePath: String) {
    val instrumentationBinaryApkFile = openFile(instrumentationBinaryApkPath) {
      "Expected instrumentation binary APK to exist: $instrumentationBinaryApkPath."
    }
    val ignoredActivities = computeIgnoredActivities(instrumentationBinaryApkFile)
    val ignoredServices = IGNORED_SERVICES.map { IgnorableNode.Service(name = it) }
    val ignoredProviders = IGNORED_PROVIDERS.map { IgnorableNode.Provider(name = it) }
    val ignoredReceivers = IGNORED_RECEIVERS.map { IgnorableNode.Receiver(name = it) }
    println(
      "Ignoring ${ignoredActivities.size} activities, ${ignoredServices.size} services," +
        " ${ignoredProviders.size} providers, and ${ignoredReceivers.size} receivers in" +
        " instrumentation manifest."
    )
    val manifestContents =
      generateManifestContents(
        ignoredActivities + ignoredServices + ignoredProviders + ignoredReceivers
      )
    val manifestFile = File(manifestFilePath).toAbsoluteNormalizedFile()
    PrintStream(manifestFile.outputStream()).use { printStream ->
      printStream.println(manifestContents)
    }
  }

  private fun computeIgnoredActivities(
    instrumentationBinaryApkFile: File
  ): List<IgnorableNode.Activity> {
    return retrieveDexFileClassNames(instrumentationBinaryApkFile).map {
      it.removePrefix("L").removeSuffix(";").replace('/', '.')
    }.filter {
      it.startsWith("org.oppia.android") && it.endsWith("Activity")
    }.map(IgnorableNode::Activity)
  }

  private companion object {
    private val IGNORED_SERVICES = listOf(
      "androidx.room.MultiInstanceInvalidationService",
      "androidx.work.impl.background.systemalarm.SystemAlarmService",
      "androidx.work.impl.background.systemjob.SystemJobService",
      "androidx.work.impl.foreground.SystemForegroundService",
      "com.google.android.datatransport.runtime.backends.TransportBackendDiscovery",
      "com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService",
      "com.google.android.gms.measurement.AppMeasurementService",
      "com.google.android.gms.measurement.AppMeasurementJobService",
      "com.google.firebase.components.ComponentDiscoveryService"
    )

    private val IGNORED_PROVIDERS = listOf(
      "androidx.lifecycle.ProcessLifecycleOwnerInitializer",
      "com.crashlytics.android.CrashlyticsInitProvider",
      "com.google.firebase.provider.FirebaseInitProvider"
    )

    private val IGNORED_RECEIVERS = listOf(
      "androidx.work.impl.utils.ForceStopRunnable${"$"}BroadcastReceiver",
      "androidx.work.impl.background.systemalarm.ConstraintProxy${"$"}BatteryChargingProxy",
      "androidx.work.impl.background.systemalarm.ConstraintProxy${"$"}BatteryNotLowProxy",
      "androidx.work.impl.background.systemalarm.ConstraintProxy${"$"}StorageNotLowProxy",
      "androidx.work.impl.background.systemalarm.ConstraintProxy${"$"}NetworkStateProxy",
      "androidx.work.impl.background.systemalarm.RescheduleReceiver",
      "androidx.work.impl.background.systemalarm.ConstraintProxyUpdateReceiver",
      "androidx.work.impl.diagnostics.DiagnosticsReceiver",
      "com.google.android.datatransport.runtime.scheduling.jobscheduling" +
        ".AlarmManagerSchedulerBroadcastReceiver",
      "com.google.android.gms.measurement.AppMeasurementReceiver",
      "com.google.firebase.iid.FirebaseInstanceIdReceiver"
    )

    private fun generateManifestContents(ignorableNodes: List<IgnorableNode>): String {
      // See https://developer.android.com/studio/build/manage-manifests for the different options
      // available for configuring manifest merging.
      // TODO: Set up Espresso tests to not need special handling for services, providers, receivers,
      //  etc. (at the moment, //app is adding extra activities that shouldn't be needed). Requires
      //  finishing up Bazel migration for //app.
      return """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest
            xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            package="org.oppia.android.test">
            <uses-sdk
                android:minSdkVersion="19"
                android:targetSdkVersion="30" />
            <instrumentation
                android:label="Tests for org.oppia.android"
                android:name="org.oppia.android.testing.OppiaTestRunner"
                android:targetPackage="org.oppia.android"
                android:handleProfiling="false"
                android:functionalTest="false" />
            <application
                android:debuggable="true"
                tools:remove="android:name,android:allowBackup">
                <uses-library android:name="android.test.runner" />
                ${ignorableNodes.joinToString(separator = "\n") {
                  "                ${it.generateIgnorableMarkup()}"
                }.trim()}
            </application>
        </manifest>""".trimIndent()
    }

    private fun retrieveDexFileClassNames(apkFile: File): List<String> {
      return ZipFile(apkFile.toAbsoluteNormalizedFile()).use { zipFile ->
        zipFile.entries().asSequence().filter { zipEntry ->
          !zipEntry.isDirectory && zipEntry.name.endsWith(".dex")
        }.map { entry ->
          DataInputStream(BufferedInputStream(zipFile.getInputStream(entry))).use { inputStream ->
            DexData(InputStreamDataSource(inputStream, entry.size.toInt())).also(DexData::load)
          }
        }.flatMap { dexData ->
          dexData.methodRefs.map { it.declClassName } + dexData.fieldRefs.map { it.declClassName }
        }.toList()
      }.distinct()
    }

    private sealed class IgnorableNode {
      abstract val name: String
      protected abstract val tagName: String

      fun generateIgnorableMarkup(): String =
        "<$tagName android:name=\"$name\" tools:node=\"remove\" />"

      data class Activity(override val name: String) : IgnorableNode() {
        override val tagName: String = "activity"
      }

      data class Service(override val name: String) : IgnorableNode() {
        override val tagName: String = "service"
      }

      data class Provider(override val name: String) : IgnorableNode() {
        override val tagName: String = "provider"
      }

      data class Receiver(override val name: String) : IgnorableNode() {
        override val tagName: String = "receiver"
      }
    }
  }
}
