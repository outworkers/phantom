import scalaz._, Scalaz._, iteratee._

object It {
  def main(args:Array[String]) = {
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
    val reader = new java.io.BufferedReader(new java.io.FileReader("numbers.txt"))

    //val enum = Iteratee.enumReader(reader).map(_.toOption)
  }
}