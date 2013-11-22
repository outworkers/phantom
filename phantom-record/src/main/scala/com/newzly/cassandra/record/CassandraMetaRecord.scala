package com.newzly.cassandra.record

import scala.collection.JavaConverters._
import net.liftweb.record.{ MetaRecord }

trait CassandraMetaRecord[MyType <: CassandraRecord[MyType]] extends MetaRecord[MyType] {
  self: MyType =>

  // class name has a $ at the end.
  private lazy val _collectionName = {
    getClass.getName.split("\\.").toList.last.replace("$", "") + "s"
  }

  /*
  * Collection names should begin with letters or an underscore and may include
  * numbers; $ is reserved. Collections can be organized in namespaces; these
  * are named groups of collections defined using a dot notation. For example,
  * you could define collections blog.posts and blog.authors, both reside under
  * "blog". Note that this is simply an organizational mechanism for the user
  * -- the collection namespace is flat from the database's perspective.
  * From: http://www.mongodb.org/display/DOCS/Collections
  */
  def fixCollectionName = _collectionName.toLowerCase match {
    case name if (name.contains("$")) => name.replace("$", "")
    case name => name
  }

  /**
   * The name of the database collection.  Override this method if you
   * want to change the collection to something other than the name of
   * the class with an 's' appended to the end.
   */
  def collectionName: String = fixCollectionName

}
