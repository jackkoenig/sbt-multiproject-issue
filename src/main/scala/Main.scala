
import foo.Foo
import bar.Bar
import fizz.Fizz

case class Root(f: Fizz)

object Main extends App {
  val value = Root(Fizz(Foo(Bar(3))))
  println(s"Hello from the root project! $value")
}
