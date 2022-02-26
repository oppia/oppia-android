package org.oppia.android.util.profile;

/** Utility for [ProfileNameValidatorImpl] containing isAlphabetic method, only available in Java.*/
public class ProfileNameValidatorUtil {
  /**
   * Validates if the character in the name is an alphabet or an allowed symbol or not.
   *
   * @param name is the input.
   * @return true if the char is one of the allowed symbol or a letter character.
   * */
  public boolean isNameValid(Character name) {
    return (Character.isAlphabetic(name) || isSymbolAllowed(name));
  }

  private boolean isSymbolAllowed(Character name) {
    return (name.equals('.') || name.equals('-') || name.equals('\''));
  }
}
