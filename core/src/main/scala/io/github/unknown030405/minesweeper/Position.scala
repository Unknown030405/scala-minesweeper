package io.github.unknown030405.minesweeper

case class Position(row: NonNegativeInt, col: NonNegativeInt) {
  def +(other: Position): Position = Position(row + other.row, col + other.col)
}
