package com.websudos.phantom.connectors

import com.datastax.driver.core.Session

trait RootConnector {

  implicit def space: KeySpace

  implicit def session: Session
}

