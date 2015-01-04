/*
 *
 *  * Copyright 2014 websudos ltd.
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

import org.scalatest.{FlatSpec, Matchers}

class TypeDefinitionTest extends FlatSpec with Matchers {

  it should "extract the name of an UDT column" in {
    TestFields.address.name shouldEqual "address"
  }

  it should "extract the name of an UDT sub-field" in {
    TestFields.address.street.name shouldEqual "street"
  }

  it should "extract the name of a non string UDT sub-field" in {
    TestFields.address.postCode.name shouldEqual "postCode"
  }


  it should "correctly serialise a UDT definition into a schema" in {
    TestFields.address.schema() shouldEqual
      s"""CREATE TYPE IF NOT EXISTS address(postCode text, street text, test int);""".stripMargin
  }
}
