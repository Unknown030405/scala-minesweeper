package io.github.unknown030405.minesweeper.gui

import io.github.unknown030405.minesweeper.CellView
import scalafx.scene.control.Button

object CellBox {
  def render(cell: CellView): Button = {
    val txt = cell match {
      case CellView.Hidden    => ""
      case CellView.Empty     => ""
      case CellView.Flagged   => "🚩"
      case CellView.Mine      => "\uD83D\uDCA3"
      case CellView.Open(num) => num.toString
    }

    new Button {
      prefWidth = 40
      prefHeight = 40
      margin = scalafx.geometry.Insets(10)
      styleClass = Seq("cell-button")
      text = txt
    }
  }
}
