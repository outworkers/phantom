package com.websudos.phantom.builder

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
