package org.oppia.android.scripts.lint

import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.LintClient

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

  /**
   * Checks discovered via [BuiltinIssueRegistry] that were not reported by `lint --list`.
   *
   * These are defaulted to requiring the full project description to ensure correctness
   * until they can be individually audited and moved to a more specific bucket.
   * TODO(#XXXX): Audit these checks and move them to the appropriate bucket above.
   */
  private val checksPendingCategorization = setOf(
    "AaptCrash",
    "AcceptsUserCertificates",
    "AnimatorKeep",
    "AnnotateVersionCheck",
    "AppIndexingService",
    "AuthLeak",
    "BlockedPrivateApi",
    "BrokenIterator",
    "ConstantLocale",
    "ConvertToWebp",
    "CustomSplashScreen",
    "CustomX509TrustManager",
    "DalvikOverride",
    "DataExtractionRules",
    "DefaultLocale",
    "DeviceAdmin",
    "DisableBaselineAlignment",
    "DiscouragedPrivateApi",
    "DrawAllocation",
    "DuplicateActivity",
    "DuplicateDivider",
    "DuplicateStrings",
    "DuplicateUsesFeature",
    "EnforceUTF8",
    "EnqueueWork",
    "ExportedContentProvider",
    "ExportedPreferenceActivity",
    "ExportedReceiver",
    "ExportedService",
    "ExtraTranslation",
    "FindViewByIdCast",
    "GetLocales",
    "GradleOverrides",
    "GrantAllUris",
    "HalfFloat",
    "IgnoreWithoutReason",
    "IllegalResourceRef",
    "ImplicitSamInstance",
    "ImpliedQuantity",
    "InOrMmUsage",
    "IncompatibleMediaBrowserServiceCompatVersion",
    "IncludeLayoutParam",
    "InconsistentLayout",
    "InefficientWeight",
    "InflateParams",
    "InnerclassSeparator",
    "InsecureBaseConfiguration",
    "InstantApps",
    "Instantiatable",
    "IntentReset",
    "InternalInsetResource",
    "InvalidId",
    "InvalidImeActionId",
    "InvalidNavigation",
    "InvalidPackage",
    "InvalidPermission",
    "InvalidResourceFolder",
    "InvalidVectorPath",
    "InvalidWakeLockTag",
    "InvalidWearFeatureAttribute",
    "JavascriptInterface",
    "JobSchedulerService",
    "KeyboardInaccessibleWidget",
    "KotlinNullnessAnnotation",
    "KotlinPropertyAccess",
    "LabelFor",
    "LambdaLast",
    "LaunchActivityFromNotification",
    "LeanbackUsesWifi",
    "LibraryCustomView",
    "LintDocExample",
    "LintImplBadUrl",
    "LintImplDollarEscapes",
    "LintImplIdFormat",
    "LintImplPsiEquals",
    "LintImplTextFormat",
    "LintImplTrimIndent",
    "LintImplUnexpectedDomain",
    "LintImplUseKotlin",
    "LintImplUseUast",
    "LocaleFolder",
    "LogConditional",
    "LogTagMismatch",
    "LongLogTag",
    "ManifestOrder",
    "ManifestResource",
    "ManifestTypo",
    "MenuTitle",
    "MergeMarker",
    "MergeRootFrame",
    "MipmapIcons",
    "MissingApplicationIcon",
    "MissingBackupPin",
    "MissingClass",
    "MissingDefaultResource",
    "MissingId",
    "MissingInflatedId",
    "MissingPermission",
    "MissingPrefix",
    "MissingQuantity",
    "MissingTranslation",
    "MissingVersion",
    "MockLocation",
    "MotionLayoutInvalidSceneFileReference",
    "MotionLayoutMissingId",
    "MotionSceneFileValidationError",
    "MultipleUsesSdk",
    "MutatingSharedPrefs",
    "NamespaceTypo",
    "NegativeMargin",
    "NestedScrolling",
    "NestedWeights",
    "NetworkSecurityConfig",
    "NfcTechWhitespace",
    "NoHardKeywords",
    "NonConstantResourceId",
    "NotConstructor",
    "NotSibling",
    "NotificationId0",
    "NotificationTrampoline",
    "NotifyDataSetChanged",
    "ObjectAnimatorBinding",
    "ObsoleteLayoutParam",
    "OldTargetApi",
    "OnClick",
    "OpenForTesting",
    "Orientation",
    "Overdraw",
    "OverrideAbstract",
    "PackagedPrivateKey",
    "ParcelClassLoader",
    "ParcelCreator",
    "PendingBindings",
    "PinSetExpiry",
    "PluralsCandidate",
    "PrivateApi",
    "PrivateResource",
    "Proguard",
    "ProguardSplit",
    "PropertyEscape",
    "ProtectedPermissions",
    "ProxyPassword",
    "PxUsage",
    "QueryAllPackagesPermission",
    "QueryPermissionsNeeded",
    "Range",
    "RecyclerView",
    "RedundantLabel",
    "RedundantNamespace",
    "Registered",
    "RelativeOverlap",
    "RemoteViewLayout",
    "RequiredSize",
    "RequiresFeature",
    "ResAuto",
    "ResourceAsColor",
    "ResourceCycle",
    "ResourceName",
    "ResourceType",
    "RestrictedApi",
    "ReturnThis",
    "RtlCompat",
    "RtlEnabled",
    "RtlHardcoded",
    "RtlSymmetry",
    "SSLCertificateSocketFactoryCreateSocket",
    "SSLCertificateSocketFactoryGetInsecure",
    "SQLiteString",
    "ScopedStorage",
    "ScrollViewSize",
    "SdCardPath",
    "SecureRandom",
    "SelectableText",
    "ServiceCast",
    "SetJavaScriptEnabled",
    "SetTextI18n",
    "SetWorldReadable",
    "SetWorldWritable",
    "ShowToast",
    "SignatureOrSystemPermissions",
    "Slices",
    "SmallSp",
    "SoonBlockedPrivateApi",
    "SpUsage",
    "StateListReachable",
    "StaticFieldLeak",
    "StringEscaping",
    "StringFormatCount",
    "StringFormatInvalid",
    "StringFormatMatches",
    "StringFormatTrivial",
    "Suspicious0dp",
    "SuspiciousImport",
    "SuspiciousIndentation",
    "SyntheticAccessor",
    "TextFields",
    "TextViewEdits",
    "TileProviderPermissions",
    "TooDeepLayout",
    "TooManyViews",
    "TranslucentOrientation",
    "TrustAllX509TrustManager",
    "TrulyRandom",
    "Typos",
    "TypographyDashes",
    "TypographyEllipsis",
    "TypographyFractions",
    "TypographyOther",
    "TypographyQuotes",
    "UastImplementation",
    "UniquePermission",
    "UnknownId",
    "UnknownIdInLayout",
    "UnknownNullness",
    "UnlocalizedSms",
    "UnprotectedSMSBroadcastReceiver",
    "UnsafeDynamicallyLoadedCode",
    "UnsafeNativeCodeLocation",
    "UnsafeProtectedBroadcastReceiver",
    "UnspecifiedImmutableFlag",
    "Untranslatable",
    "UnusedIds",
    "UnusedNamespace",
    "UnusedQuantity",
    "UnusedResources",
    "UsableSpace",
    "UseAlpha2",
    "UseCompoundDrawables",
    "UseSparseArrays",
    "UseValueOf",
    "UselessLeaf",
    "UselessParent",
    "UsesMinSdkAttributes",
    "UsingHttp",
    "ValidRestrictions",
    "VectorDrawableCompat",
    "VectorPath",
    "VectorRaster",
    "ViewBindingType",
    "ViewConstructor",
    "ViewHolder",
    "VisibleForTests",
    "WakelockTimeout",
    "Wakelock",
    "WatchFaceEditor",
    "WatchFaceForAndroidX",
    "WearStandaloneAppFlag",
    "WearableActionDuplicate",
    "WearableBindListener",
    "WearableConfigurationAction",
    "WebViewApiAvailability",
    "WebViewClientOnReceivedSslError",
    "WebViewLayout",
    "WebpUnsupported",
    "WifiManagerLeak",
    "WifiManagerPotentialLeak",
    "WorldReadableFiles",
    "WorldWriteableFiles",
    "WrongCall",
    "WrongCase",
    "WrongConstant",
    "WrongFolder",
    "WrongManifestParent",
    "WrongRegion",
    "WrongThread",
    "WrongThreadInterprocedural",
    "WrongViewCast"
  )

  /** Union of all categorized checks. */
  val allKnownChecks: Set<String> by lazy {
    gradleChecksToIgnore +
      checksNotNeedingSources +
      checksForIncrementalSources +
      checksRequiringFullProject +
      checksPendingCategorization
  }

  /**
   * The complete set of check IDs from the lint JAR's [BuiltinIssueRegistry].
   *
   * This is the authoritative source of truth for what checks are available at runtime.
   * Unlike `lint --list` (which only reports ~152 checks), the registry includes all
   * dynamically-loaded checks. The consistency mode validates that [allKnownChecks]
   * matches this set.
   */
  val registryChecks: Set<String> by lazy {
    // LintClient.clientName must be initialized before BuiltinIssueRegistry can be
    // instantiated, because some detectors (e.g. AssertDetector) check
    // LintClient.isStudio() during static class initialization.
    try {
      LintClient.clientName
    } catch (e: UninitializedPropertyAccessException) {
      LintClient.clientName = LintClient.CLIENT_CLI
    }
    BuiltinIssueRegistry().issues.map { it.id }.toSet()
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
    gradleChecksToIgnore + checksRequiringFullProject + checksPendingCategorization
}
