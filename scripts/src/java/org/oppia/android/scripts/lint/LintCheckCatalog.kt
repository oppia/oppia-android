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
    "SourceLockedOrientationActivity",
    // Vector / drawable checks
    "VectorPath",
    "VectorRaster",
    "VectorDrawableCompat",
    "ConvertToWebp",
    "WebpUnsupported",
    "GradleOverrides",
    // Manifest / resource checks
    "MissingVersion",
    "RedundantLabel",
    "ManifestOrder",
    "ManifestTypo",
    "ManifestResource",
    "MultipleUsesSdk",
    "AaptCrash",
    "DuplicateActivity",
    "DuplicateUsesFeature",
    "MockLocation",
    "DeviceAdmin",
    "DataExtractionRules",
    "NetworkSecurityConfig",
    "MissingApplicationIcon",
    "MissingBackupPin",
    "GrantAllUris",
    "QueryAllPackagesPermission",
    "QueryPermissionsNeeded",
    "ProtectedPermissions",
    "SignatureOrSystemPermissions",
    "InvalidPermission",
    "UniquePermission",
    "ResAuto",
    "InstantApps",
    "NfcTechWhitespace",
    "WearStandaloneAppFlag",
    "WatchFaceEditor",
    "WatchFaceForAndroidX",
    "WearableActionDuplicate",
    "WearableBindListener",
    "WearableConfigurationAction",
    "LeanbackUsesWifi",
    "TileProviderPermissions",
    "PackagedPrivateKey",
    "InsecureBaseConfiguration",
    "PinSetExpiry",
    "AppIndexingService",
    "ProxyPassword",
    "ValidRestrictions",
    // Layout / XML checks
    "UseCompoundDrawables",
    "KeyboardInaccessibleWidget",
    "InconsistentLayout",
    "Overdraw",
    "InefficientWeight",
    "NestedWeights",
    "NestedScrolling",
    "DisableBaselineAlignment",
    "RelativeOverlap",
    "NegativeMargin",
    "PxUsage",
    "SmallSp",
    "SpUsage",
    "InOrMmUsage",
    "MergeRootFrame",
    "ObsoleteLayoutParam",
    "ScrollViewSize",
    "TooDeepLayout",
    "TooManyViews",
    "Suspicious0dp",
    "RequiredSize",
    "Orientation",
    "DuplicateDivider",
    "IncludeLayoutParam",
    "TextFields",
    "SelectableText",
    "LabelFor",
    "TextViewEdits",
    "WebViewLayout",
    "RemoteViewLayout",
    "UselessLeaf",
    "UselessParent",
    "StateListReachable",
    "ObjectAnimatorBinding",
    "PendingBindings",
    "ViewBindingType",
    "ViewConstructor",
    // Resource / string checks
    "TypographyDashes",
    "TypographyQuotes",
    "TypographyEllipsis",
    "TypographyFractions",
    "TypographyOther",
    "ExtraTranslation",
    "MissingTranslation",
    "MissingQuantity",
    "ImpliedQuantity",
    "UnusedQuantity",
    "Untranslatable",
    "PluralsCandidate",
    "StringEscaping",
    "SetTextI18n",
    "DuplicateStrings",
    "MissingDefaultResource",
    "PrivateResource",
    "ResourceCycle",
    "ResourceName",
    "IllegalResourceRef",
    "InvalidResourceFolder",
    "LocaleFolder",
    "UseAlpha2",
    "WrongFolder",
    "WrongRegion",
    "PropertyEscape",
    "EnforceUTF8",
    "MergeMarker",
    "NamespaceTypo",
    "RedundantNamespace",
    "UnusedNamespace",
    "Typos",
    "UnusedIds",
    "InvalidId",
    "MissingId",
    "InvalidImeActionId",
    "InvalidNavigation",
    "MenuTitle",
    "NotSibling",
    "NotConstructor",
    "MipmapIcons",
    "MissingPrefix",
    "WrongCase",
    "AnimatorKeep",
    "InternalInsetResource",
    "NonConstantResourceId",
    "InvalidVectorPath"
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
    "WeekBasedYear",
    // Checks that operate on individual source files
    "NotifyDataSetChanged",
    "CustomSplashScreen",
    "AcceptsUserCertificates",
    "AuthLeak",
    "BlockedPrivateApi",
    "BrokenIterator",
    "ConstantLocale",
    "CustomX509TrustManager",
    "DalvikOverride",
    "DefaultLocale",
    "DiscouragedPrivateApi",
    "DrawAllocation",
    "EnqueueWork",
    "ExportedContentProvider",
    "ExportedPreferenceActivity",
    "ExportedReceiver",
    "ExportedService",
    "FindViewByIdCast",
    "GetLocales",
    "HalfFloat",
    "IgnoreWithoutReason",
    "ImplicitSamInstance",
    "InflateParams",
    "InnerclassSeparator",
    "JavascriptInterface",
    "JobSchedulerService",
    "KotlinNullnessAnnotation",
    "KotlinPropertyAccess",
    "LambdaLast",
    "LaunchActivityFromNotification",
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
    "LogConditional",
    "LogTagMismatch",
    "LongLogTag",
    "MissingClass",
    "MissingInflatedId",
    "MissingPermission",
    "MotionLayoutInvalidSceneFileReference",
    "MotionLayoutMissingId",
    "MotionSceneFileValidationError",
    "MutatingSharedPrefs",
    "NoHardKeywords",
    "NotificationId0",
    "NotificationTrampoline",
    "OnClick",
    "OpenForTesting",
    "OverrideAbstract",
    "ParcelClassLoader",
    "ParcelCreator",
    "PrivateApi",
    "Proguard",
    "ProguardSplit",
    "Range",
    "RecyclerView",
    "RequiresFeature",
    "ResourceAsColor",
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
    "SdCardPath",
    "SecureRandom",
    "ServiceCast",
    "SetJavaScriptEnabled",
    "SetWorldReadable",
    "SetWorldWritable",
    "ShowToast",
    "Slices",
    "SoonBlockedPrivateApi",
    "StaticFieldLeak",
    "StringFormatCount",
    "StringFormatInvalid",
    "StringFormatTrivial",
    "SuspiciousImport",
    "SuspiciousIndentation",
    "SyntheticAccessor",
    "TranslucentOrientation",
    "TrustAllX509TrustManager",
    "TrulyRandom",
    "UastImplementation",
    "UnknownNullness",
    "UnlocalizedSms",
    "UnprotectedSMSBroadcastReceiver",
    "UnsafeDynamicallyLoadedCode",
    "UnsafeNativeCodeLocation",
    "UnsafeProtectedBroadcastReceiver",
    "UnspecifiedImmutableFlag",
    "UsableSpace",
    "UseSparseArrays",
    "UseValueOf",
    "UsesMinSdkAttributes",
    "UsingHttp",
    "ViewHolder",
    "VisibleForTests",
    "WakelockTimeout",
    "Wakelock",
    "WebViewApiAvailability",
    "WebViewClientOnReceivedSslError",
    "WifiManagerLeak",
    "WifiManagerPotentialLeak",
    "WorldReadableFiles",
    "WorldWriteableFiles",
    "WrongCall",
    "WrongConstant",
    "WrongManifestParent",
    "WrongThread",
    "WrongViewCast",
    "AnnotateVersionCheck",
    "InvalidPackage",
    "InvalidWakeLockTag",
    "InvalidWearFeatureAttribute",
    "IncompatibleMediaBrowserServiceCompatVersion",
    "IntentReset",
    "OldTargetApi"
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
    "UnusedAttribute",
    // Registered checks cross-file to see if a declared component is referenced in the
    // manifest. This requires the full manifest + source corpus.
    "Registered",
    // Instantiatable checks whether classes referenced in XML (activities, services, fragments)
    // are actually instantiatable — requires cross-referencing manifest + sources.
    "Instantiatable",
    // UnusedResources requires scanning ALL source files to determine if a resource is
    // referenced anywhere. Cannot run on a subset.
    "UnusedResources",
    // WrongThreadInterprocedural requires whole-program call graph analysis.
    "WrongThreadInterprocedural",
    // StringFormatMatches cross-references string resources with format call sites.
    "StringFormatMatches"
  )



  /** Union of all categorized checks. */
  val allKnownChecks: Set<String> by lazy {
    gradleChecksToIgnore +
      checksNotNeedingSources +
      checksForIncrementalSources +
      checksRequiringFullProject
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
    gradleChecksToIgnore + checksRequiringFullProject
}
