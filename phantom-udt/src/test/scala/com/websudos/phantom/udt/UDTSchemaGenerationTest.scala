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

import com.websudos.util.testing._

class UDTSchemaGenerationTest extends TestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    // Await.ready(TestFields.udtExecute(), 2.seconds)
  }

  ignore should "generate the schema of an UDT during table creation" in {
    TestFields.udtExecute().successful {
      res => {
        Console.println(res.toString)
      }
    }
  }
}
