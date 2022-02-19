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
	 <color name="background_green">#90EE90</color>
	 <color name="secondary_green">#90EE90</color>
	 <color name="text_view_error_red">#FF0000</color>
	 ```
	 > Do
	 ```xml
	 <color name="lime_green">#90EE90</color>
	 <color name="blue">#0000FF</color>
	 ```
	 Declarations from this file should be only used in `color_palette.xml`.

2. **`color_palette.xml`**<br>
	There are two versions for this file (day/night variations). The purpose of this file is to split the colors for them to be later referenced by `component_colors.xml`. The color declarations in this should have a generic name and should not contain any name tied to the intended component like toolbar, edittext, textview, etec. to be used on. Also, colors should **not** contain "feature" based name (like *add_profile_background_color*).
	The declarations in this file should only reference `color_defs.xml`.
	>Don't:
	```xml
 	<color name="add_profile_background_color">@color/light_black</color>
 	<color name="text_input_layout_error_color">@color/oppia_pink</color>
	```
	>Do:
	```xml
 	<color name="background_color">@color/light_black</color>
 	<color name="error_color">@color/oppia_pink</color>
	```
	You can refer to both variations of these files to see how it separates the colors.
3. **`component_colors.xml`**<br>
	This file contains the highest level of color declarations. The declarations in this file should only reference `color_palette.xml`. It uses UI component specific names. Component colors should be shared very little outside of their respective views/fragments/activities. *All the layouts/views should only reference this file for colors.*<br>
	examples:<br>
	```xml
 	<color name="shared_text_input_edit_text_cursor_color">@color/primary_text_color</color>
  	<color name="shared_activity_toolbar_color">@color/toolbar_color</color>
  	<!-- styles.xml -->
  	<color name="shared_text_input_layout_text_color">@color/primary_text_color</color>
  	<color name="shared_input_interaction_edit_text_text_color">@color/primary_text_color</color>
  	<color name="shared_text_input_layout_background_color">@color/text_input_background_color</color>
  	<!-- Admin Auth Activity -->
  	<color name="admin_auth_secondary_text_color">@color/description_text_color</color>
  	<color name="admin_auth_layout_background_color">@color/background_color</color>
  	<!-- Add Profile Activity -->
  	<color name="add_profile_activity_label_text_color">@color/primary_text_color</color>
  	<color name="add_profile_activity_switch_text_color">@color/dark_text_color</color>
	```
4. **`colors_migrating.xml`**<br>
	This file contains color declarations which are supposed to be in color_defs.xml but has not been renamed yet to have actual color name instead of names linked to their use and components. This is a temporary measure to make sure other 4 color files follows the convention decided for them.
	This file should be deleted after all colors have been shifted to `color_defs.xml`.<br>

*Note: All color names should strictly follow `snake_case` naming convention.*<br>
*All colors in `component_colors.xml` and `color_palette.xml` should have `_color` as suffix and just the opposite for `color_defs.xml`*


<p align="center">
<img src="https://user-images.githubusercontent.com/76056229/153405110-a1c547b4-e8b8-4539-89dd-efe15dbb1b0d.png" width="700px" alt="visual representation of color hierarchy"></p>

## Working with the layouts
Currently most of the layouts are directly referencing colors from `color_defs.xml`, they don't have separate colors for day and night mode. Our goal here is to make sure that views and layouts are using specified colors for day and night wherever applicable.

You can refer to the design mocks for expected final result : [Dark Mode Mocks](https://xd.adobe.com/view/c05e9343-60f6-4c11-84ac-c756b75b940f-950d/grid/)

#### How to acheive this goal?
Here is how I would go around working with any particular layout...<br>

- Replace all the generic colors in the layout with something more specific to the component by defining it in the `component_colors.xml`, generally it should be named in the format *`<activity_name>_<component_name>_color`*. 

- Go through the mock for the concerned activity and note down which component of the app needs separate colors for day and night modes. The mock has provided hex color codes for all the elements in the UI, if any of the colors is not already present in the `color_defs.xml` then add it to the file with the actual color name.

- Now, the newly defined colors in `component_colors.xml` should reference to something in `color_palette.xml`, define new colors in `color_palette.xml` based on general use case if not already defined. You will need to define same colors twice, in `values\color_palette.xml` as well as `values-night\color_palette.xml`. Both these declarations can be same as well, if there is no difference in the mocks for day and night mode. Make sure `color_palette.xml` is using colors from `color_defs.xml` only.

Naming these colors can be bit tricky so it is suggested to take help from already exisitng colors in these files.

In short, the general idea is to make sure layouts reference colors only from `component_colors.xml`, which is then referencing a version of `color_palette.xml` based on the active theme, making sure all the color declarations are as per the conventions decided for them.

*Tip: Use layout Inspector to know more about targeted views.*

### Running the app with dark mode
We suggest running the app on an API 30 Google Pixel emulator using the system-wide dark mode option from settings.<br>

Some other skins of android might force their own version of dark mode to screens not having dark mode support yet.
