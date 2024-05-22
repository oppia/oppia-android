## Table of Contents

- [Overview of entities](#overview-of-entities)
- [Key terms](#key-terms)
- [How to visit?](#how-to-visit)
  - [Concept Card](#concept-card)
  - [Hints & Solution](#hints--solution)
  - [Completed Stories](#completed-stories)
  - [Ongoing Topics](#ongoing-topics)  

## Overview of entities

This diagram outlines the various entities in Oppia and how they relate
to each other:

```mermaid
flowchart TD
T("Topic") --> S1("Story-1")
T --> S2("Story-2")
S1 --> C1("Chapter-1/Exploration")
C1 --> c1("Card-1")
C1 --> c2("card-2")
S1 --> C2("chapter-2")
C2 --> c3("Card-3")
c3 --> co("Content")
c3 ----> in("Interaction")
in --> mc("Multiple-choice")
in --> is("Image Selection")
in --> fi("Fraction-input")
in --> oqt("other question types")
S2 --> C3("Chapter-3")
S2 --> C4("Chapter-4")
C3 --> c4("Card-4")
C4 --> c5("Card-5")
C4 --> c6("Card-6")
```

## Key terms

1. **Topic**: A topic is a broad term that refers to the subject content being taught (e.g. Addition/Subtraction). A list of available topics appears on the Home screen of app.
2. **Story**: Stories are situations/scenarios that are meant to help users understand the topic. For example, if the topic is addition, then one of the stories could be about a kid going to a shop and to buy 3 pens and 4 pencils. A list of stories is shown in the "Lessons" tab when you open a topic from the home screen.
3. **Promoted Story**: Promoted Story is mainly the recent Story/Chapter you played. It is shown on the "home screen" with heading text "Stories For You".
4. **Skill**: This is a concrete learning outcome that describes something that a learner should be able to do. It is usually stated in the form “Given X, compute/calculate/draw/etc. Y.” For example: “Given a fraction, identify its numerator.”
5. **Exploration/Chapter**: This is a structured learning experience that is part of a story, and provides the learner with an active way to learn new concepts, as well as targeted feedback. It is the core unit of learning in Oppia. The flow/screen that appears when any story is started is known as the Exploration/Chapter.
6. **Concept Card**: This is a non-story-based explanation of how to perform a particular skill. It serves as a reference/reminder for students who may have encountered the skill before but forgotten how to carry it out. These can be accessed from  the "Revision" tab or are linked within the chapter you are playing.
7. **Question/QuestionPlayer**: This is a standalone question that may be used by students as part of a practice session.

## How to visit?

### Concept Card

`Home` --> `Choose Topic` --> `Revision Tab` --> `Select revision card` --> `Goto hyperlink present in description text`

<img width="350" height="700" alt="Visit Concept Card" src="https://github.com/oppia/oppia-android/assets/76530270/d71c5fc2-92eb-4087-9660-9f463bb282a2">

### Hints & Solution

`Home` --> `Choose Topic` --> `Start any lesson` --> `Wait for sometime to blue Hints bar popup`

After all hints are opened, Solution will show up.

<img width="350" height="700" alt="Hints and Solution" src="https://github.com/oppia/oppia-android/assets/76530270/3b2cc6ea-335f-439a-80de-05b997dc9c99">

### Completed Stories

<img width="350" height="700" alt="Hints and Solution" src="https://github.com/oppia/oppia-android/assets/76530270/bf4b57b0-f3b3-4177-8ab8-7de69f0f4204">

### Ongoing Topics

<img width="350" height="700" alt="Hints and Solution" src="https://github.com/oppia/oppia-android/assets/76530270/e63f8676-7b81-4237-87e7-06a2ff7d7fcf">