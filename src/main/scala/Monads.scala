object Monads extends App {

//  A parametrized type — e.g, Option[T]
//  Unit (return) — e.g, Option.apply
//  FlatMap (bind) —e.g, Option.flatMap

  //List

  val list1: List[Int] = List.apply(1)
  val list2: List[Int] = List(1)

  println(s"$list1 and $list2 are equal. They both represent the Unit function, which lifts value 1 into the list.")

  println("Unit is A => M[A]. Int is lifted into the List[Int] monad.")

  def makeListOfDoubles(int: Int): List[Double] = {
    List(int.toDouble)
  }

  val list3: Seq[Double] = list1.flatMap[Double](makeListOfDoubles)

  println(s"$list3 show's flatMap compressing the  List[List[Double]] into a List[Double]")

  val list4 = list1.map(_.toDouble)
  println(s"Whereas $list4 show's map creating the  List[List[Double]]")

  //second example

  val names = List("Jaz", "Jack", "Fred", "Helen", "Juniper", "Joseline", "Jamie")
  println(s"$names contains the parameritized type String")

  val names2: List[String] = List("Jaz", "Jack", "Fred", "Helen", "Juniper", "Joseline", "Jamie")
  println(s"$names2 is identical but is using the apply function (a => M[A]) to lift the String into the List")

  def containsJ(name: String): List[String] = {
    List(if(name.toUpperCase.contains("J")) name else "")
  }

  def containsA(name: String): List[String] = {
    List(if(name.toUpperCase.contains("A")) name else "")
  }

  def numberOfChar(name: String): List[Int] = {
    List(name.length)
  }

  val numchar: List[Int] = names2.flatMap(containsJ).flatMap(containsA).flatMap(numberOfChar)
  println(s"numchar = $numchar")

 val forcomp = names2.flatMap { name =>
   for {
   a <- containsJ(name)
   b <- containsA(a)
   c <- numberOfChar(b)
   } yield c
 }

  //Option
  println("Parametrized type: Option[Int] can only hold Ints. Option[String] can only hold string")

  val op: Option[String] = Option("hello")
  val some: Some[String] = Some("hello")
  val none: Option[Nothing] = None

  def countLetters(name: String): Option[Int] = {
    Option(name.length)
  }

  def times2(num: Int): Option[Int] = {
    Option(num * 2)
  }

  def half(num: Int): Option[Double] = {
    Option(num/2)
  }

  op.flatMap(countLetters).flatMap(times2).flatMap(half)
  none.flatMap(countLetters).flatMap(times2).flatMap(half)

  val someCase = op.flatMap { maybeName =>
    for {
    numOfLetters <- countLetters(maybeName)
    twice <- times2(numOfLetters)
    half <- half(twice)
    } yield half
  }
  println(s"someCase: $someCase")

  val noneCase = none.flatMap { maybeName =>
    for {
      numOfLetters <- countLetters(maybeName)
      twice <- times2(numOfLetters)
      half <- half(twice)
    } yield half
  }
  println(s"noneCase: $noneCase")

  println("Option has two subtypes: Some and None")
  println(s"See the types above that show how specific declaration changes the type. Some[String], which is a specific child type of Option[String].")

  def numOfLetters(word: String): Option[Int] = {
    Option(word.length)
  }

  val op2 = op.flatMap(numOfLetters)
  val op3 = op.map(numOfLetters)
  val op4: Option[Int] = none.flatMap(numOfLetters)

  println("Option is success biased. If it finds a value in the Option, it executes, otherwise it doesn't. ")

  println("To access the value we can call Option.getOrElse - it reruns the contents of Option if available, or a default value if it’s not.")

  val a: Int = op4.getOrElse(1)

  println("Option can also be accessed via pattern matching")

  val o1: Option[String] = Some("Hello")

  o1 match {
    case Some(x) => x
    case None => "nothing"
  }

  //Either
  val left: Either[String, Int] = Left("hi")
  val right: Either[String, _] = Right(1)

  val lifting = Right.apply(1)

  def divideBy2(x: Int): Either[String, Int] = {
    divideByX(x, 2)
  }

  def divideByX(x: Int, y: Int): Either[String, Int] = {
    y match {
      case 0 => Left("Cannot divide!")
      case _ => Right(x/y)
    }
  }

  def greaterThan2(num: Int): Either[String, String] = {
    num match {
      case x > 2 => Right("Is bigger than 2")
      case _ => Left("It's less than 2")
    }
  }

  left.flatMap(divideByX(2,  2))


  //MAP
  //To access the values (not keys), uses special method mapValues
  val aMap = Map((1 -> "a"), (2 -> "b"))
  aMap.view.mapValues(_.toUpperCase).toMap

  //Set
  val badger = Set(1,2,3)

}
