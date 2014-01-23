package com.newzly.phantom.dsl.specialized

import com.datastax.driver.core.{ Row }
import org.joda.time.DateTime
import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import org.scalatest.{Assertions, Matchers}
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.helper.{ BaseTest, Tables, TestRow }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._

class JodaDateTimeColumn extends BaseTest with Matchers with Tables  with Assertions with AsyncAssertions {
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
