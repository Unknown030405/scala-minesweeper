package io.github.unknown030405.minesweeper

sealed trait Difficulty

object Difficulty {
  case object Easy   extends Difficulty
  case object Normal extends Difficulty
  case object Hard   extends Difficulty

  def getMinesNum(diff: Difficulty, size: NonNegativeInt): NonNegativeInt = {
    diff match {
      case Easy   => size / NonNegativeInt.two
      case Normal => size
      case Hard   => size * NonNegativeInt.two
    }
  }

  val values = List(Easy, Normal, Hard)
}
