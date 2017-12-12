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
package com.outworkers.phantom.macros

package object debug {
  object optionTypes {
    sealed trait ShowTrees
    sealed trait ShowCache
    sealed trait ShowAborts
    sealed trait ShowBoundStatements
    sealed trait ShowCompileLog
    sealed trait ShowAll
  }


  object Options {

    /**
      * Import this value to have Iota print the macro generated code
      * to the console during compilation
      */
    implicit object ShowTrees extends optionTypes.ShowTrees

    /**
      * Shows the internal compilation log for phantom tables.
      * If this is not imported all the c.info level logs are hidden.
      */
    implicit object ShowLog extends optionTypes.ShowCompileLog

    /**
      * Import this value to have Iota print the cached computations
      * during macro expansion
      */
    implicit object ShowCache extends optionTypes.ShowCache

    /**
      * Import this value to have debug print aborted instance
      * materialization for [[com.outworkers.phantom.builder.primitives.Primitive]] and [[DatabaseHelper]] macros.
      */
    implicit object ShowAborts extends optionTypes.ShowAborts

    /**
      * Import this to show the query string debug information for prepared statements.
      */
    implicit object ShowBoundStatements extends optionTypes.ShowBoundStatements
  }

}
