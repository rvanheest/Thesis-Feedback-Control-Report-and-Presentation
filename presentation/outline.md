Presentation outline
====================


Introduction
------------
* story about backpressure with screenshot of https://github.com/ReactiveX/RxJava/releases?after=0.20.0-RC3
    - use wiki article https://github.com/ReactiveX/RxJava/wiki/Backpressure#how-a-subscriber-establishes-reactive-pull-backpressure
* backpressure as solution to overproduction
    - what is overproduction (fast producer, slow consumer)
    - backpressure approach
    - Reactive Streams interface
* question: is RS and backpressure this still reactive?
    - formal definition by Berry
    - RS derivation (don't show!!!) shows it is not reactive (+ screenshot from https://youtu.be/pOl4E8x3fmw?t=32m59s)
    - leads to all kinds of interesting questions about how to implement operators like merge en group (don't go into this, only mention it)
    - quote from (https://github.com/ReactiveX/RxJava/wiki/backpressure): "*Backpressure doesn’t make the problem of an overproducing Observable or an underconsuming Subscriber go away. **It just moves the problem up the chain of operators to a point where it can be handled better.***"
* various kinds of sources
    - hot vs cold
    - [sync vs async] cold
    - backpressure/RS can only solve for (sync/async) cold sources (which is the not-reactive part)


Connection with feedback control
--------------------------------
* overproduction seen as a control problem



    - why not move it even further, to the *source* of the stream?
    - various kinds of sources (hot/cold/sync/async)
    - restrict to (async) cold for the rest of this talk


