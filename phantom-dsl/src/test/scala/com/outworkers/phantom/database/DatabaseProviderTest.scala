/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.database

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.TestDatabase

class DatabaseProviderTest extends PhantomSuite {
  it should "maintain a reference to the singleton object using the shorthand syntax" in {
    (db eq TestDatabase) shouldEqual true
  }

  it should "maintain a reference to the singleton object injected" in {
    (database eq TestDatabase) shouldEqual true
  }
}
