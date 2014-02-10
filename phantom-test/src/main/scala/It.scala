import scalaz._
import Scalaz._
import iteratee._
import Iteratee._
import effect.IO
object It {
  def main(args:Array[String]) {
    if(args.size>0) start()
    else run()
  }
  def start() = {
    val w = new java.io.PrintWriter("numbers.txt")
    val r = new scala.util.Random(0)

    (1 to 1000000).foreach(_ =>
      w.println((1 to 100).map(_ => r.nextInt(10)).mkString)
    )

    w.close()
  }
  def run() = {
    println("test iteratees")
    val l = (length[Int, Id] &= enumerate(Stream(1, 2, 3))).run
    for (i <-l)
      println(i)

    val reader = new java.io.BufferedReader(new java.io.FileReader("numbers.txt"))

    val enum = Iteratee.enumReader(reader).map(_.toOption)
    val split = Iteratee.splitOn[Option[Char], List, IO](_.cata(_ != '\n', false))
    val lines = split.run(enum).map(_.sequence.map(_.mkString))
   // val tw = Iteratee.takeWhile[Option[String], Id](_.isDefined)  map (m=>Console.println(m))
   val pred = (_: String).count(_ == '0') >= 20
    val filtered = Iteratee.filter[Option[String], IO](_.cata(pred, true)).run(lines)
    val printAction = (Iteratee.putStrTo[Option[String]](System.out) &= filtered).run
    printAction.unsafePerformIO()

    // val stw = tw.run(lines)
    //val printAction = (Iteratee.putStrTo[Option[String]](System.out) &= lines).run
    //val printAction = (tw &= lines).run
    //printAction.unsafePerformIO()
  }
}