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

package com.websudos.phantom.zookeeper

import org.scalatest.{Matchers, FlatSpec, ParallelTestExecution}

class ZookeeperConnectorTest extends FlatSpec with Matchers with  ParallelTestExecution {

  it should "correctly use the default localhost:2181 connector address if no environment variable has been set" in {
    System.setProperty(TestTable.envString, "")

    TestTable.zkAddress.getHostName shouldEqual "localhost"

    TestTable.zkAddress.getPort shouldEqual 2181

  }

  it should "use the values from the environment variable if they are set" in {
    System.setProperty(TestTable.envString, "localhost:4902")

    TestTable.zkAddress.getHostName shouldEqual "localhost"

    TestTable.zkAddress.getPort shouldEqual 4902
  }

  it should "return the default if the environment property is in invalid format" in {
    System.setProperty(TestTable.envString, "localhost:invalidint")

    TestTable.zkAddress.getHostName shouldEqual "localhost"

    TestTable.zkAddress.getPort shouldEqual 2181
  }

}
