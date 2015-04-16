package com.websudos.phantom.exceptions

case class InvalidPrimaryKeyException(msg: String = "You need to define at least one PartitionKey for the schema") extends RuntimeException(msg)

case class InvalidTableException(msg: String) extends RuntimeException(msg)
