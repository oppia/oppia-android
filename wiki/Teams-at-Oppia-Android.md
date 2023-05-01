The Oppia team is a distributed team of contributors from all over the world. To ensure that the project is as stable as possible, we have several infrastructure teams devoted to maintaining the health of various aspects of the development workflow. We also have an onboarding team that helps new contributors get started with the project and a welfare team responsible for assisting new contributors.

This wiki page explains the different teams in Oppia and their composition.

### CLaM Team

CLaM team is responsible for most of the UI based code (app-layer), which includes exploration player, question player, concept-card, subtopics, topics, etc. All these user-facing features must be RTL supportive, accessible by all and should work on all devices (mobile + tablet).

**Team contacts:** Ben Henning (@BenHenning) (lead), Adhiambo Peres (@adhiamboperes)

### Dev-workflow Team
The dev-workflow team ensures that the Oppia development process is smooth, stable and enjoyable, by ensuring that the following always hold:

1. There are no issues with the codebase setup (especially for new contributors).
2. Automated checks work as intended and are not unduly burdensome on both contributor’s machines and CI servers.
3. The technical documentation on the wiki is well-arranged, useful, and correct.
4. There are no security issues relating to npm dependencies.
5. The review process is speedy and streamlined.

Long-term projects include:

1. Working with the Onboarding team to identify areas where new contributors get stuck during the onboarding process and taking steps to fix those issues.
2. Streamlining the code review flow by: 
    * adding pre-submit checks for common errors 
    * enabling Oppiabot to automatically handle review/code-owner assignments 
    * speeding up the CI processes.

**Team contact:** Ben Henning (@BenHenning) (lead)

### Infrastructure Teams

##### Release Process Team
This team is responsible for ensuring that Oppia releases happen smoothly, correctly, and on time. Long-term projects include:
1. Streamlining the release process, and automating as many parts as possible, in order to reduce the chance of human error.
2. Adding automatic safeguards to ensure the correctness of releases.
3. Organizing the release coordinator rotation.

**Team Contact:** Ben Henning (@BenHenning)


### Onboarding/Welfare Team
This team is a group of Oppia developers who are committed to helping developers to be able to unblock themselves when they face any problems. They also aim to welcome new contributors and answer their questions.

**Team contact:** Mohit Gupta (@MohitGupta121)