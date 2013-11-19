package net.liftweb.cassandra.blackpepper

object Implicits {

  implicit def columnToQueryColumn[RR: CSPrimitive](col: Column[RR]) =
    new QueryColumn(col)

  implicit def simpleColumnToAssignment[RR: CSPrimitive](col: AbstractColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def simpleOptionalColumnToAssignment[RR: CSPrimitive](col: OptionalColumn[RR]) = {
    new ModifyColumnOptional[RR](col)
  }

  implicit def enumColumnToAssignment[RR <: Enumeration](col: EnumColumn[RR]) = {
    new ModifyColumn[RR#Value](col)
  }

  implicit def jsonColumnToAssignment[RR: Format](col: JsonTypeColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def seqColumnToAssignment[RR: CSPrimitive](col: SeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def jsonSeqColumnToAssignment[RR: Format](col: JsonTypeSeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def columnIsSelectable[T](col: Column[T]): SelectColumn[T] =
    new SelectColumnRequired[T](col)

  implicit def optionalColumnIsSelectable[T](col: OptionalColumn[T]): SelectColumn[Option[T]] =
    new SelectColumnOptional[T](col)
}