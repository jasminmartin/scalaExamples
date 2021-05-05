//class Generics {
//
//  def foo[A](a: A, num: Int): A {
//    a
//  }
//  A = String
//  def foo(a: String, num: Int): String {
//    a
//  }
//  A = Int(a: Int, num: Int): Int {
//    a
//  }
//  foo("Jaz", 3) "Jaz"
//  foo(4, 3) 4
//  def count_chars(s: String): Int = s.length
//  def bar[B](b: B, f: B => Int): Int {
//    f(b)
//  }
//  def bar(b: String, f: String => Int): Int {
//    f(b)
//  }
//  def bar(b: Int, f: Int => Int): Int {
//    f(b)
//  }
//  bar("Jaz", count_chars) 3
//  bar(2, x => x + 2)  4
//  def add_2(x: Int): Int = x + 2
//  count_chars
//  class List[String] {
//    def map[Int](f: String => Int): List[Int]
//  }
//  class List[Int] {
//    def map[Int](f: Int => Int): List[Int]
//  }
//  class List[A] {
//    def map[B](f: A => B): List[B] {
//    }
//  }
//}
