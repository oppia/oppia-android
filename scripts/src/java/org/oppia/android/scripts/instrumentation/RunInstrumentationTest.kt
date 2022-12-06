package org.oppia.android.scripts.instrumentation

import kotlin.system.exitProcess
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.FileUtils.openFile
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedFile
import org.oppia.android.scripts.common.FileUtils.toAbsoluteNormalizedPath
import org.oppia.android.scripts.emulator.AdbClient

fun main(args: Array<String>) {
  RunInstrumentationTest(CommandExecutorImpl.BuilderImpl.FactoryImpl()).run {
    parse(args)
    runInstrumentationTest()
  }
}

class RunInstrumentationTest(
  private val commandExecutorBuilderFactory: CommandExecutor.Builder.Factory
): ArgParser("//scripts:run_instrumentation_test") {
  private val adbBinaryPath by option(
    ArgType.String,
    fullName = "adb-binary-path",
    description = "Specifies the location of the ADB executable included as part of the" +
      " Android SDK."
  ).required()

  private val testApkPath by option(
    ArgType.String,
    fullName = "test-apk-path",
    description = "Specifies the location of the test APK to run."
  ).required()

  private val instrumentedBinaryApkPath by option(
    ArgType.String,
    fullName = "instrumented-binary-apk-path",
    description = "Specifies the location of the binary APK to instrument."
  ).required()

  private val testServicesApkPath by option(
    ArgType.String,
    fullName = "test-services-apk-path",
    description = "Specifies the location of the framework test services APK."
  ).required()

  private val testOrchestratorApkPath by option(
    ArgType.String,
    fullName = "test-orchestrator-apk-path",
    description = "Specifies the location of the framework test orchestrator APK."
  ).required()

  private val deviceSerial by option(
    ArgType.String,
    fullName = "device-serial",
    description = "Specifies the Android device serial which will host the tests being run. If" +
      " omitted, the orchestrator will auto-select the current device connected (must be one)."
  ).default(DEFAULT_DEVICE_SERIAL)

  private val avdName by option(
    ArgType.String,
    fullName = "avd-name",
    description = "Specifies the name of the AVD this test should use. This will be matched" +
      " against open emulators to find an open device. Only this or device-serial should be set."
  ).default(DEFAULT_DEVICE_SERIAL)

  private val testClass by option(
    ArgType.String,
    fullName = "test-class",
    description = "Specifies the fully qualified test class to run."
  ).required()

  private val adbBinaryFile by lazy {
    openFile(adbBinaryPath) {
      "Expected provided ADB executable to exist: ${it.toAbsoluteNormalizedPath()}."
    }.toAbsoluteNormalizedFile()
  }

  private val testApkFile by lazy {
    openFile(testApkPath) {
      "Expected provided test APK to exist: ${it.toAbsoluteNormalizedPath()}."
    }.toAbsoluteNormalizedFile()
  }

  private val instrumentedBinaryApkFile by lazy {
    openFile(instrumentedBinaryApkPath) {
      "Expected provided instrumented binary APK to exist: ${it.toAbsoluteNormalizedPath()}."
    }.toAbsoluteNormalizedFile()
  }

  private val testServicesApkFile by lazy {
    openFile(testServicesApkPath) {
      "Expected provided test services APK to exist: ${it.toAbsoluteNormalizedPath()}."
    }.toAbsoluteNormalizedFile()
  }

  private val testOrchestratorApkFile by lazy {
    openFile(testOrchestratorApkPath) {
      "Expected provided test orchestrator APK to exist: ${it.toAbsoluteNormalizedPath()}."
    }.toAbsoluteNormalizedFile()
  }

  private val adbClient by lazy {
    AdbClient(adbBinaryFile, commandExecutorBuilderFactory)
  }

  private val device by lazy { adbClient.retrieveTargetDevice(deviceSerial, avdName) }

  fun runInstrumentationTest() {
    val testServicesPackage = "androidx.test.services"
    val orchestratorPackage = "androidx.test.orchestrator"
    val runnerPackage = "org.oppia.android.test"
    val runningAppPackage = "org.oppia.android"
    val runnerClass = "org.oppia.android.testing.OppiaTestRunner"

    println(
      "Installing services, orchestrator, test, and instrumented binary APKs on device with" +
        " serial: ${device.serialName}..."
    )
    adbClient.forceInstallApk(device, appPackage = testServicesPackage, testServicesApkFile)
    adbClient.forceInstallApk(device, appPackage = orchestratorPackage, testOrchestratorApkFile)
    adbClient.forceInstallApk(device, appPackage = runnerPackage, testApkFile)
    adbClient.forceInstallApk(device, appPackage = runningAppPackage, instrumentedBinaryApkFile)

    // See https://github.com/bazelbuild/rules_python/issues/240#issuecomment-891406085 and
    // https://github.com/bazelbuild/bazel/blob/09c621/src/java_tools/junitrunner/java/com/google/testing/junit/runner/junit4/JUnit4Options.java#L75
    // for why this is the correct variable to use.
    val testFilter = System.getenv()[("TESTBRIDGE_TEST_ONLY")]
    if (testFilter != null) {
      println("Starting tests (with filter: '$testFilter')...")
    } else println("Starting tests...")

    // Use an AndroidX test orchestrator to run tests in an individual process with cleared app data
    // between runs.
    val pathOutput = adbClient.runPackageManagerCommand(device, "path", testServicesPackage)
    val classpath = pathOutput.singleOrNull()?.takeIf(String::isNotBlank)
      ?: error(
        "Failed to pull $testServicesPackage path: ${pathOutput.joinToString(separator = "\n")}"
      )
    // For available arguments, see:
    // https://cs.android.com/androidx/android-test/+/master:runner/android_test_orchestrator/java/androidx/test/orchestrator/OrchestratorConstants.java;drc=20d394a8c1f4d0dd901b460afe3880966b8ff05a;l=19
    // and
    // https://cs.android.com/androidx/android-test/+/master:runner/android_junit_runner/java/androidx/test/internal/runner/RunnerArgs.java;drc=c2dd4ddb83d69a363ade9e122c35da3d8d79fcfd;l=51.
    val testResults = adbClient.runInstrumentation(
      device,
      testOptions = mapOfNotNull(
        "clearPackageData" to "true",
        "class" to testClass,
        "targetInstrumentation" to "$runnerPackage/$runnerClass",
        testFilter?.let { "tests_regex" to it }
      ),
      testPackageName = orchestratorPackage,
      testRunnerQualifiedClassName = "androidx.test.orchestrator.AndroidTestOrchestrator",
      commandWrapper = AdbClient.ShellCommandWrapper.AppProcess(
        classpath, mainClass = "androidx.test.services.shellexecutor.ShellMain"
      )
    ).dropLastWhile { it.trim().isEmpty() }

    testResults.forEach(::println)

    // Exit with a non-zero error code if any tests didn't succeed, or if no tests were run (since
    // that's probably an error on the part of the test author or runner).
    val resultLine = testResults.last().trim()
    if (resultLine == "OK (0 tests)") {
      println(
        "Error: No tests ran (this is probably an issue with a test filter or the test framework)."
      )
      exitProcess(1)
    }
    exitProcess(if (resultLine.startsWith("OK")) 0 else 1)
  }

  private companion object {
    private const val DEFAULT_DEVICE_SERIAL = "<default device serial>"
    private const val AVD_NAME_PROPERTY_KEY = "debug.avd_name"

    private fun AdbClient.retrieveTargetDevice(
      deviceSerial: String, avdName: String
    ): AdbClient.AndroidDevice {
      return when {
        avdName.isNotBlank() && deviceSerial == DEFAULT_DEVICE_SERIAL ->
          retrieveDeviceMatchingAvd(avdName)
        avdName.isBlank() && deviceSerial != DEFAULT_DEVICE_SERIAL ->
          retrieveDeviceWithSerial(deviceSerial)
        avdName.isBlank() && deviceSerial == DEFAULT_DEVICE_SERIAL ->
          retrieveSingleCurrentDevice()
        else -> error("device-serial and avd-name cannot be both simultaneously provided.")
      }
    }

    private fun AdbClient.retrieveSingleCurrentDevice(): AdbClient.AndroidDevice {
      return checkNotNull(listDevices().singleOrNull()) {
        "Expected exactly one device to be online since no serial was specified."
      }
    }

    private fun AdbClient.retrieveDeviceWithSerial(deviceSerial: String): AdbClient.AndroidDevice {
      return checkNotNull(findDeviceWithSerial(deviceSerial)) {
        "Expected to find online device with provided serial: $deviceSerial."
      }
    }

    private fun AdbClient.retrieveDeviceMatchingAvd(avdName: String): AdbClient.AndroidDevice {
      val matchingDevice =
        listDevices().singleOrNull { getProperty(it, AVD_NAME_PROPERTY_KEY) == avdName }
      return checkNotNull(matchingDevice) {
        "Expected exactly one device to be online with AVD: $avdName."
      }
    }

    private fun <K, V> mapOfNotNull(vararg elements: Pair<K, V>?) = listOfNotNull(*elements).toMap()
  }
}
