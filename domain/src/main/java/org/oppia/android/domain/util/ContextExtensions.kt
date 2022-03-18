package org.oppia.android.domain.util

import android.content.Context
import android.provider.Settings

/**
 * Returns the [Settings.Secure] string for the corresponding [name], or null if it's unavailable.
 *
 * This is an approved alternative to [Settings.Secure.getString] per the file content regex checks.
 */
fun Context.getSecureString(name: String): String? =
  Settings.Secure.getString(contentResolver, name)
