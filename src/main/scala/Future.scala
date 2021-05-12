import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}

// Placeholder for a value that doesn't exist yet
//Used concurrently - asynchronous, non-blocking parallel

//Futures and promises need an execution context
//Execution.global is an ExecutionContext backed by a ForkJoinPool which manages a limited number of threads (parallelism level).

//Future is an object holding a value that may become available at some point - it can be not completed or completed. When completed, it may be successfully completed or failed with an exception.
object Futures extends App {
  val firstOccurrence: Future[Int] = Future {
    val source = scala.io.Source.fromFile("mytext.txt")
    source.toSeq.indexOfSlice("keyword")
  }

  val firstOccurrence2: Future[Seq[Char]] = Future {
    val source = scala.io.Source.fromFile("mytext.txt")
    source.toSeq
  }


  //The most general form of registering a callback is by using the onComplete method, which takes a callback function of type Try[T] => U.

  val callback1: Unit = firstOccurrence.onComplete {
    case Success(posts) => posts + 1
    case Failure(t) => println(t)
  }

  //The onComplete method is general in the sense that it allows the client to handle the result of both failed and successful future computations. In the case where only successful results need to be handled, the foreach callback can be used:

  val callback2 = firstOccurrence2 foreach { posts =>
    for (post <- posts) println(post)
  }

  //The onComplete and foreach methods both have result type Unit, which means invocations of these methods cannot be chained. Note that this design is intentional, to avoid suggesting that chained invocations may imply an ordering on the execution of the registered callbacks (callbacks registered on the same future are unordered).

  var totalA = 0

  val text = Future {
    "na" * 16 + "BATMAN!!!"
  }

  text foreach { txt =>
    totalA += txt.count(_ == 'a')
  }

  text foreach { txt =>
    totalA += txt.count(_ == 'A')
  }

  //Above, the two callbacks may execute one after the other, in which case the variable totalA holds the expected value 18. However, they could also execute concurrently, so totalA could end up being either 16 or 2, since += is not an atomic operation (i.e. it consists of a read and a write step which may interleave arbitrarily with other reads and writes).

  //We can use combinators on futures. Using callbacks alone would result in nesting and would mean the future results will not be in scope of external code.

  val myNameIs = Future {
    s"${5*"J"} + asmin"
  }

  myNameIs foreach { name =>
    val nameChecker = Future {
      if (name == "JJJJJasmin") "WooHoo"
    else throw new Exception("not true")
  }
    nameChecker foreach {name => ("Output" + name)}
  }

  val name = myNameIs map { name =>
    if (name == "JJJJJasmin") "WooHoo"
    else throw new Exception("not true")
  }
  name foreach { name => println("Output" + name)}

    //can also use flatMap
  val claps = Future {"claps" * 20 }
  val shakes = Future {"shake" * 10}
  def isSound(clap: String, shake: String) = clap.length > 10 && shake.length > 5

  val sounds = for {
  clap <- claps
  shake <- shakes
  if isSound(clap, shake)
  } yield (clap, shake)

  sounds foreach { sounds => println(sounds)}

    //To reach the failure case when composing futures, you can use the recover function

  val catchException = claps map {
    clap => isSound(clap, clap)
  } recover {
    case e => e
  }
}
