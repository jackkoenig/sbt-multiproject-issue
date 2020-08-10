
package bar

import upickle.default._

case class Bar(x: Int)

object BarMain extends App {
  val b = Bar(23)
  println(s"Hello from Bar! $b ${write(List(1,2,3))}")

}
