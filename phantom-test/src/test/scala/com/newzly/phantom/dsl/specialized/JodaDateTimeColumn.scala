package com.newzly.phantom.dsl.specialized

import org.joda.time.DateTime
import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest

class JodaDateTimeColumn extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "UpdateTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "work fine" in {
    case class JodaRow( pkey: String, int: Int,
                          bi: DateTime)
    class PrimitivesJoda extends CassandraTable[PrimitivesJoda, JodaRow] {
      override def fromRow(r: Row): JodaRow = {
        JodaRow(pkey(r),int(r),  bi(r))
      }
      object pkey extends PrimitiveColumn[String]
      object int extends PrimitiveColumn[Int]
      object bi extends PrimitiveColumn[DateTime]
      val _key = pkey
    }

    object PrimitivesJoda extends PrimitivesJoda {
      override def tableName = "PrimitivesJoda"
    }

    val dt = DateTime.now()
    val row =  JodaRow("w",1,dt)

    val w = PrimitivesJoda.create(_.pkey,_.int,_.bi).execute() flatMap {
      _ => {
        PrimitivesJoda.insert.value(_.pkey,"w").value(_.int,1)
          .value(_.bi,dt).execute()
        }
      } flatMap  {
          _ => PrimitivesJoda.select.one
        }

    w successful {
      case res => assert(res.get === row)
    }
  }
}
