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
package com.outworkers.phantom.builder.primitives

import java.util

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.{CodecRegistry, DataType, ProtocolVersion, TupleValue}

/**
  * A tuple type.
  * <p/>
  * A tuple type is a essentially a list of types.
  */
object TupleType {

  /**
    * Creates a "disconnected" tuple type (<b>you should prefer
    * {@link Metadata#newTupleType(DataType...) cluster.getMetadata().newTupleType(...)}
    * whenever possible</b>).
    * <p/>
    * This method is only exposed for situations where you don't have a {@code Cluster}
    * instance available. If you create a type with this method and use it with a
    * {@code Cluster} later, you won't be able to set tuple fields with custom codecs
    * registered against the cluster, or you might get errors if the protocol versions don't
    * match.
    *
    * @param protocolVersion the protocol version to use.
    * @param types           the types for the tuple type.
    * @return the newly created tuple type.
    */
  def of(
    protocolVersion: ProtocolVersion,
    types: DataType*
  ): TupleType = new TupleType(types.toList, protocolVersion)
}

class TupleType(
  val types: List[DataType],
  val protocolVersion: ProtocolVersion
) extends DataType(DataType.Name.TUPLE) {

  /**
    * The (immutable) list of types composing this tuple type.
    *
    * @return the (immutable) list of types composing this tuple type.
    */
  def getComponentTypes: List[DataType] = types

  /**
    * Returns a new empty value for this tuple type.
    *
    * @return an empty (with all component to { @code null}) value for this
    *                                                 user type definition.
    */
  def newValue: TupleValue = new TupleValue(this)

  /**
    * Returns a new value for this tuple type that uses the provided values
    * for the components.
    * <p/>
    * The numbers of values passed to this method must correspond to the
    * number of components in this tuple type. The {@code i}th parameter
    * value will then be assigned to the {@code i}th component of the resulting
    * tuple value.
    *
    * @param values the values to use for the component of the resulting
    *               tuple.
    * @return a new tuple values based on the provided values.
    * @throws IllegalArgumentException if the number of { @code values}
    *                                                           provided does not correspond to the number of components in this tuple
    *                                                           type.
    * @throws InvalidTypeException if any of the provided value is not of
    *                              the correct type for the component.
    */
  def newValue(values: Any*): TupleValue = {
    if (values.length != types.size) {
      throw new IllegalArgumentException(
        String.format(
          "Invalid number of values. Expecting %d but got %d",
          types.size,
          values.length
        )
      )
    }

    val t: TupleValue = newValue
    var i: Int = 0
    while (i < values.length) {
      {
        val dataType: DataType = types(i)
        if (values(i) == null) t.setValue(i, null)
        else t.setValue(
          i,
          codecRegistry.codecFor(dataType, values(i))
            .serialize(values(i), protocolVersion)
        )
      }
      {
        i += 1; i - 1
      }
    }
    t
  }

  def isFrozen: Boolean = true

  /**
    * Return the protocol version that has been used to deserialize
    * this tuple type, or that will be used to serialize it.
    * In most cases this should be the version
    * currently in use by the cluster instance
    * that this tuple type belongs to, as reported by
    * {@link ProtocolOptions#getProtocolVersion()}.
    *
    * @return the protocol version that has been used to deserialize
    *         this tuple type, or that will be used to serialize it.
    */
  private[core] def getProtocolVersion: ProtocolVersion = protocolVersion

  override def hashCode: Int = util.Arrays.hashCode(Array[AnyRef](name, types))

  override def equals(o: Any): Boolean = {
    if (!o.isInstanceOf[TupleType]) false
    val d: TupleType = o.asInstanceOf[TupleType]
    (name eq d.name) && types == d.types
  }

  /**
    * Return {@code true} if this tuple type contains the given tuple type,
    * and {@code false} otherwise.
    * <p/>
    * A tuple type is said to contain another one
    * if the latter has fewer components than the former,
    * but all of them are of the same type.
    * E.g. the type {@code tuple<int, text>}
    * is contained by the type {@code tuple<int, text, double>}.
    * <p/>
    * A contained type can be seen as a "partial" view
    * of a containing type, where the missing components
    * are supposed to be {@code null}.
    *
    * @param other the tuple type to compare against the current one
    * @return { @code true} if this tuple type contains the given tuple type,
    *                 and { @code false} otherwise.
    */
  def contains(other: TupleType): Boolean = {
    if (this == other) {
      true
    } else if (other.types.size > this.types.size) {
      false
    } else {
      types.slice(0, other.types.size) == other.types
    }
  }

  override def toString: String = "frozen<" + asFunctionParameterString + ">"

  override def asFunctionParameterString: String = {
    val sb: StringBuilder = new StringBuilder
    for (tpe <- types) {
      sb.append(if (sb.isEmpty) "tuple<" else ", ")
      sb.append(tpe.asFunctionParameterString)
    }
    sb.append(">").toString
  }
}

