package com.newzly.phantom.dsl.specialized

import com.newzly.phantom.dsl.BaseTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers
import com.newzly.phantom.helper.Tables
import com.datastax.driver.core.{Row, Session}
import org.joda.time.DateTime
import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import scala.concurrent.ExecutionContext.Implicits.global

class JodaDateTimeColumn extends BaseTest with ScalaFutures with Matchers with Tables{
  implicit val session: Session = cassandraSession
  it should "test fake" in {
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
    val w = PrimitivesJoda.create(_.pkey,_.int,_.bi)
    w.execute().sync()
    val dt = DateTime.now()
    PrimitivesJoda.insert.value(_.pkey,"w").value(_.int,1)
      .value(_.bi,dt).execute().sync()
    val row =  JodaRow("w",1,dt)
    assert(PrimitivesJoda.select.one.sync().get === row)

  }

}
