# Overview
Testing the app is an integral part of an app development process. By running tests against the app consistently, you can verify your app's correctness, functional behavior, and usability before you release it publicly.

In Oppia we are considering:
* Robolectric testing
* JUnit testing
* Integration/Screenshot tests using Espresso
* Performance tests
* LeakCanary
* Hermetic end-to-end testing using Espresso + some way to run the Oppia developer backend (or maybe better: drive using UIAutomator using a real production Oppia backend set up for testing purposes)

# Guidelines for testing
1. Test names should read like a sentence, and be consistent with other nearby test names to facilitate easily coming up with new tests. Test names should include three things:
Consider using a format similar to the following for naming test functions:

    `<testAction>_<withOneCondition>_<withSecondCondition>_<hasExpectedOutcome>`


where <testAction>, <withCondition> and <hasExpectedOutcome> are replaced with appropriate descriptions in snake_case. Put the outcome at the end of the name, so that you and others can easily compare consecutive tests of the same method that have slightly different conditions with divergent outcomes.
### For Example:
`testProfileChooserFragment_initializeProfiles_checkProfilesAreShown`
`testSplashActivity_initialOpen_routesToHomeActivity`

2. Use:

assertThat instead of assertEqual(), assertTrue() / assertFalse()

The first benefit is that assertThat is more readable than the other assert methods. For example take a look at the following assertEquals method:

     assertEquals(expected, actual);
 In the assertEquals method, you can easily get confused and interchange the  actual and  expected argument position.
 
      assertThat(actual, is(equalTo(expected)));
The first thing to notice is that it’s the other way around (actual first, expected second), which is the biggest hurdle to get over.  It also reads more like a sentence: “Assert that the actual value is equal to the expected value.”  As another, better example of readability, compare how to check for not equals, first the old way:

        assertFalse(expected.equals(actual));
Since there is no “assertNotEquals” (unless it’s custom coded) we have to use assertFalse and do an equals on the two variables. Here’s the much more readable new way with assertThat:

         assertThat(actual, is(not(equalTo(expected))));
What’s cool about the “not” method is that it can surround any other method, which makes it a negate for any matcher.  Also as seen above, the matcher methods can be chained to create any number of possible assertions.  Another cool thing is that there’s an equivalent short-hand version of the above equality methods which saves on typing:
Hence assertThat should be the preffered method over the other methods.
 
3. Guidelines for testing private methods/functions: 

Tests should only be written to verify the behaviour of public methods/functions. Private functions should not be used in behavioural tests. Here are some suggestions for what to do in specific cases (if this doesn't help for your particular case and you're not sure what to do, please talk to @BenHenning):
* If you want to test code execution a private method/function, test it through public interface, or move it to a utility (if it's general-purpose) where it becomes public. Avoid testing private APIs since that may lead to brittle test in unexpected situations (such as when the implementation of the API changes, but the behaviour remains the same).
*  If you’re trying to access hidden information, consider getting that information from one level below instead (e.g. datastore).

# Oppia project organization for tests

The following is the default directory structure for Oppia application and test code:
* `app/src/main/java`- for the source code of the Oppia application build
* `app/src/test/java `- for any unit test which can run on the JVM
* `app/src/androidTest/java` - for any test which should run on an Android device

If you follow this conversion, the Android build system runs your tests on the correct target (JVM or Android device).

## Roboelectric
With Robolectric you can write tests like this:

        @RunWith(AndroidJUnit4.class)
        public class MyActivityTest {

        @Test
         public void clickingButton_shouldChangeResultsViewText() throws Exception {
         Activity activity = Robolectric.setupActivity(MyActivity.class);

          Button button = (Button) activity.findViewById(R.id.press_me_button);
          TextView results = (TextView) activity.findViewById(R.id.results_text_view);

          button.performClick();
          assertThat(results.getText().toString(), equalTo("Testing Android Rocks!"));
        }
       }


## Espresso
Use Espresso to write concise, beautiful, and reliable Android UI tests.
Example JUnit4 test using Rules:
 
       @RunWith(AndroidJUnit4::class)
       class SplashActivityTest {

          @get:Rule
          var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
          SplashActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false)
        
         @Before
         fun setUp() {
           Intents.init()
          }
         @Test
         fun testSplashActivity_initialOpen_routesToHomeActivity() {
            activityTestRule.launchActivity(null)
            intended(hasComponent(ProfileActivity::class.java.name))
          }
         @After
         fun tearDown() {
           Intents.release()
          }
       }


How to use View Matchers, View Actions and View Assertions in Espresso?
Espresso has many ViewMatcher options which are very effective in uniquely locate UI element. You can also combine and create a combination of View Matchers to find element uniquely. 

The View Matcher is written like onView(ViewMatcher) which are commonly used. There are two types of actions that can be performed on View those are -

onView(ViewMatcher).perform(ViewAction)

onView(ViewMatcher).check(ViewAssertion)

      // frequently used matchers
      // using resource id
      onView(withId(R.id.my_view))
      // using visible text
      onView(withText("Done"))
      // using content description
      onView(withContentDescription("profile"));
      //using Hint Text
      onView(withHint("Sample_text"))
      //return TextView with links
      onView(hasLinks())

      //UI property matchers are mostly used in combination 
      // withId(R.id.my_view) is a ViewMatcher
      // click() is a ViewAction
      // matches(isDisplayed()) is a ViewAssertion
      onView(withId(R.id.my_view))
      .perform(click())
      .check(matches(isDisplayed()))

       onView(withId(R.id.profile_edit_name)).check(matches(withText("Sean")))
  
       onView(withId(R.id.test_number_input_interaction_view))
          .check(matches(isDisplayed())).check(matches(withText("9")))

       onView(withText(R.string.home_activity_back_dialog_exit)).perform(click())
       onView(withId(R.id.story_chapter_list)).perform(
          scrollToPosition<RecyclerView.ViewHolder>(1))
       onView(withId(R.id.story_chapter_list)).check(hasItemCount(4))


A test case can never be called complete without assertions and hence it is important to know View Assertions provided by Espresso to complete your test cases.

### Using `isCompletelyDisplayed` and `isDisplayed`

* **isCompletelyDisplayed** : Returns a matcher which only accepts a view whose height and width fit perfectly within the currently displayed region of this view. 
* There exist views (such as ScrollViews) whose height and width are larger then the physical device screen by design. Such views will _never_ be completely displayed.
*  **isDisplayed** : Returns a matcher that matches {@link View}s that are currently displayed on the screen to the user.
* Note: isDisplayed will select views that are partially displayed (eg: the full height/width of the view is greater then the height/width of the visible rectangle). If you wish to ensure the entire rectangle this view draws is displayed to the user use isCompletelyDisplayed

### Using `swipeLeft/Right` and using `scrollToPage`:
* Espresso release contains new left and right swiping actions: swipeLeft() and swipeRight(). They both are really useful when you'd like to swipe between activity fragments, tab layouts or any other UI elements.
* At times, GeneralSwipeAction can become unreliable because of its calculation varies on different screen size or density, so it may not be suitable for ViewPager. Instead, we can use to scroll with ViewPagerActions

      @Test
       fun testOnboardingFragment_checkSlide1Description_isCorrect() {
         launch(OnboardingActivity::class.java).use {
         onView(withId(R.id.onboarding_slide_view_pager)).perform(scrollToPage(1))
          onView(
             allOf(
                withId(R.id.slide_description_text_view),
                 isCompletelyDisplayed()
                )
              ).check(matches(withText(R.string.onboarding_slide_1_description)))
         }


## Testing RecyclerViews at Specific Positions

### RecyclerViewActions
The espresso-contrib library provides a RecyclerViewActions class that offers a way to click on a specific position in a RecyclerView (see instructions on configuring espress-contrib).

### RecyclerViewMatcher
Using the RecyclerViewMatcher under package ‘org.oppia.app.recyclerview’, you can perform actions on an item at a specific position in a RecyclerView, and also check that some content is contained within a descendant of a specific item.


     @Test
     fun testHomeActivity_recyclerViewIndex3_clickTopicSummary_opensTopicActivity() {
       launch(HomeActivity::class.java).use {
          onView(atPosition(R.id.home_recycler_view, 3)).perform(click())
          intended(hasComponent(TopicActivity::class.java.name))
          intended(hasExtra(TopicActivity.TOPIC_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, TEST_TOPIC_ID_0))
         }
      }
    
     @Test
     fun testHomeActivity_recyclerViewIndex1_promotedCard_storyNameIsCorrect() {
          launch(HomeActivity::class.java).use {
             onView(withId(R.id.home_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(1))
             onView(atPositionOnView(R.id.home_recycler_view, 1, R.id.story_name_text_view)).check(
             matches(
               withText(containsString("Matthew Goes to the Bakery"))
               )
             )
          }
     }


## Integrate Espresso Idling Resources in the app to build flexible UI tests

Espresso test might execute some checks while the app is doing some operations in the background threads, due to which the test may have no much content to interact with, therefore it throws an exception.

We can solve this by adding an artificial delay (solution illustrated in some of SO answers to this issue) either by adding code like SystemClock.sleep(period) or Thread.sleep(period) . Yet with this approach, we might end up having inflexible and slow tests

In order to solve this in a clean and effective manner, we have integrated IdlingResources in the application, CountingIdlingResource is one of the easiest to understand resource that comes bundled within the framework.