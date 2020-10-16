## Script

### Functional programming in the world of mobile

The Functional Programming paradigm is broadly adopted for server side development and services, but might not be so 
known for mobile developers. By history, mobile has been all object oriented programing, mostly driven by the native 
platforms where apps are expected to run.

Over time, we learned how to perform proper concern separation so we could keep our UI as passive and agnostic as 
possible from our business logic. That not only leveraged testability, but opened the door for decoupled architectures 
where we became free to use any other paradigms than the Object Oriented one, like Reactive Programming for example.

Reactive was a good start and let us dive into the world of Streams of events that our UI was able to react to. We 
started thinking on apps as a merged stream of events coming from both the view and the business layer.
It was also a natural step towards a more declarative approach where cold streams where pretty relevant for 
compositionality.

Today, we can move one more step forward and bring Functional Programming into the mix, driven by the concept of 
Functional Streams.

This is how we will deliver our first Functional Android app today. Let's get to work!

### Potential resources / ideas for the video

* [The Rx Marble diagrams](https://rxmarbles.com/), which are widely used to understand how streams of events work. They are very representative of what we are talking about when we say "Streams of events" here. Any images showing marble diagrams will be easily identified by a viewer as reactive programming. [This one is a good example](https://rxmarbles.com/#merge).
* [Clean architecture diagram](https://www.google.com/search?q=clean+architecture&tbm=isch&ved=2ahUKEwjp9-bhpYjqAhXBw4UKHedIAqcQ2-cCegQIABAA&oq=clean+architecture&gs_lcp=CgNpbWcQAzICCAAyAggAMgIIADICCAAyAggAMgIIADICCAAyAggAMgIIADICCAA6BAgAEEM6BQgAELEDUI0SWIodYMYeaABwAHgAgAF9iAHFDJIBBDEzLjWYAQCgAQGqAQtnd3Mtd2l6LWltZw&sclient=img&ei=_MHpXumJCcGHlwTnkYm4Cg&bih=766&biw=1440#imgrc=nPr36GbgH40TfM). When we talk about "concern separation" in the beginning, we're referring to architectures widely adopted like the Clean Architecture one. We can use the diagram for that one to reflect that, if needed.
* Any mobile apps running on phones (iPhone / Android) might work pretty fine as resources for the video.
* Any developer cuts, people working on a laptop would also work fine.