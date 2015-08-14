package com.websudos.phantom.db

import com.websudos.phantom.connectors.ContactPoint
import com.websudos.phantom.tables.{Recipes, JsonTable, EnumTable, BasicTable}


private[this] object DefaultKeysapce {
  lazy val local = ContactPoint.local.keySpace("phantom")
}

class TestDatabase extends DatabaseImpl(DefaultKeysapce.local) {
  object basicTable extends BasicTable with connector.Connector
  object enumTable extends EnumTable with connector.Connector
  object jsonTable extends JsonTable with connector.Connector
  object recipes extends Recipes with connector.Connector
}


class ValueInitDatabase extends DatabaseImpl(DefaultKeysapce.local) {
  val basicTable = new BasicTable with connector.Connector
  val enumTable = new EnumTable with connector.Connector
  val jsonTable = new JsonTable with connector.Connector
  val recipes = new Recipes with connector.Connector
}