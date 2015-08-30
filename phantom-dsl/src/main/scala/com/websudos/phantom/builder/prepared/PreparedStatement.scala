package com.websudos.phantom.builder.prepared

import com.websudos.phantom.builder.clauses.WhereClause

sealed trait ParametricNode
final class ParametricValue[PVT, P <: ParametricNode] extends ParametricNode
final class PNil extends ParametricNode

/**
 * Condition resulting from creating predicate with a parameter.
 * @tparam V Type of parameter.
 */
trait ParametricCondition[V] {
  def parametrize(value: V): WhereClause.Condition
}

/**
 * Prepared statement
 */
class PreparedStatement[P <: ParametricNode](args: Any*) {

  type **[PV, PN <: ParametricNode] = ParametricValue[PV, PN]

  def where[V](pc: ParametricCondition[V]): PreparedStatement[V ** P] = {
    new PreparedStatement[V ** P](args)
  }

  /**
   * Set PS parameters. This function is applicable to single parametric value.
   * @return Returns statement ready to execute.
   */
  def withParams[V1](v1: V1)(implicit ev: P =:= V1 ** PNil): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](v1)
  }

  /**
   * Set PS parameters. This function is applicable to four parametric values.
   * @return Returns statement ready to execute.
   */
  def withParams[V1, V2](v1: V1, v2: V2)(implicit ev: P =:= V2 ** (V1 ** PNil)): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](v1, v2)
  }

  /**
   * Set PS parameters. This function is applicable to four parametric values.
   * @return Returns statement ready to execute.
   */
  def withParams[V1, V2, V3](v1: V1, v2: V2, v3: V3)(implicit ev: P =:= V3 ** (V2 ** (V1 ** PNil))): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](v1, v2, v3)
  }

  /**
   * Set PS parameters. This function is applicable to four parametric values.
   * @return Returns statement ready to execute.
   */
  def withParams[V1, V2, V3, V4](v1: V1, v2: V2, v3: V3, v4: V4)(implicit ev: P =:= V4 ** (V3 ** (V2 ** (V1 ** PNil)))): PreparedStatement[PNil] = {
    new PreparedStatement[PNil](v1, v2, v3, v4)
  }

}
