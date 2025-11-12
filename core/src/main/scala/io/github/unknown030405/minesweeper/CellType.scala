package io.github.unknown030405.minesweeper

sealed trait CellType

object CellType {
  case object Mine extends CellType

  case object Hidden extends CellType

  case object Flagged extends CellType

  case object Empty extends CellType

  case class Open(number: Int) extends CellType
}
