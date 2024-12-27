Whenever you are debugging a problem, you may find it useful to keep a record of your debugging process. We often do this already in issues. Issues usually begin with a detailed description of the problem, which is followed by discussion, reports of attempted debugging steps, and what root cause was identified. However, issues' linear comment structure makes them more amenable to communication among team members than organizing your thoughts. Debugging docs, on the other hand, serve primarily to organize your thoughts.

**Note:** In general, we strongly recommend writing and sharing a debugging doc if you can't figure out a solution to a problem within the **first 2 hours** of working on it.

## Table of Contents

- [Benefits](#benefits)
- [How to Write a Debugging Doc](#how-to-write-a-debugging-doc)
  - [1. Begin with Describing What Goes Wrong](#1-begin-with-describing-what-goes-wrong)
  - [2. Describe Your Investigations](#2-describe-your-investigations)
  - [3. Document Your Guesses and Testing](#3-document-your-guesses-and-testing)
  - [4. Continue/Share on GitHub Discussions](#4-continueshare-on-github-discussions)
  - [5. Document Your Solution](#5-document-your-solution)
- [Get Started](#get-started)

## Benefits

Primarily, debugging docs help keep your thoughts organized. When you have written down the steps you've already tried and the results of your investigations, you don't have to worry about forgetting your earlier work. Further, when you document your work, you force yourself to reflect on what you've already tried. Debugging docs also make it easy for you to bring someone else up to speed on your bug.

Finally, these documents can serve as records of debugging strategies and bug causes that we can reference later on. For example, we might search these debugging docs for an error message we are encountering to see if it has been fixed before.

## How to Write a Debugging Doc

### 1. Begin with Describing What Goes Wrong

Your description should include:

- Context: What branch or PR is the bug happening on? If this is happening locally, what system are you running, and what code changes have you made?

- How the Bug Manifests: This might include error logs that you pasted into the document, screenshots, or links to a test run on a PR. **If you provide a link, copy the relevant part of the page you are linking to.** This keeps the document self-contained so we can search them. It also makes the doc easier for people to read.

### 2. Describe Your Investigations

What did you try, and what happened when you tried it? You want to include enough detail so that someone could reproduce your investigation to see if they get the same results.

### 3. Document Your Guesses and Testing

After some investigation, you might have some ideas for what might be going wrong. Document your guesses and describe how you go about testing them. Report the results of that testing and describe whether you think your guess was right. What's your reasoning?

### 4. Continue/Share on GitHub Discussions

Keep going! Continue documenting your investigations, guesses, and tests of those guesses. You can share your debugging doc on GitHub Discussions to review and help you in finding the root cause of the issue or the solution.

### 5. Document Your Solution

Once you figure out what the problem was, write that down too! Include an analysis of how the root cause you identify caused the errors you described at the top. Often, this will take the form of one of your suspected solutions.

## Get Started

Ready to get started with your own debugging doc? You can make a copy of [this template](https://docs.google.com/document/d/1OBAio60bchrNCpIrPBY2ResjeR11ekcN0w5kNJ0DHw8) to get started.
