from PIL import Image, ImageChops
import os

rootPath = os.path.dirname(__file__)
img1 = Image.open(rootPath + "/golden_screenshots/org.oppia.app.onboarding.OnboardingActivity.png")
img2 = Image.open(rootPath + "/screenshots_for_test/org.oppia.app.onboarding.OnboardingActivity.png")

diff = ImageChops.difference(img1, img2)

if(diff.getbbox()):
    print("Test failed, there are regression")
else:
    print("Passed")