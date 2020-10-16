<img src="https://d1bv04v5uizu9x.cloudfront.net/assets/event_images/2020-10-07-kotlin-functional-android-development.png"/>

---

## Why FP? 🤔

<div class="card">
    Achieve <b>determinism</b> to fight race conditions.
</div>

* Pure functions 👉 improve code reasoning.
* Keep side effects under control.

---

## Concern separation

<div class="card">
    Staying <b>declarative and deferred</b>.
</div>

* Memory representation of the program (algebras).
* Decoupled runtime - optimizations.
* Simple example: Kotlin `Sequences` 👉 terminal ops to consume - `toList()`

---

## Another example of this?

<div class="card">
    <b>Jetpack Compose</b> 😲
</div>

<img src="/assets/jetpack-compose.svg"/>

---

<div>
  <img style="vertical-align:middle;width:120px;" src="/assets/jetpack-compose.svg"/>
  <h2 style="display:inline;"><span style="">Compose</span></h2>
</div>

<div class="card">
    Also applies <b>concern separation</b>
</div>

* Creates an in-memory representation of the UI tree 🌲
* The runtime interprets it by applying desired optimizations.

*(Run composable functions in parallel, in different order, smart recomposition...).*

---

## Composable functions

<div class="card">
    Similar to suspend functions 🤔
</div>

* <u>Description</u> of an effect to render UI.
* Only callable from within other composable functions or a prepared **environment** 👉 integration point 👉 `setContent {}`
* Enforces a usage scope to keep control over it.

---

## Composable functions

```kotlin
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    /* ... */
    setContent { // integration point for Android
      AppTheme {
        MainContent() // composable tree
      }
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* The integration point interprets the in-memory UI tree 👉 skia in Android
* Allow using different runtimes.

---

## Android architecture?

<div class="card">
    We can leverage the same idea using <b>suspend</b> 🍃.
</div>

* Flags a potentially blocking long running computation 👉 effect 🌀
* Enforces it to run under a prepared environment (Coroutine).
* Makes the effect compile **time tracked**.

---

## Flag effects as suspend

<div class="card">
 Make 'em <b>pure</b>!
</div>

```diff
interface UserService {
-  fun loadUser(): User
+  suspend fun loadUser(): User
}
```

```diff
class UserPersistence {
-  fun loadUser(): User = TODO()
+  suspend fun loadUser(): User = TODO()
}
```

```diff
class AnalyticsTracker {
-  fun trackEvent(event: Event): Unit = TODO()
+  suspend fun trackEvent(event: Event): Unit = TODO()
}
```

---

## But we'll need a runtime

<div class="card">
    Every suspended program <b>requires an environment</b> (runtime) to run.
</div>

* Suspend makes our program <u>declarative</u> 👉 description of effects.
* Crawls the call stack up until the integration point 👉 coroutine launch.

---

## Environment in KotlinX

* KotlinX Coroutines builders 👉 launch, async.

```kotlin
class MyFragment: Fragment() {
  override fun onViewCreated(...) {
    /* ... */
    viewLifecycleOwner.lifecycleScope.launch {
      // suspended program 🍃
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Environment in FP

* Arrow Fx Coroutines Environment.

```kotlin
class MyFragment: Fragment() {
  override fun onViewCreated(...) {
    /* ... */
    val env = Environment()
    val cancellable = env.unsafeRunAsyncCancellable(
     { /* suspended program 🍃 */ },
     { e -> /* handle errors unhandled by the program */ },
     { a -> /* handle result of the program */ }
    )
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Takes care of the execution strategy / context to run the program.

---

## App entry points

<div class="card">
    Also called <b>"edge of the world"</b>.
</div>

* Android 🙅‍♂️ no `suspend` entry points.
* Inversion of control.
* Lifecycle callbacks 👉 entry points to hook logic.

---

## The suspended program

<div class="card">
    Or in other words, our pure logics.
</div>

* Leverage data types to <b>raise concerns over the data</b>.
* `Either<L, R>` will be our friend 🤗

---

# Railway oriented programming 🚂

<div class="card">
  Programs as a composition of functions that <b>can succeed or fail</b>.
</b>

---

<video data-autoplay src="https://d1bv04v5uizu9x.cloudfront.net/assets/animations/academy/errors-railways/47deg_Academy_069_Error_and_railways_webm_1of3/47deg_Academy_069_ERR_Rail_01_General_Concepts%201.webm" type="video/webm"></video>

---

## Railway oriented programming

By **Scott Wlaschin** from 11 May 2013

  - 📝 Post 👇 [fsharpforfunandprofit.com/posts/recipe-part2/](https://fsharpforfunandprofit.com/posts/recipe-part2/)
  - 🎬 Talk video + slides 👇 [fsharpforfunandprofit.com/rop/](https://fsharpforfunandprofit.com/rop/)

---

## `Either<L, R>`

<div class="card">
    A path <u>we want to follow</u>, vs an "alternative" one
</div>

* Compute over the happy path 👉 plug error handlers.
* Make disjunction explicit 👉 <u>both paths need to be handled</u>.
* Makes our program complete.

---

## In code 👩🏾‍💻

`Either<A, B>` models this scenario.

```kotlin
sealed class Either<out A, out B> {
  data class Left<out A>(val a: A) : Either<A, Nothing>()
  data class Right<out B>(val b: B) : Either<Nothing, B>()

  // operations like map, flatMap, fold, mapLeft...
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Convention: Errors on `Left`, success on the `Right`.
* Biased towards the `Right` side 👉 compute over the happy path.

---

## fold to handle both sides

```kotlin
fun loadUser: Either<UserNotFound, User> =
  Right(User("John")) // or Left(UserNotFound)

// Alternatively: user.right() or exception.left()

val user: Either<UserNotFound, User> =
  loadUser().fold(
    ifLeft = { e -> handleError(e) },
    ifRight = { user -> render(user) }
  )
```
<!-- .element: class="arrow"  -->

---

## Nullable data

<strike>`Option<A>`</strike> getting deprecated.
* 💡 Alternative 1: `A?`
* 💡 Alternative 2: `Either<Unit, A>`

```kotlin
typealias EpisodeNotFound = Unit

fun EpisodeDB.loadEpisode(episodeId: String): Either<EpisodeNotFound, List<Character>> =
  Either.fromNullable(loadEpisode("id1"))
    .map { episode -> episode.characters }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Integration with effects

<div class="card">
    <b>Either.catch</b> for 3rd party calls.
</div>

```kotlin
suspend fun loadSpeakers(): Either<Errors, List<Speaker>> =
  Either.catch { service.loadSpeakers() } // any suspended op
    .mapLeft { it.toDomainError() } // strongly type errors
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Combined with `mapLeft` to map the `Throwable` into something else.
* This program could <b>never run outside of a controlled environment</b> 🌟

---

## Composing logics

* We got means to write our logic as pure functions.
* We need the glue for them 👉 `flatMap`.
* Any program 👉 sequence of computations.

<div class="card">
    How does <code>flatMap</code> work for <code>Either</code>?
</div>

---

<video data-autoplay src="https://d1bv04v5uizu9x.cloudfront.net/assets/animations/academy/errors-railways/47deg_Academy_069_Error_and_railways_webm_2of3/47deg_Academy_069_ERR_Rail_05_Bind_01.webm" type="video/webm"></video>

---

## Failing fast

<div class="card">
    When two operations are dependent, you <b>cannot perform the second one without a successful result by the first one</b>.
</div>

* This means we can save computation time in that case.
* `Either#flatMap` 👉 <u>sequential</u> computations that return `Either`.

---

<video data-autoplay src="https://d1bv04v5uizu9x.cloudfront.net/assets/animations/academy/errors-railways/47deg_Academy_069_Error_and_railways_webm_2of3/47deg_Academy_069_ERR_Rail_05_Bind_02.webm" type="video/webm"></video>

---

## Sequential effects

<div class="card">
  Here's a program with 2 dependent operations.
</div>

```kotlin
suspend fun loadSpeaker(id: SpeakerId): Either<SpeakerNotFound, Speaker> =
  TODO()

suspend fun loadTalks(ids: List<TalkId>): Either<InvalidIds, List<Talk>> =
  TODO()

suspend fun main() {
  val talks = loadSpeaker("SomeId")
                .flatMap { loadTalks(it.talkIds) }

  // listOf(Talk(...), Talk(...), Talk(...))
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

### Sequential effects - bindings

<div class="card">
  Alternative syntax 👉 Either bindings 👉 sugar 🍬
</div>

```kotlin
suspend fun main() {
  val talks = either  { // Either<Error, List<Talk>>
    val speaker = !loadSpeaker("SomeId")
    val talks = !loadTalks(speaker.talkIds)
    talks
  }

  // listOf(Talk(...), Talk(...), Talk(...))
}
//sampleEnd
```
<!-- .element: class="arrow"  -->

---

## Fail fast

First operation fails 👉 short circuits

```kotlin
suspend fun main() {
  val events = either {
    val speaker = !loadSpeaker("SomeId") // Left(SpeakerNotFound)!
    val talks = !loadTalks(speaker.talkIds)
    val events = talks.map { !loadEvent(it.event) }
    events
  }

  println(events) // Left(SpeakerNotFound)
}
```
<!-- .element: class="arrow"  -->

---

## Error accumulation?

* Interested in **all errors occurring**, not a single one.
* Only in the context of **independent computations**.

---

## Validated & ValidatedNel

```kotlin
sealed class Validated<out E, out A> {
  data class Valid<out A>(val a: A) : Validated<Nothing, A>()
  data class Invalid<out E>(val e: E) : Validated<E, Nothing>()
}

typealias ValidatedNel<E, A> = Validated<NonEmptyList<E>, A>
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `ValidatedNel` alias for error accumulation on a `NonEmptyList`.

---

## Validated & ValidatedNel

<div class="card">
    Independent data validation with the applicative.
</div>

```kotlin
//sampleStart
suspend fun loadSpeaker(id: SpeakerId): ValidatedNel<SpeakerNotFound, Speaker> =
  Validated.catchNel { throw Exception("Boom 💥!!") }.mapLeft { SpeakerNotFound.nel() }

suspend fun loadEvents(ids: List<TalkId>): ValidatedNel<InvalidIds, List<Event>> =
  Validated.catchNel { throw Exception("Boom 💥!!") }.mapLeft { InvalidIds.nel() }

suspend fun main() {
  val accumulator = NonEmptyList.semigroup<Error>()

  val res = Validated.applicative(accumulator)
    .tupledN(
      loadSpeaker("SomeId"),
      loadEvents(listOf("1", "2"))
    )

  println(res)
  // Invalid(NonEmptyList([Error.SpeakerNotFound, Error.InvalidIds]))
}
//sampleEnd
```
<!-- .element: class="arrow"  -->

---

## Limitations

* `Either` or `Validated` are **eager**.
* We want them deferred 👉 declarative.
* `suspend` will do the work 👍

<div class="card">
  But what about <b>threading / concurrency</b>?
</div>

---

## Arrow Fx Coroutines 🤲

<img src="/assets/logo_arrowfx.svg" style="width:300px;margin-top:40px;"/>

---

<div>
  <img style="vertical-align:middle;width:50px;padding-bottom:14px;margin-right: 12px;" src="/assets/logo_arrowfx.svg"/>
  <h2 style="display:inline;"><span style="">Arrow Fx Coroutines</span></h2>
</div>

* Functional concurrency framework.
* Functional operators to run <u>suspended effects</u>.
* Cancellation system ✅ 👉 All Arrow Fx operators **automatically check for cancellation**.

---

## Environment

<div class="card">
  Our <b>runtime</b>. Picks the execution strategy.
</div>

```kotlin
// synchronous
env.unsafeRunSync { greet() }

// asynchronous
env.unsafeRunAsync(
  fa = { greet() },
  e = { e -> println(e)},
  a = { a -> println(a) }
)

// cancellable asynchronous
val disposable = env.unsafeRunAsyncCancellable(
  fa = { greet() },
  e = { e -> println(e)},
  a = { a -> println(a) }
)

disposable() // cancel!
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* `interface` to implement custom ones.

---

## evalOn(ctx)

<div class="card">
    Offload an effect to an arbitrary context and <b>get back to the original one</b>.
</div>

```kotlin
suspend fun loadTalks(ids: List<TalkId>): Either<Error.TalksNotFound, List<Talk>> =
  evalOn(IOPool) { // supports any suspended effects
      Either.catch { fetchTalksFromNetwork() }
          .mapLeft { Error.TalksNotFound }
  }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## parMapN

* Run N parallel effects.
* Cancel parent 👉 cancels all children.
* Child failure 👉 cancels other children 😲
* All results are required.

```kotlin
suspend fun loadEvent(): Event {
    val op1 = suspend { loadSpeakers() }
    val op2 = suspend { loadRooms() }
    val op3 = suspend { loadVenues() }

    return parMapN(op1, op2, op3) { speakers, rooms, venues ->
        Event(speakers, rooms, venues)
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## parTupledN

* Same without callback style.
* Returns a tuple with all the results.
* Cancellation works the same way.

```kotlin
suspend fun loadEvent(): Event {
    val op1 = suspend { loadSpeakers() }
    val op2 = suspend { loadRooms() }
    val op3 = suspend { loadVenues() }

    val res: Triple<List<Speaker>, List<Room>, List<Venue>> =
        parTupledN(op1, op2, op3)

    return Event(res.first, res.second, res.third)
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## parTraverse

* Traverses a <u>dynamic amount</u> of elements running an effect for each, all of them <b>in parallel</b>.
* Cancellation works the same way.

```kotlin
suspend fun loadEvents() {
    val eventIds = listOf(1, 2, 3)

    return eventIds.parTraverse(IOPool) { id ->
      eventService.loadEvent(id)
    }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## parSequence

* Traverse <u>list of effects</u>, run all in parallel.
* Cancellation works the same.

```kotlin
suspend fun main() {
    val ops = listOf(
        suspend { service.loadTalks(eventId1) },
        suspend { service.loadTalks(eventId2) },
        suspend { service.loadTalks(eventId3) })

    ops.parSequence()
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## raceN

* Racing parallel effects.
* Returns the winner, cancels losers.
* Cancelling parent 👉 cancels all children.
* Child failure 👉 cancels other children.

```kotlin
suspend fun main() {
    val res = raceN(::op1, ::op2, ::op3) // suspended ops
    res.fold(
      ifA = {},
      ifB = {},
      ifC = {}
    )
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Android use case

<div class="card">
    Racing against the Android lifecycle 🏎 🏁
</div>

```kotlin
suspend fun AppCompatActivity.suspendUntilDestroy() =
  suspendCoroutine<Unit> { cont ->
    val lifecycleObserver = object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
      fun destroyListener() {
        cont.resumeWith(Result.success(Unit))
      }
    }
    this.lifecycle.addObserver(lifecycleObserver)
  }

suspend fun longRunningComputation(): Int = evalOn(IOPool) {
  delay(5000)
  1
}

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val env = Environment()
    env.unsafeRunAsync {
      raceN(::longRunningComputation, ::suspendUntilDestroy)
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Retrying / repeating

<div class="card">
  Highly composable retry policies for suspended effects.
</div>

```kotlin
fun <A> complexPolicy() =
    Schedule.exponential<A>(10.milliseconds)
        .whileOutput { it.seconds < 60.seconds }
        .andThen(spaced<A>(60.seconds) and recurs(100))

suspend fun loadTalk(id: TalkId): List<Talks> =
    retry(complexPolicy()) {
        fetchTalk(id) // retry any suspended effect
    }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Concurrent Error handling

<div class="card">
  All Arrow Fx Coroutines operators rethrow on failure.
</div>

* Can use `Either.catch`, `Validated.catch`, `Validated.catchNel`, at any level ✨

---

## And FRP?

<div class="card">
    Android apps as a <b>combination of Streams</b>.
</div>

* Inversion of control is strong 👉 Streams can <u>bring determinism</u> to it.
* Unidirectional data flow architectures.
* Lifecycle events, user interactions, application state updates...

<img width="200px;" style="margin-top:20px;" src="/assets/refresh.svg"/>

---

## What we need

* Emit multiple times.
* **Embed suspended effects.**
* **Compatible with all the Arrow Fx Coroutines operators**.
* Cold streams 👉 purity 👉 declarative.
* Composition.

---

## 💡 Pull based Stream

* vs push based alternatives (RxJava, Reactor)
* Receiver `suspends` until data can be pulled.
* Built in back-pressure 🌟

---

## Embedding effects

<div class="card">
    Evaluates a suspended effect, <b>emits result</b>.
</div>

```kotlin
val s = Stream.effect { println("Run!") }
  .flatMap {}
  .map {}
  ...

s.drain() // consume stream

// Run!
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* **Cold**. Describes what will happen when the stream is interpreted.
* Terminal operator to run it.
* Errors **raised into the Stream**.

---

## Embedding effects

<div class="card">
    Any Arrow Fx Coroutines operators can be evaluated.
</div>

* Result is emitted over the `Stream`.

```kotlin
val s = Stream.effect { // any suspended effect
  parMapN(op1, op2, op3) { speakers, rooms, venues ->
    Event(speakers, rooms, venues)
  }
}

s.drain()
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Threading via Arrow Fx Coroutines: `parMapN`, `parTupledN`, `evalOn`, `parTraverse`, `parSequence`... etc.

---

## parJoin

<div class="card">
    Composing streams <b>in parallel</b>.
</div>

```kotlin
val s1 = Stream.effect { 1 }
val s2 = Stream.effect { 2 }
val s3 = Stream.effect { 3 }

val program = Stream(s1, s2, s3).parJoinUnbounded()
// or parJoin(maxOpen = 3)

val res = program.toList()
println(res)

// [2, 1, 3]
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Concurrently 👉 emits values as they come 👉 unexpected order.

---

## async wrapper

<div class="card">
    Wrap callback based apis.
</div>

```kotlin
fun SwipeRefreshLayout.refreshes(): Stream<Unit> =
    Stream.callback {
      val listener = OnRefreshListener {
        emit(Unit)
      }
      this@refreshes.setOnRefreshListener(listener)
    }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Cancellable async wrapper

<div class="card">
    Wrap callback based apis in a <b>cancellable</b> Stream.
</div>

* Return a `CancelToken` to release and <u>avoid leaks</u>.

```kotlin
fun SwipeRefreshLayout.refreshes(): Stream<Unit> =
    Stream.cancellable {
      val listener = OnRefreshListener {
        emit(Unit)
      }
      this@refreshes.setOnRefreshListener(listener)

      // Return a cancellation token
      CancelToken { this@refreshes.removeListener(listener) }
    }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## bracket

<div class="card">
    Scope resources to the Stream life span.
</div>

* Calls release lambda once the Stream terminates.

```kotlin
Stream.bracket({ openFile() }, { closeFile() })
   .effectMap { canWorkWithFile() }
   .handleErrorWith { alternativeResult() }
   .drain()
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Other relevant operators

<div class="card">
    The usual ones.
</div>

```kotlin
Stream.effect { loadSpeakers() }
  .handleErrorWith { Stream.empty() }
  .effectMap { loadTalks(it.map { it.id }) } // flatMap + effect
  .map { talks -> talks.map { it.id } }
  .drain() // terminal - suspend
```
<!-- .element: class="arrow" data-highlight-only="true" -->

* Stays declarative and deferred until `drain()`

---

## interruptWhen + lifecycle

<div class="card">
    Arbitrary Stream interruption by <b>racing streams</b>.
</div>

* Will terminate your program as soon as a lifecycle `ON_DESTROY` event is emited.

```kotlin
program()
    .interruptWhen(lifecycleDestroy()) // races both
    .drain()
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## interruptWhen + lifecycle

<div class="card">
    Stream out of lifecycle events 👉 destroy
</div>

```kotlin
fun Fragment.lifecycleDestroy(): Stream<Boolean> =
    Stream.callback {
      viewLifecycleOwner.lifecycle.addObserver(
          LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
              emit(true)
            }
          })
    }
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Consuming streams safely

<div class="card">
  Terminal ops are suspend 👉 <b>Stream has to run within a safe environment</b>.
</div>

```kotlin
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val env = Environment()
    env.unsafeRunAsync {
      HomeDependencies.program() // program as a Stream
        .interruptWhen(lifecycleDestroy())
        .drain() // suspended - terminal op to consume
    }
  }
}
```
<!-- .element: class="arrow" data-highlight-only="true" -->

---

## Thank you! 🙌

<div>
  <img style="vertical-align:middle;width:140px;margin-right:20px;" src="/assets/twitter_logo.jpg"/>
  <h2 style="display:inline;"><span style=""><u>@JorgeCastilloPr</u></span></h2>
</div>

<div class="card">
  To expand on these ideas 👉 Fully-fledged <b>Functional Android course</b>.
</div>

* www.47deg.com/trainings/Functional-Android-development/
* Bookable as a group / company.
