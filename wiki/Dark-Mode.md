_Author credit: Thanks to **@ayush0402** for writing up the initial version of this guide._

## Table of Contents

- [Overview](#overview)
  - [Knowing the convention](#knowing-the-convention)
- [Working with the layouts](#working-with-the-layouts)
  - [How to achieve this goal?](#how-to-achieve-this-goal)
- [Running the app with dark mode](#running-the-app-with-dark-mode)

## Overview
This guide explains the newly adopted convention for using colors in oppia-android and adding support for dark mode 
to any particular layout while keeping the code organised and strictly following the decided convention.

The approach is to split color declarations in 4 different files instead of keeping the colors at one place, promoting separation of 
concerns and hence improving code maintainability and understandability.



#### Knowing the convention

The following files have been added for maintaining the colors : 
1. **`color_defs.xml`**<br>
	 This file should strictly contain actual color names (irrespective of their intended use) with their hex color code declaration.<br>
	 example:<br>
	 > Don't
	 ```xml
	 <color name="color_def_background_green">#90EE90</color>
	 <color name="color_def_secondary_green">#90EE90</color>
	 <color name="color_def_text_view_error_red">#FF0000</color>
	 ```
	 > Do
	 ```xml
	 <color name="color_def_lime_green">#90EE90</color>
	 <color name="color_def_blue">#0000FF</color>
	 ```
	 Declarations from this file should be only used in `color_palette.xml`.

2. **`color_palette.xml`**<br>
	There are two versions for this file (day/night variations). The purpose of this file is to split the colors for them to be later referenced by `component_colors.xml`. The color declarations in this should have a generic name and should not contain any name tied to the intended component like toolbar, edittext, textview, etec. to be used on. Also, colors should **not** contain "feature" based name (like *add_profile_background_color*).
	The declarations in this file should only reference `color_defs.xml`.
	>Don't:
	```xml
 	<color name="color_palette_add_profile_background_color">@color/light_black</color>
 	<color name="color_palette_text_input_layout_error_color">@color/oppia_pink</color>
	```
	>Do:
	```xml
 	<color name="color_palette_background_color">@color/light_black</color>
 	<color name="color_palette_error_color">@color/oppia_pink</color>
	```
	You can refer to both variations of these files to see how it separates the colors.
3. **`component_colors.xml`**<br>
	This file contains the highest level of color declarations. The declarations in this file should only reference `color_palette.xml`. It uses UI component specific names. Component colors should be shared very little outside of their respective views/fragments/activities. *All the layouts/views should only reference this file for colors.*<br>
	examples:<br>
	```xml
 	<color name="component_color_shared_text_input_edit_text_cursor_color">@color/primary_text_color</color>
  	<color name="component_color_shared_activity_toolbar_color">@color/toolbar_color</color>
  	<!-- styles.xml -->
  	<color name="component_color_shared_text_input_layout_text_color">@color/primary_text_color</color>
  	<color name="component_color_shared_input_interaction_edit_text_text_color">@color/primary_text_color</color>
  	<color name="component_color_shared_text_input_layout_background_color">@color/text_input_background_color</color>
  	<!-- Admin Auth Activity -->
  	<color name="component_color_admin_auth_secondary_text_color">@color/description_text_color</color>
  	<color name="component_color_admin_auth_layout_background_color">@color/background_color</color>
  	<!-- Add Profile Activity -->
  	<color name="component_color_add_profile_activity_label_text_color">@color/primary_text_color</color>
  	<color name="component_color_add_profile_activity_switch_text_color">@color/dark_text_color</color>
	```

_Note:_
- *All color names should strictly follow `snake_case` naming convention.*<br>
- *All colors in `component_colors.xml` and `color_palette.xml` should have `_color` as suffix and just the opposite for `color_defs.xml`*<br>
- *All color declaration must have their parent file name as prefix of their name, i.e. "`<file_name>_<color_name>`" (Look at the color name examples for better understanding.)*


<p align="center">
<img src="https://user-images.githubusercontent.com/76056229/153405110-a1c547b4-e8b8-4539-89dd-efe15dbb1b0d.png" width="700px" alt="visual representation of color hierarchy"></p>

## Working with the layouts

- Naming these colors can be bit tricky so it is suggested to take help from already exisitng colors in these files.
- The general idea is to make sure layouts reference colors only from `component_colors.xml`, which is then referencing a version of `color_palette.xml` based on the active theme, making sure all the color declarations are as per the conventions decided for them.

*Tip: Use layout Inspector to know more about targeted views.*

### Running the app with dark mode
We suggest running the app on an API 30 Google Pixel emulator using the system-wide dark mode option from settings.<br>

Some other skins of android might force their own version of dark mode to screens not having dark mode support yet.

## How is dark-mode implemented currently?
_Disclaimer: The current designs in the android code do not match the [mocks](https://xd.adobe.com/view/c05e9343-60f6-4c11-84ac-c756b75b940f-950d/grid/)  fully because the mocks were inconsistent across various screens and also there were some UI parts where we don’t have dark-mode mocks entirely._

These rules were followed while implementing dark-mode designs in android app:
- Avoid introducing new colors in the codebase as much as possible. For example, if there are two colors `#727272` and another `#737373` both of which have slight differences only, then use only one color, whichever is getting used more in general on similar screens.
- Keep the UI for similar screens consistent. For example: the colors of all screens where a pin is entered for authentication like `Add Profile`, `Pin Verification`, `Admin Auth`, etc should have similar colors in both dark mode and light mode.
- Similar views should be consistent. Best example for this is Toolbar/ActionBar. In current dark-mode designs the toolbar color is very inconsistent, so to remove any confusion we have kept the background color of all toolbars the same for dark mode (with some minor exceptions). Similarly, the divider color in `Help` screen and in `Options` screen should have similar color too. Same consistency rule applies for buttons and many other views too.
- New color rule: As mentioned earlier, we tried introducing new colors as much as possible and also used consistent colors across various screens but if we had to introduce a new color on our own we did this by using light mode designs as guidelines along with keeping the contrast ratio the same. For example: for this [App Version](https://xd.adobe.com/view/d405de00-a871-4f0f-73a0-f8acef30349b-a234/screen/80d1f4d6-be84-42db-ab23-9b5e1eb6e596/) blue background color we did not have dark-mode mocks and this color is not even getting used anywhere in the app. So for this we introduced a new dark mode color by following these steps:
- - The background color code is : `#A5D3EC` which is blue, so for dark mode we introduced `#1B5879` which is similar to blue but also makes sure that the color contrast is more than required so that it passes WCAG guidelines.

### Notes for Designers
- Have a look at [color_def](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/color_defs.xml), color_palette [light](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/color_palette.xml) & [dark](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values-night/color_palette.xml) and [component_colors](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/component_colors.xml) files.
- If you need to introduce any new color, check if you can find some similar color in the [color_def](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/color_defs.xml) file and possibly use that.
- Try to introduce similar colors for the same UI. For example: if you are using `Topic Name` somewhere then if possible try to use similar colors which have already been used for topic names in the android app.
- If you are looking for an already existing view and trying to find its light/dark color code, you will need to navigate to [component_colors](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/component_colors.xml) first and find the name for the view in component_colors.xml file. For example you can look at:
    - [<color name="component_color_add_profile_activity_info_icon_color">@color/color_palette_info_icon_color</color>](https://github.com/oppia/oppia-android/blob/12ded96886d29d46682e837c3232c4b6d3377800/app/src/main/res/values/component_colors.xml#L124C3-L124C114)
    - In this, we now we can find the related color_palette name, i.e., color_palette_info_icon_color. Now we can visit the color_palette file for light and dark mode, i.e.,for light mode [<color name="color_palette_info_icon_color">@color/color_def_chooser_grey</color>](https://github.com/oppia/oppia-android/blob/12ded96886d29d46682e837c3232c4b6d3377800/app/src/main/res/values/color_palette.xml#L65) and for dark-mode [<color name="color_palette_info_icon_color">@color/color_def_oppia_silver</color>](https://github.com/oppia/oppia-android/blob/12ded96886d29d46682e837c3232c4b6d3377800/app/src/main/res/values-night/color_palette.xml#L60)
    - In the end, we can see that the light color is color_def_chooser_grey and dark color is color_def_oppia_silver and we can find the exact color code for both in the [color_def](https://github.com/oppia/oppia-android/blob/develop/app/src/main/res/values/color_defs.xml) file.

### Notes for Reviewers
- Ensure that the naming of new colors across all files should follow the rules mentioned [above](https://github.com/oppia/oppia-android/wiki/Dark-Mode#knowing-the-convention).
- If any new color is getting introduced in the `color_defs.xml` file, then treat it as a red flag and keep a close eye on why it is getting introduced? Why can't any of the existing colors be used? Has this been approved by the design team?
- If any new color palette is getting introduced in `color_palette.xml` keep a close eye on that to make sure that there is no similar color palette already defined in `color_palette.xml` light/dark files. If there is a similar color palette, can you rename the existing color_palette such that it can be used on existing places as well as new places?
- Hardcoded-colors should only be mentioned in icons/thumbnails of `mipmap` and `drawable`folder and in the `color_defs.xml` file.
