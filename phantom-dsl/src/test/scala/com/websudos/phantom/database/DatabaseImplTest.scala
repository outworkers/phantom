/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.database

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.outworkers.util.testing._

class DatabaseImplTest extends PhantomSuite {
  val db = new TestDatabase
  val db2 = new ValueInitDatabase

  it should "instantiate a database and collect references to the tables" in {
    db.tables.size shouldEqual 4
  }

  it should "automatically generate the CQL schema and initialise tables " in {
    db.autocreate().future().successful {
      res => {
        res.nonEmpty shouldEqual true
      }
    }
  }

  ignore should "instantiate a database object and collect references to value fields" in {
    db2.tables.foreach(item => info(item.tableName))
    db2.tables.size shouldEqual 4
  }

  ignore should "automatically generate the CQL schema and initialise tables for value tables" in {
    db2.autocreate().future().successful {
      res => {
        res.nonEmpty shouldEqual true
      }
    }
  }
}
