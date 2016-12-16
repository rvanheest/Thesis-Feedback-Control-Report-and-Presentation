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
* lossy/loss-less/combined operators
* sources matter
    - hot source - can't be interacted with (mouse)
    - cold source - can be interacted with (iterable collection)
* further distinction in cold source
    - cold async dependent on notion of time
    - cold sync not dependent on notion of time
* backpressure/reactive-streams/reactive-pull
    - tell the source how much data it can produce
    - power given to every operator
    - problems with implementations > merge example
    - does not follow the definition of RP
* alternative solution for overproduction in cold sources
    - can't do much for the hot sources
    - provide a *reactive* solution for cold sources
    - move overproduction control as high up the chain as it can possibly go: to the source
    - automatic way to tell how much data can be processed currently: feedback control


Feedback Control
----------------
* topic that is not well-known in computer science
    - mainly mathematically defined and tought (Laplace transformations)
    - used in all other area's of science and engineering
    - we don't really know the mathematical functions behind datastructures
* examples
    - thermostate
    - space shuttle
    - fosset system
    - washing machine
    - chicken head
* basic feedback system
    - system under control
    - control output
    - setpoint
    - tracking error
    - controller
    - control input
    - example thermostate
* API for creating and running feedback systems
    - production systems
    - using Rx
    - mention derivation of API/operators
* example with ball motion
    ```scala
    def feedbackSystem: BallFeedbackSystem = {
        Controllers.pidController(kp, ki, kd)
            .map(restrictAcceleration)
            .scan(new AccVel)((accvel, acc) => accvel.accelerate(acc))
            .drop(1)
            .scan(Ball1D(ballRadius))((ball, accvel) => ball.move(accvel))
            .sample(16 milliseconds)
            .feedback(_.position)
    }
    
    def feedback: Component[Setpoint, Ball2D] = {
        val fbcX = Component.create[Setpoint, Pos] { case (x, _) => x } >>> feedbackSystem
        val fbcY = Component.create[Setpoint, Pos] { case (_, y) => y } >>> feedbackSystem

        fbcX.combine(fbcY)((ballX, ballY) => Ball2D(ballX, ballY))
    }
    ```


Feedback applied to overproduction
----------------------------------
* recap
    - overproduction: fast producer, slow consumer
    - hot source: can't be interacted with; lossy and lossless operators
    - cold source: can be interacted with; backpressure/reactive streams
    - problems with backpressure: does not belong in reactive API according to Gerard Berry; problems with implementing operators
* general overview of my solution
    - focus on cold source
    - bring overproduction handling to the place where it can be handled best: **the source**
    - put a feedback system between the source and the stream
    - consider the rest of the stream to be hot
* feedback system
    - cold source
    - operators & consumer
    - source control: request `n` elements
    - put resulting elements in buffer
    - buffer drained by operators & consumers
    - controller provides `n`
    - feedback system
    - metric
        + #elements in buffer - fixed setpoint?
        + throughput - ratio between #elements in/out
    - throughput formula's
    - setpoint: 1.0
* Requestable
    - universal interactive interface
    - request elements
    - stream them to you
    - various instances (Iterable/ResultSet)
    - `observe` with sample rate
* demo in slide
* compared to backpressure
    - clean way to wrap a cold source in a reactive programming model
    - overproduction safety
    - everything after the feedback system is considered hot
    - operators stay reactive


Conclusion
----------
* Main achievements
    - analysis of sources and existing solutions for overproduction
    - feedback4s
    - reactive solution to overproduction problem for cold sources
