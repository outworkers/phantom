package com.newzly.phantom.column
import java.io._
import scala.reflect.ClassTag
import org.apache.commons.codec.binary.Base64

object Serializer {
  object Marshal {
    import java.io._
    import scala.reflect.ClassTag

    def dump[A](o: A)(implicit t: ClassTag[A]): Array[Byte] = {
      val ba = new ByteArrayOutputStream(512)
      val out = new ObjectOutputStream(ba)
      out.writeObject(t)
      out.writeObject(o)
      out.close()
      ba.toByteArray()
    }

    @throws(classOf[IOException])
    @throws(classOf[ClassCastException])
    @throws(classOf[ClassNotFoundException])
    def load[A](buffer: Array[Byte])(implicit expected: ClassTag[A]): A = {
      val in = new ObjectInputStream(new ByteArrayInputStream(buffer))
      val found = in.readObject.asInstanceOf[ClassTag[_]]
      try {
        found.runtimeClass.asSubclass(expected.runtimeClass)
        in.readObject.asInstanceOf[A]
      } catch {
        case _: ClassCastException =>
          in.close()
          throw new ClassCastException("type mismatch;"+
            "\n found   : "+found+
            "\n required: "+expected)
      }
    }
  }

  def serialize[T](value: T)(implicit c: ClassTag[T]) = new String(Base64.encodeBase64(Marshal.dump(value)))
  def deserialize[A](value: String)(implicit expected: ClassTag[A]) = Marshal.load(Base64.decodeBase64(value.getBytes()))
}
