package io.github.unknown030405.minesweeper

class NonNegativeInt private (val value: Int) extends AnyVal {
  override def toString: String = value.toString

  def +(other: NonNegativeInt): NonNegativeInt =
    NonNegativeInt(value + other.value)

  def -(other: NonNegativeInt): Option[NonNegativeInt] = {
    if (other.value > value) {
      None
    } else {
      Some(NonNegativeInt(value - other.value))
    }
  }

  def /(other: NonNegativeInt): NonNegativeInt = NonNegativeInt(value / other.value)

  def *(other: NonNegativeInt): NonNegativeInt = NonNegativeInt(value * other.value)

  def <(other: NonNegativeInt): Boolean = value < other.value

  def <=(other: NonNegativeInt): Boolean = value <= other.value
}

object NonNegativeInt {
  def unsafeFromInt(value: Int): NonNegativeInt =
    if (value >= 0) new NonNegativeInt(value)
    else throw new IllegalArgumentException(s"Negative value: $value")

  def fromInt(value: Int): Option[NonNegativeInt] =
    if (value >= 0) Some(new NonNegativeInt(value)) else None

  def apply(value: Int): NonNegativeInt = unsafeFromInt(value)

  val zero: NonNegativeInt  = NonNegativeInt(0)
  val one: NonNegativeInt   = NonNegativeInt(1)
  val two: NonNegativeInt   = NonNegativeInt(2)
  val three: NonNegativeInt = NonNegativeInt(3)
}
