
package fizz

import foo.Foo
import bar.Bar

case class Fizz(f: Foo)

object Main extends App {
  val fizz = Fizz(Foo(Bar(3)))
  println(s"Hello from fizz project! $fizz")
}
