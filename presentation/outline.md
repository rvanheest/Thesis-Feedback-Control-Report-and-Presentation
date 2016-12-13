Presentation outline
====================

Welcome
-------
* thanks for coming; very glad to have you all here
* especially welcome to the thesis committee
* Erik joins from Silicon Valley
* goal of this afternoon: present the results of my master thesis research and finish my studies at TU Delft
* schedule:
    - presentation by me
    - questions from the audience
    - questions from the thesis committee


Introduction
------------
* Venkat Subramaniam: "We work in a field where right about every ten years, we'll think of a new name for what we already do, and get all excited about it. This time around it's reactive programming... "
* Reactive programming invented by the French computer scientist Gerard Berry (1989):
    - *Interactive programs* interact at their own speed with users or with other programs
    - *Reactive programs* also maintain a continuous interaction with their environment, but **at a speed which is determined by the environment**, not by the program itself.
    - Example (code) of interactive program (while-loop over `Iterable`, rewritten to foreach `List(1, 2, 3, 4).foreach(i => println(i))`)
    - Example (code) of reactive program (`Observable.just(1, 2, 3, 4).subscribe(i => println(i))`)
    - Example of operator similarity between interactive and reactive
    - emphasize the point that in reactive programming the **producer is in charge** of the data flow


Overproduction
--------------
* fast producer, slow consumer
    - producer is in charge
    - naive solution: buffering - doesn't work
    - lossy/loss-less operators
* sources matter
    - hot source - can't be interacted with (mouse)
    - cold source - can be interacted with (iterable collection)
* further distinction in cold source
    - cold async dependent on notion of time
    - cold sync not dependent on notion of time
    - FIVE effects of computation
* backpressure/reactive-streams/reactive-pull
    - tell the source how much data it can produce
    - power given to every operator
    - problems with implementations
        > merge
        > groupBy
    - does not follow the definition of RP
* alternative solution for overproduction in cold sources
    - can't do much for the hot sources
    - provide a *reactive* solution for cold sources
    - move overproduction control as high up the chain as it can possibly go: to the source
    - automatic way to tell how much data can be processed currently: feedback control


Feedback Control
----------------
* topic that is not well-known in computer science
    - used in all other area's of science and engineering
* examples
    - thermostate
    - space shuttle
    - 
* basic feedback system
    - system under control
    - control output
    - setpoint
    - tracking error
    - controller
    - control input
    - example washing machine
* API for creating and running feedback systems
    - production systems
    - using Rx
    - mention derivation of API/operators
    - example with ball motion









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


