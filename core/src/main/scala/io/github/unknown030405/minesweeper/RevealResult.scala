package io.github.unknown030405.minesweeper

sealed trait RevealResult
object RevealResult {
  case class Ignored(reason: IgnoreReason, game: Game) extends RevealResult
  case class Opened(cell: CellView, game: Game) extends RevealResult
  case class Exploded(minePos: Position, finalGame: Game) extends RevealResult

  sealed trait IgnoreReason
  object IgnoreReason {
    case object AlreadyRevealed extends IgnoreReason
    case object Flagged extends IgnoreReason
    case object InvalidPosition extends IgnoreReason
  }
}