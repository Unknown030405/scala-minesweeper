package io.github.unknown030405.minesweeper

sealed trait GameStatus

object GameStatus {
  case object Playing extends GameStatus

  case object Won extends GameStatus

  case object Lost extends GameStatus
}
