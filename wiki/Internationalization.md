We are grateful for the support of [Translatewiki](https://translatewiki.net/w/i.php?title=Special:Translate&group=oppia-android-app&filter=%21translated&action=translate) in contributing internationalized platform strings for the Oppia Android app! (Note that Translatewiki only provides support for translations of platform UI strings; content string translations are handled via the Contributor Dashboard on the Oppia.org website.)

## Table of Contents

- [Helping out with translations](#helping-out-with-translations)
- [Minimal set of topics](#minimal-set-of-topics)
- [Policy for enabling new languages](#policy-for-enabling-new-languages)
- [Procedure for enabling new languages](#procedure-for-enabling-new-languages)
  - [To enable a new language in Translatewiki](#to-enable-a-new-language-in-translatewiki)
  - [To enable a new language in the Android app](#to-enable-a-new-language-in-the-android-app)

## Helping out with translations

If you would like to help out with translations, you can do so by visiting the [Translatewiki dashboard](https://translatewiki.net/w/i.php?title=Special:Translate&group=oppia-android-app&filter=%21translated&action=translate) and picking a supported language from the dropdown menu on the right.

## Minimal set of topics

In addition to platform translations, we need the Oppia lessons themselves to be sufficiently translated before we can offer the app in a given language.

Currently, we define "sufficiently translated" to mean that the **minimal set** of topics are fully translated. This minimal set covers 6 topics: Place Values, Addition and Subtraction, Multiplication, Division, Fractions and Ratios.

## Policy for enabling new languages

Note that we only enable a select set of languages for Translatewiki, since releasing the lessons on the Android app is gated by the lessons themselves needing to be translated (see above). Therefore, in order not to waste translators' efforts, the policy for enabling languages for translation on Translatewiki is as follows:

  - The translations for all topics in the [minimal set](https://github.com/oppia/oppia-android/wiki/Internationalization#minimal-set-of-topics) on Oppia.org should be complete, or very close to complete.

And the policy for enabling a language in the Android app is as follows:

  - The translations for all topics in the [minimal set](https://github.com/oppia/oppia-android/wiki/Internationalization#minimal-set-of-topics) on Oppia.org should be complete.
  - All oppia-android-app translations for that language are completed in Translatewiki.

## Procedure for enabling new languages

### To enable a new language in Translatewiki

Please do the following steps:

  1. Verify that translations for all lessons in the [minimal set](https://github.com/oppia/oppia-android/wiki/Internationalization#minimal-set-of-topics) on Oppia.org are complete (or very close to complete) in that language, by going to the Contributor Dashboard and checking that there are no (or very few) translations left to do.
  2. Contact Ben/Sean and ask them to add the language to the oppia-android-app project. (They will do this by contacting translatewiki@translatewiki.net .)

### To enable a new language in the Android app

Please do the following steps:

  1. Verify that translations for all lessons in the [minimal set](https://github.com/oppia/oppia-android/wiki/Internationalization#minimal-set-of-topics) on Oppia.org are complete in that language, by going to the Contributor Dashboard and checking that there are no translations left to do.
  2. Verify that the translations on [Translatewiki](https://translatewiki.net/w/i.php?title=Special:Translate&group=oppia-android-app&filter=%21translated&action=translate) for the Oppia Android app are complete in that language.
  3. In consultation with Ben, make a PR adding support for that language ([here](https://github.com/oppia/oppia-android/pull/4307/files) is an example for Swahili).