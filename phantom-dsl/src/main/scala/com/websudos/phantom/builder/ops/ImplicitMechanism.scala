package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.column._

import scala.annotation.implicitNotFound

sealed class CasConditionalOperators[RR : Primitive](col: AbstractColumn[RR]) {

  @implicitNotFound("Only types with associate primitives can be used.")
  final def eqs(value: RR): CompareAndSet.Condition = {
    new CompareAndSet.Condition(QueryBuilder.Where.eqs(col.name, implicitly[Primitive[RR]].asCql(value)))
  }
}

sealed trait CasConditionsImplicits extends LowPriorityImplicits {
  /*
    @implicitNotFound(msg = "Compare-and-set queries can only be applied to non indexed primitive columns.")
    implicit final def columnToCasCompareColumn[RR : Primitive](col: AbstractColumn[RR]): CasConditionalOperators[RR] = {
      new CasConditionalOperators[RR](col)
  }
  */

}

private[phantom] trait ImplicitMechanism extends LowPriorityImplicits
  with ModifyMechanism
  with IndexRestrictions
  with CasConditionsImplicits
