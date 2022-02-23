The Oppia team is a distributed team of contributors from all over the world. To ensure that the project is as stable as possible, we have several infrastructure teams devoted to maintaining the health of various aspects of the development workflow. We also have an onboarding team that helps new contributors get started with the project and a welfare team responsible for assisting new contributors.

This wiki page explains the different teams in Oppia and their composition.

### CLaM Team

CLaM team is responsible for most of the UI based code (app-layer), which includes exploration player, question player, concept-card, subtopics, topics, etc. All these user-facing features must be RTL supportive, accessible by all and should work on all devices (mobile + tablet).

**Team contact:** Rajat Talesra (@rt4914) (lead), Ayush Kumar (@ayush0402), Yash Verma (@yash10019coder)

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

**Team contact:** Vinita Murthi (@vinitamurthi) (lead), Arjun Gupta (@ARJUPTA)

### Infrastructure Teams

##### Core Maintainers Team
The core maintainers team is accountable to the Oppia developer community with regards to preserving the stability of the entire Oppia codebase, by ensuring that the following things are true at all times for the "develop" branch on GitHub:
1. It is free of serious bugs that adversely impact the end-user experience.
2. It passes the CI checks.
3. It has no security issues.

Because this group understands the "big picture" of Oppia, other contributors are expected to take their advice seriously, especially when members of this group are acting in an incident-response capacity.

Membership of this group is decided by appointment, by a committee made up of the currently-serving core maintainers.

Contributors should notify the members of this team when they see major bugs or security vulnerabilities. The Core Maintainers group can be contacted via the @oppia/core-maintainers alias or at oppia-core-maintainers@googlegroups.com.


##### Release Process Team
This team is responsible for ensuring that Oppia releases happen smoothly, correctly, and on time. Long-term projects include:
1. Streamlining the release process, and automating as many parts as possible, in order to reduce the chance of human error.
2. Adding automatic safeguards to ensure the correctness of releases.
3. Organizing the release coordinator rotation.

**Team Contact:** Ben Henning (@BenHenning)

##### Quality Assurance Team
This team is responsible for ensuring that the Oppia codebase and releases are bug-free. Long-term projects include:
1. Deciding on a strategy for maintaining test coverage.
2. Improving test coverage to 100%.
3. Reducing the StackDriver error count to 0.
4. Organize the QA and bug fixing teams for each release.
5. Fix any known bugs in Oppia (especially user-facing ones).

**Team Contact:** Ben Henning (@BenHenning)

### Onboarding Team
The onboarding team aims to welcome new contributors and answer their questions.

**Team contact:** Rajat Talesra (@rt4914) (lead), Sarthak Aggarwal (@Sarthak2601)

### Welfare Team
The welfare team is a group of Oppia developers who are committed to helping developers to be able to unblock themselves when they face any problems. 

**Team contact:** Farees Hussain (@FareesHussain) (lead), Apoorv Srivastava (@MaskedCarrot)