package org.oppia.android.util.profile;

public class ProfileNameValidatorUtil {
  public boolean isNameValid(Character name) {
    return (
        Character.isAlphabetic(name) || isSymbolAllowed(name)
    );
  }

  public boolean isSymbolAllowed(Character name) {
    return (
        name.equals('.') || name.equals('-') || name.equals('\'')
    );
  }
}
