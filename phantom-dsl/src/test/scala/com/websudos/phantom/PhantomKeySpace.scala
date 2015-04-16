package com.websudos.phantom

import com.websudos.phantom.connectors.{KeySpace, CassandraConnector}

trait PhantomKeySpace {

  implicit val keySpace: KeySpace = KeySpace("phantom")

}
