package io.github.unknown030405.minesweeper

sealed trait CellView

object CellView {
  case object Mine    extends CellView
  case object Hidden  extends CellView
  case object Flagged extends CellView
  case object Empty   extends CellView

  case class Open(number: Int) extends CellView
}
