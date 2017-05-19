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
  }
  
  import optionTypes._

  object options {

    /** Import this value to have Iota print the macro generated code
      * to the console during compilation
      */
    implicit val ShowTrees: ShowTrees = null.asInstanceOf[ShowTrees]

    /** Import this value to have Iota print the cached computations
      * during macro expansion
      */
    implicit val ShowCache: ShowCache = null.asInstanceOf[ShowCache]

    /** Import this value to have Iota print aborted instance
      * materialization for [[TList]] and [[KList]] helpers
      */
    implicit val ShowAborts: ShowAborts = null.asInstanceOf[ShowAborts]
  }
}
