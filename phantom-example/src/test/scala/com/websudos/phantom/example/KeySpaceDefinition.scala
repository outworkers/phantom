package com.websudos.phantom.example

import com.websudos.phantom.connectors.KeySpace

sealed trait KeySpaceDefinition {
  implicit val keySpace = KeySpace("phantom_example")
}
