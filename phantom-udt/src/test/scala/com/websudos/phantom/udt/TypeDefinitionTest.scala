/*
 *
 *  * Copyright 2014 newzly ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.udt

import com.websudos.phantom.testing.BaseTest

class TypeDefinitionTest extends BaseTest {
  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  it should "correctly serialise a UDT definition into a schema" in {
    TestFields.address.schema() shouldEqual
      s"""
        |CREATE TYPE ${Connector.keySpace}.address (
        |   postCode text,
        |   street text,
        |   test int
        |);
      """.stripMargin
  }
}
