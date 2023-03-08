## Instructions
When writing design docs at Oppia, please use this [design doc template](https://docs.google.com/document/d/1mnz8f708DZIa6BpUyRmbb0gCT6EKO3wIvWa_3rOEOYs/edit#). This will ensure that all the necessary information for the project is documented in a central location, and enable project reviews to be done effectively.

More specifically, here’s how to get started:

1. Make a copy of [this document](https://docs.google.com/document/d/1mnz8f708DZIa6BpUyRmbb0gCT6EKO3wIvWa_3rOEOYs/edit#).
2. Fill in the sections with details pertaining to your feature/project, following the instructions in the document template.
3. Proofread what you've written before asking reviewers to take a look.

## How to respond to doc reviews

When you're responding to a reviewer's comments in a doc, we recommend that you treat it like [responding to a code review](https://github.com/oppia/oppia-android/wiki/Guidance-on-submitting-a-PR#tips-for-getting-your-pr-merged-after-submission). 

More specifically:
- Feel free to accept (or reject!) suggestions. If you reject a suggestion, that's fine, but say why.
- In general, treat comments similarly to how you would treat comments in a standard code review. In other words:
  - Before asking for a follow-up review, make sure to reply to each comment (maybe with "Done") and update the doc as needed, but **don't resolve the comment thread**. Let the reviewer handle that, so that they can keep track of which comments still need to be addressed. Sometimes, more follow-up might be needed when the reviewer looks at the updated version of the doc.
  - If you have any questions about the reviewer's comments, feel free to ask them for clarification.

## Why we write design documents
Design documents provide team members with the opportunity to review the future implementation of a project before the code has been fully written. A good design document:

1. Outlines the technical goals of a feature or project that needs to be implemented
2. Describes the architectural changes in the codebase that the project will include
3. Describes how the project will be implemented (specific details of what you plan to build, but not the actual code)
4. Provides a break-down of tasks that will be completed
5. Provides time estimates for each task & how they will fit within defined milestones
6. Considers other aspects of the project, including privacy, security, accessibility, and others

## Tips when writing design documents

- Make sure that you understand the high-level goals of the project before going into increasingly lower-level details.
- Use dependency graphs, flow diagrams, and bullet lists when communicating the high-level architectural changes of the project.
- When considering additional options that go beyond the initial goals of the project, consider whether these are essential. If they aren't, add them to a "future work" section that could be worked on alongside or after the project (but not as part of the project itself). If it is essential, make sure you factor that into the implementation plan.
- When breaking down a project, determine all of the tasks that need to be completed for the project. A task is either performing a migration, adding/updating documentation, or creating a PR (each PR should correspond to a single task).
- When estimating how long a task will take for a project, assume it will take longer than you expect. Engineers often have a tendency of assuming implementation will go perfectly, but they sometimes don't and it's difficult to anticipate the things that could go wrong (e.g. bugs are found, a chosen library won't work, etc.). Suggest always multiplying your time estimates by 1.5x or 2x based on past experience (we call this a "fudge factor"). Consider also going back to old projects and comparing how much time you spent versus your estimates--this can help you figure out a good "fudge factor" to use when providing time estimates for future projects.
- If you're unsure how to approach the implementation, start by writing a basic hacked-together prototype to help solve specific questions of the implementation that you're unsure of. Look at other similar features for how they're laid out to compare. You should avoid implementing too much of the project in advance: the main purpose of a design document is to get feedback for a project before spending the large amount of time implementing it. For that reason, the document should take much less time than the project to create.
- If you're unsure about specific parts of the project: call these out as open questions so that other team members can weigh in and provide suggestions or resources that can help you resolve the open questions.
- If there's something you don't fully understand when writing any part of the design document, ask more questions. Sometimes we make mistakes in how we explain things, and that can lead to projects being taken in a direction we didn't anticipate. Other times, we see contributors make assumptions about one particular technical area (such as testing) and describe something other than what we expect. More questions can help bridge any missing knowledge, and can result in changes to our document templates or project goals.
- Use related artifacts when coming up with designs. We have lots of past design docs that can provide more detail on the types of things we value as a team, and how to describe those things. Things that follow established patterns are easier to understand since they minimize the amount of context needed to comprehend it. One way to evaluate how much a particular design document or proposal minimizes context is by considering how much easier/harder it is to understand when compared with other documents describing a project of similar complexity.

### Additional tips for large projects

The following tips correspond to projects that span 3+ months:
- Use milestones to organize tasks & specify expected completion times to communicate expectations with the team
- When creating milestones, consider the high-level "deliverables" of a project: what can you demonstrate to someone else after a set of tasks are completed that move Oppia toward the finished result of your project? 
- When estimating a milestone, first estimate how much time each task takes and then fit as many tasks as you can within a milestone. Don't change your time estimates based on the milestone (just because something is expected to get done within a certain timeframe doesn't mean it can). If the milestones don't provide enough time to finish the project, that may indicate that the project needs to be rescoped.
- If the project has multiple developers, ensure tasks and milestones have clear owners assigned to them.

