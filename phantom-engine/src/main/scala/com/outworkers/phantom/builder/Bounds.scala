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
package com.outworkers.phantom.builder

sealed trait LimitBound
trait Limited extends LimitBound
trait Unlimited extends LimitBound

sealed trait OrderBound
trait Ordered extends OrderBound
trait Unordered extends OrderBound

sealed trait ConsistencyBound
trait Specified extends ConsistencyBound
trait Unspecified extends ConsistencyBound

sealed trait WhereBound
trait Chainned extends WhereBound
trait Unchainned extends WhereBound