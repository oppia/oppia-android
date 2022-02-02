## Table of Contents

* [Communication channels](#communication-channels)
  * [Email](#email)
  * [Gitter](#gitter)
  * [Google Chat or Hangouts](#google-chat-or-hangouts)
  * [GitHub](#github)
  * [GitHub Discussions](#github-discussions)
* [How to Ask Good Questions](#how-to-ask-good-questions)
  * [Setup-related questions](#setup-related-questions)
    * [Before you ask a setup question](#before-you-ask-a-setup-question)
    * [How to ask a setup question](#how-to-ask-a-setup-question)
* [General questions](#general-questions)
  * [Before you ask a general question](#before-you-ask-a-general-question)
  * [How to ask a general question](#how-to-ask-a-general-question)
  * [Important points](#important-points)

Here we document Oppia's main communication channels and how to ask good questions.

## Communication channels

If you need help, there are a few communication channels you can use. Developers usually respond within 24 hours so long as you use a channel they actually check.

### Email

We have several mailing lists in the form of Google Groups that you can join:

* [oppia-announce](https://groups.google.com/forum/#!forum/oppia-announce) is for announcements of new releases or blog posts. It's not for asking questions though.
* [oppia-android-dev](https://groups.google.com/g/oppia-android-dev) is the main mailing list for communication between developers and for technical questions. You can post to it even if you're not a member of the group. This is where you can ask questions, solicit feedback, or make developer-specific announcements (e.g. a temporary GitHub outage).

You can also email your mentor with any questions. If you don't have a mentor, complete the steps on the [wiki page for contributing code to Oppia](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android) and you'll be assigned one.

### Gitter

We have a developer chat room [here](https://gitter.im/oppia/oppia-android). Feel free to drop in and say hi! Oppia's Welfare team monitors this chat to help out new contributors, so it's the best place to ask questions about Oppia or getting started. For questions about setting up Oppia or regarding the codebase or tests you can create a [GitHub Discussion](https://github.com/oppia/oppia-android/discussions) where Oppia's Welfare team and other developers will assist you in resolving the issue.

You can also directly message developers over Gitter. However, some developers don't monitor their Gitter messages regularly, so you might not get a response very quickly (or at all).

### Google Chat or Hangouts

Most Oppia developers and teams use Google Chat as their primary means of communication, and they usually respond quickly.  However, invites often get lost in spam folders, and some developers use a non-public email address for Google Chat. You can ask your mentor to put you in touch with a developer if you don't know their address or if they haven't acted on your invite.

### GitHub

If you have a question about a pull request or issue, you can also reach out to developers by at-mentioning them (e.g. `@developer-username`) in a comment and assigning them to the issue. Be sure you both at-mention and assign them! Some developers only look at their GitHub notifications (which at-mentions trigger), while others only look at what they're assigned to.

You can even mention whole teams of people! For example, if you find an issue that is destabilizing the project, you could notify all the core maintainers by including `@oppia/core-maintainers` in your issue. The teams are all listed [here](https://github.com/orgs/oppia/teams).

### GitHub Discussions
If you have questions regarding Oppia you can create a discussion here where Oppia's Welfare team and other developers will assist you in resolving the issue. You can create a discussion in any of the following categories based on the question:

* Developer announcements: All announcements that affect developers for the Oppia repository will be announced here.
* Setup Issues: Any issues that developers are facing with setting up or starting the server.
* Q&A: Any questions that developers have.
* Ideas: Any developer can place an idea here and it can be discussed here.
* Release: Discussion for bugs & PRs targeting the release.

You can refer to [this guide](https://docs.github.com/en/discussions/quickstart#creating-a-new-discussion) on how to create a new discussion.

## How to Ask Good Questions

At Oppia we don’t care how silly your question is! Just ensure your question is clear, and provide us with enough information to help us resolve it faster. We've divided the questions into 2 categories - Setup-related and General questions. You can start following the sections below to understand how you can ask each of them.

### Setup-related questions

#### Before you ask a setup question

1. You can setup/install oppia-android by visiting [this page](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#install-oppia-android).  Make sure you follow **all** the mentioned instructions from the beginning in a step-by-step manner.

2. Make sure each step succeeds (verify it with the expected behavior if mentioned in the wiki).

3. In case of any unexpected behavior/errors at any step, make sure you check out our wiki on [troubleshoot installation](https://github.com/oppia/oppia-android/wiki/Troubleshooting-Installation).

If you are still not able to fix your error, start following the section below to raise your question on [GitHub Discussions](https://github.com/oppia/oppia-android/discussions/categories/setup-issues).

#### How to ask a setup question

**Note**: If you are stuck at Step X, we will assume all previous steps through X-1 were successful for you. In case there were any previously failed steps, kindly mention those too with their error logs.

Please follow the template given below (mark x inside checkboxes to tick them) for creating a GitHub Discussions.

```md
**Checklist**
- [ ]  I have followed the [Before you ask a setup question](https://github.com/oppia/oppia-android/wiki/Get-help#before-you-ask-a-setup-question) section of the wiki.

**System Information**
- OS: (Be specific Ex: Ubuntu 20.04 or Ubuntu 20.04 VM on MacOS 11.2.1)

**Steps followed**

// If you encountered this error while following a wiki page, provide a link to the page and specify which step failed. Otherwise, list what steps caused the error. These should be detailed enough for someone else to follow them.

**Error log**

//paste error log here

or

paste a screenshot

**Approaches already used to resolve the issue**

(eg: Link to a Stack Overflow answer or any solution that you have tried)
- enter any additional description
```

## General questions

### Before you ask a general question

* We expect that you have already **set up Oppia on your machine**, and it is successfully running. (If not, kindly do that first!)
* Prepare a debugging doc following [this template](https://docs.google.com/document/d/1OBAio60bchrNCpIrPBY2ResjeR11ekcN0w5kNJ0DHw8).
* If there are **checks** on your PR, and you haven’t done any changes in that direction, kindly understand that sometimes they just fail due to flakiness. You should request for a re-run of those only when it’s preventing your PR from getting merged.

### How to ask a general question

Follow the template below for asking questions (fill in the values inside {{}} brackets and mark x inside checkboxes to tick them) to leave a comment on a pull request. Adapt the template as needed if you are using another channel.

```md
@{{PR reviewer or Mentor username}} PTAL!

**Checklist**
- [ ] I have filled the [CLA](https://goo.gl/forms/AttNH80OV0) and the [Oppia Contributor Survey](https://goo.gl/forms/otv30JV3Ihv0dT3C3)
- [ ] I have followed to install oppia-android mentioned [here](https://github.com/oppia/oppia-android/wiki/Contributing-to-Oppia-android#install-oppia-android)
- [ ]  I have worked through the [Before you ask a setup question](https://github.com/oppia/oppia/wiki/Get-help#before-you-ask-a-setup-question) section of the wiki.

**System Information**
- OS: (Be specific Ex: Ubuntu 20.04)
- [ ] Virtual machine

**About the issue**
- {{Describe the problem}} (eg. Failing robolectric tests: "The test has been
  failing repeatedly since {{x}} previous runs, and I haven’t done any
  changes related to it. Requesting a re-run.")

**Approaches already used to resolve the issue**

(eg: Link to a Stack Overflow answer or any solution that you have tried)
- #{{Link to the debugging doc}}
- enter any additional description
```

### Important points

* If you are unable to push changes due to some reason, you can create a [patch file](https://docs.gitlab.com/omnibus/development/creating-patches.html) and share it with the person you're asking for help.

* If you are facing issues in completing the assigned task, you can create a PR on your fork of the oppia-android repository, troubleshoot your problem on that pull request with help from your mentor, and then create a new PR on the original oppia-android repository.

* If you have not made a PR yet, because you are not sure:

  * what the issue is about, or
  * which files have to be modified, or
  * if your approach towards the solution is correct

  Then ask for help by commenting with your doubt/suggested approach on the issue page itself.  If you don’t get any response within **24 hours**, you can drop a message on the [gitter](https://gitter.im/oppia/oppia-chat#) chat too.

* If you want to have a discussion on your approach, but aren’t ready to make a PR yet, you can create a [public gist](https://gitter.im/oppia/oppia-android) and include the link to it in your question. It’s always better to see the code you are talking about!

* **Avoid asking for help from people via emails or direct messaging.** We encourage everyone to ask for help on a common channel so that whoever sees your query first can help you or guide you how to take your query forward. Note that directing questions to your mentor is fine.

  * Comment on the issue page or the PR if your question is very specific.

  * Use the [gitter chat](https://gitter.im/oppia/oppia-android) if your question is not issue-specific.

### Welfare Team
If you still feel blocked even after following the above steps or are unsure who to contact, you can reach out to the welfare team. You can also ping the welfare team by at-mentioning the welfare team: @oppia/android-welfare-team
