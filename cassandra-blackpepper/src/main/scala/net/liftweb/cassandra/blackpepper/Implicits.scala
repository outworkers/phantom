package net.liftweb.cassandra.blackpepper

object Implicits {

  implicit def columnToQueryColumn[RR: CassandraPrimitive](col: Column[RR]) =
    new QueryColumn(col)

  implicit def simpleColumnToAssignment[RR: CassandraPrimitive](col: AbstractColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def simpleOptionalColumnToAssignment[RR: CassandraPrimitive](col: OptionalColumn[RR]) = {
    new ModifyColumnOptional[RR](col)
  }

  implicit def enumColumnToAssignment[RR <: Enumeration](col: EnumColumn[RR]) = {
    new ModifyColumn[RR#Value](col)
  }

  implicit def jsonColumnToAssignment[RR: Manifest](col: JsonTypeColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def seqColumnToAssignment[RR: CassandraPrimitive](col: SeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def jsonSeqColumnToAssignment[RR: Manifest](col: JsonTypeSeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def columnIsSeleCassandraTable[T](col: Column[T]): SelectColumn[T] =
    new SelectColumnRequired[T](col)

  implicit def optionalColumnIsSeleCassandraTable[T](col: OptionalColumn[T]): SelectColumn[Option[T]] =
    new SelectColumnOptional[T](col)
}