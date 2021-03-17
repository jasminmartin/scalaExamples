//Main Source: https://stackoverflow.com/questions/35120082/how-to-get-started-with-akka-streams

//3 key properties - uses the Reactive Streams spec as a base, which purely does no.1.

//1 - asynchronous stream processing with non-blocking backpressure. Great for unbound data streams.
//2 - They provide an abstraction for an evaluation engine for the streams, which is called Materializer.
//3 - Programs are formulated as reusable building blocks, which are represented as the three main types Source, Sink and Flow.

//Reactive Streams Components (this API contains less features than Akka Streams):
//1 - Publisher
//2 - Subscriber
//3 - Subscription
//4 - Processor

// scalastyle:off magic.number
// scalastyle:off regex

import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent._

//Akka streams requires an implicit Actor System
implicit val system = ActorSystem("TestSystem")

//Source - has a single output channel and no input channel. They are lazy, requiring specific evaluation (the run() method).

//Finite Sources
val source1 = Source.single("Single source")
val source2 = Source(1 to 3)

//Infinite Source - here we are evaluating the source with the runForEach and using the 'take' to create an artificial stopping point.
val source3: Source[Int, NotUsed] = Source.repeat(5)
source3 take 3 runForeach println

//Sink - consume the data - single input channel, no output. They specify the behavior of the data collector in a reusable way and without evaluating the stream.
val source4 = Source(1 to 10)
val sink1 = Sink.foreach[Int](elem => println(s"sink received: $elem"))

//Connecting the source to the sink requires a flow. This is done using the 'to' method. Here I am returning a runnable flow - a special kind of flow that can be ran using the run method.
val flow: RunnableGraph[NotUsed] = source4 to sink1
flow.run()

//Flows - act as connectors between streams and can be used to transform elements.
val source5: Source[Int, NotUsed] = Source(1 to 8)
val sink2: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

val invertFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(elem => elem * -1)
val doubleFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(elem => elem * 2)

//Using the via method we can connect a Source and Sink with a Flow
val runnable: RunnableGraph[NotUsed] = source5 via invertFlow via doubleFlow to sink2
runnable.run()

//Note, for the source and sink, the second type param denotes the materialized value
val source7: Source[Int, NotUsed] = Source(1 to 3)
val sink3: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

//For flows, the third param denotes the materialized value
val flow2: Flow[Int, String, NotUsed] = Flow[Int].map(x => x.toString)

//Look this is a source ! (despite having flows connected)
val surpriseSource: Source[Int, NotUsed] = source5 via invertFlow via doubleFlow

//Look this is a sink ! (despite having flows connected)
val surpriseSink: Sink[Int, NotUsed] = invertFlow via doubleFlow to sink3

//flows are completely independent from any data producers and consumers. They only transform the data and forward it to the output channel
//uniqueGraph1 and uniqueGraph2 represent completely new streams - they do not share any data despite using the same building blocks
val uniqueGraph1: RunnableGraph[NotUsed] = source5 via invertFlow via doubleFlow to sink2
val uniqueGraph2: RunnableGraph[NotUsed] = source5 via invertFlow via doubleFlow to sink2

//More technical example - the clickStream
// https://gist.github.com/kiritsuku/8533e23f5856bd459e27#file-clickstreamexample-scala

//Materializers - Finally we connect the Flow to the previously prepared Sink using toMat.
//Example 1
val source8: Source[Int, NotUsed] = Source(1 to 12)
val count: Flow[Int, Int, NotUsed] = Flow[Int].map(_ => 1)
val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
val counterGraph: RunnableGraph[Future[Int]] =
  source8
    //.viaMat(count)(Keep.left) //this is equivalent to .via(count), keeps the materialized value from the source
    .via(count)
    //.toMat(sumSink)(Keep.both) //tuple of both the sink and source values
    .toMat(sumSink)(Keep.right) //just the Future[Int] from the sink

val rightSumSink: Future[Int] = counterGraph.run()
//val bothSumSink: (Int, Future[Int]) = counterGraph.run() // the result when using keep.both

//More Complex Graphs
//Akka Streams intentionally separate the linear stream structures (Flows) from the non-linear, branching ones (Graphs) in order to offer the most convenient API for both of these cases. Graphs can express arbitrarily complex stream setups at the expense of not reading as familiarly as collection transformations.
//Elements that can be used to form such "fan-out" (or "fan-in") structures are referred to as "junctions" in Akka Streams.

//We can create complex graphs or complex flows, the graphs being the full runnable stream and the complex flow being a partial graph that can be used as part of a stream
//The commented out code show the complex Flow. For the complex flow, remove the sinks and tie the two streams back together using the merge function. The flow is then placed into a RunnableGraph

//Dummy data set up
case class Tweets(author: String, hashtags: List[String])
val bcastSource: Source[Tweets, NotUsed]
val writeAuthors: Sink[String, Unit] = ???
val writeHashtags: Sink[String, Unit] = ???

//val complexSource = Flow.fromGraph(GraphDSL.create() { implicit b =>

val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
  import GraphDSL.Implicits._
  //Broadcast splits the flow into 2 streams
  val bcast = b.add(Broadcast[Tweets](2))
//    val merge = b.add(Merge[Tweets](2))

  //the ~> symbol is akin to 'via'
  bcastSource ~> bcast.in
  bcast.out(0) ~> Flow[Tweets].map(_.author)
  bcast.out(1) ~> Flow[Tweets].mapConcat(_.hashtags.toList)

  //The shape of the Graph is Closed as the output is placed into the sinks writeAuthors and writeHashtags
  ClosedShape
//    FlowShape(bcast.in, merge.out)
})

//val countingTweets: Flow[Tweets, Int, NotUsed] = Flow[Tweets].map(_ => 1)

//complexSource via countingTweets to Sink.ignore
