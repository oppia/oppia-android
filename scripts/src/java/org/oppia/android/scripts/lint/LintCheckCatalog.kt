package org.oppia.android.scripts.lint

/**
 * Catalog of all lint checks known to the version of Android Lint used by this project.
 *
 * Every check is classified into exactly one of four buckets:
 * 1. Gradle-specific checks irrelevant in a Bazel project.
 * 2. XML, resource, icon, and manifest checks that operate without any Java/Kotlin
 *    source files.
 * 3. Source-file-scoped checks that can run on only changed files (incremental).
 * 4. Cross-file or classpath-dependent checks that need the full project description.
 *
 * The union of all four sets equals [allKnownChecks]. A consistency mode
 * (--mode=check-script-consistency) compares this catalog against the linter's actual
 * check list to catch additions or removals after lint version upgrades.
 */
object LintCheckCatalog {

  /** Gradle-specific checks that are irrelevant for Bazel-based projects. */
  private val gradleChecksToIgnore = setOf(
    "AndroidGradlePluginVersion",
    "AnnotationProcessorOnCompilePath",
    "DataBindingWithoutKapt",
    "ExpiredTargetSdkVersion",
    "ExpiringTargetSdkVersion",
    "GradleCompatible",
    "GradleDependency",
    "GradleDeprecated",
    "GradleDeprecatedConfiguration",
    "GradleDynamicVersion",
    "GradleGetter",
    "GradleIdeError",
    "GradlePath",
    "GradlePluginVersion",
    "JavaPluginLanguageLevel",
    "JcenterRepositoryObsolete",
    "LifecycleAnnotationProcessorWithJava8",
    "MinSdkTooLow",
    "NewerVersionAvailable",
    "OutdatedLibrary",
    "RiskyLibrary"
  )

  /**
   * Checks that do NOT need Java/Kotlin source files.
   *
   * These operate on XML layouts, resources, drawables, icons, manifests, or other
   * non-source artifacts. They can run with an empty source file list and still produce
   * valid findings.
   */
  private val checksNotNeedingSources = setOf(
    // XML / Layout / Resource checks
    "AdapterViewChildren",
    "AllCaps",
    "AlwaysShowAction",
    "AppBundleLocaleChanges",
    "Autofill",
    "BackButton",
    "BottomAppBar",
    "ButtonCase",
    "ButtonOrder",
    "ButtonStyle",
    "ByteOrderMark",
    "ContentDescription",
    "CustomViewStyleable",
    "Deprecated",
    "DuplicateDefinition",
    "DuplicateIds",
    "EllipsizeMaxLines",
    "ExtraText",
    "GridLayout",
    "HardcodedText",
    "InconsistentArrays",
    "MangledCRLF",
    "MissingConstraints",
    "NotInterpolated",
    "ReferenceType",
    "ScrollViewCount",
    "StringShouldBeInt",
    "XmlEscapeNeeded",
    // Icon / Drawable checks
    "GifUsage",
    "IconColors",
    "IconDensities",
    "IconDipSize",
    "IconDuplicates",
    "IconDuplicatesConfig",
    "IconExpectedSize",
    "IconExtension",
    "IconLauncherShape",
    "IconLocation",
    "IconMissingDensityFolder",
    "IconMixedNinePatch",
    "IconNoDpi",
    "IconXmlAndPng",
    // Manifest checks
    "AppLinksAutoVerify",
    "AppLinkUrlError",
    "DevModeObsolete",
    "DuplicatePlatformClasses",
    "FullBackupContent",
    "HardcodedDebugMode",
    "HighAppVersionCode",
    "ImpliedTouchscreenHardware",
    "IntentFilterExportedReceiver",
    "IntentFilterUniqueDataAttributes",
    "MissingLeanbackLauncher",
    "MissingLeanbackSupport",
    "MissingTvBanner",
    "NotificationIconCompatibility",
    "PermissionImpliesUnsupportedChromeOsHardware",
    "PermissionImpliesUnsupportedHardware",
    "TestAppLink",
    "UnsupportedChromeOsCameraSystemFeature",
    "UnsupportedChromeOsHardware",
    "UnsupportedTvHardware",
    "ValidActionsXml",
    // Font / analytics / other non-source checks
    "FontValidation",
    "InvalidAnalyticsName",
    "InvalidUsesTagAttribute",
    "EasterEgg",
    "LocalSuppress",
    "LockedOrientationActivity",
    "NonResizeableActivity",
    "SourceLockedOrientationActivity"
  )

  /**
   * Source-file-scoped checks that can run incrementally on only changed files.
   *
   * These analyze individual `.kt`/`.java` files and do not require cross-file
   * resolution. They are safe to run with a filtered (incremental) source file list.
   */
  private val checksForIncrementalSources = setOf(
    "AccidentalOctal",
    "AddJavascriptInterface",
    "AllowAllHostnameVerifier",
    "AppCompatCustomView",
    "AppCompatMethod",
    "AppCompatResource",
    "ApplySharedPref",
    "AssertionSideEffect",
    "BadHostnameVerifier",
    "BatteryLife",
    "BidiSpoofing",
    "CanvasSize",
    "CheckResult",
    "ClickableViewAccessibility",
    "CoarseFineLocation",
    "CommitPrefEdits",
    "CommitTransaction",
    "DefaultEncoding",
    "DeletedProvider",
    "DeprecatedProvider",
    "DeprecatedSinceApi",
    "DiffUtilEquals",
    "DiscouragedApi",
    "EmptySuperCall",
    "ExifInterface",
    "ExpensiveAssertion",
    "FileEndsWithExt",
    "GetContentDescriptionOverride",
    "GetInstance",
    "HandlerLeak",
    "HardwareIds",
    "HighSamplingRate",
    "InlinedApi",
    "KtxExtensionAvailable",
    "MissingFirebaseInstanceTokenRefresh",
    "MissingIntentFilterForMediaSearch",
    "MissingMediaBrowserServiceIntentFilter",
    "MissingOnPlayFromSearch",
    "MissingSuperCall",
    "NewApi",
    "ObsoleteSdkInt",
    "Override",
    "PackageManagerGetSignatures",
    "Recycle",
    "ShiftFlags",
    "ShortAlarm",
    "SimpleDateFormat",
    "StopShip",
    "SupportAnnotationUsage",
    "UniqueConstants",
    "UseCheckPermission",
    "UseOfBundledGooglePlayServices",
    "UsingC2DM",
    "ValidFragment",
    "VulnerableCordovaVersion",
    "WeekBasedYear"
  )

  /**
   * Cross-file or classpath-dependent checks that need the full project description.
   *
   * These require cross-file analysis, classpath resolution, manifest-to-source
   * cross-referencing, or must inspect the full source corpus to avoid false positives.
   * They cannot run incrementally.
   */
  private val checksRequiringFullProject = setOf(
    "CutPasteId",
    "DuplicateIncludedIds",
    "SwitchIntDef",
    // UnusedAttribute checks whether a custom XML attribute is referenced anywhere in the
    // codebase. Running it on a subset of changed files produces false positives because
    // usages in unchanged files are invisible to the checker.
    "UnusedAttribute"
  )

  /** Union of all categorized checks. */
  val allKnownChecks: Set<String> by lazy {
    gradleChecksToIgnore +
      checksNotNeedingSources +
      checksForIncrementalSources +
      checksRequiringFullProject
  }

  /**
   * Returns the set of check IDs to disable when running a full analysis.
   *
   * In full mode, only Gradle-specific checks are disabled since they are irrelevant
   * for Bazel-based builds.
   */
  fun computeChecksToDisableInFullRun(): Set<String> = gradleChecksToIgnore

  /**
   * Returns the set of check IDs to disable when running an incremental analysis.
   *
   * In incremental mode, Gradle-specific checks and project-scoped checks are disabled
   * since project-scoped checks require full cross-file context that isn't available
   * when analyzing only changed files.
   */
  fun computeChecksToDisableInIncrementalRun(): Set<String> =
    gradleChecksToIgnore + checksRequiringFullProject
}
