package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.column._
import com.websudos.phantom.keys.{Undroppable, Indexed}
import shapeless.<:!<

import scala.annotation.implicitNotFound

sealed class DropColumn[RR](val column: AbstractColumn[RR])

sealed class CasConditionalOperators[RR](col: AbstractColumn[RR]) {
  @implicitNotFound("Only types with associate primitives can be used.")
  final def is(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.eqs(col.name, col.asCql(value)))
  }
}

sealed trait CasConditionsImplicits extends LowPriorityImplicits {

  @implicitNotFound(msg = "Compare-and-set queries can only be applied to non indexed primitive columns.")
  implicit final def columnToCasCompareColumn[RR](col: AbstractColumn[RR])(implicit ev: col.type <:!< Indexed): CasConditionalOperators[RR] = {
    new CasConditionalOperators[RR](col)
  }

}

sealed trait DropImplicits {

  implicit final def columnToDropColumn[T](col: AbstractColumn[T])(implicit ev: col.type <:!< Undroppable): DropColumn[T] = new DropColumn[T](col)
}

private[phantom] trait ImplicitMechanism extends LowPriorityImplicits
  with ModifyMechanism
  with IndexRestrictions
  with CasConditionsImplicits
  with DropImplicits

