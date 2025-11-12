package io.github.unknown030405.minesweeper

import scala.annotation.tailrec
import scala.util.Random

case class Game private(
                         status: GameStatus,
                         private val board: Board,
                         private val revealed: Set[Position],
                         private val flagged: Set[Position]
                       ) {

  def reveal(pos: Position): (Game, Option[CellType]) = {
    if (revealed.contains(pos)) {
      (copy(), None)
    }
    else {
      board.getCell(pos) match { // return None if it is mine or if it is out of bounds
        case None if board.isMine(pos) => (looseGame, Some(CellType.Mine)) // Mine
        case None => (copy(), None) // Out of bounds
        case Some(NonNegativeInt.zero) =>
          (revealNeighbours(pos), Some(CellType.Empty))
        case Some(value) => (copy(revealed = revealed + pos), Some(CellType.Open(value.value)))
      }
    }
  }

  private def revealNeighbours(pos: Position): Game = {
    @tailrec
    def helper(revealed: Set[Position], queue: List[Position]): Set[Position] = {
      queue match {
        case Nil => revealed
        case head :: tail if board.getCell(head).contains(NonNegativeInt.zero) => helper(
          revealed + head,
          board.getNeighbours(head) ++ tail)
        case head :: tail if board.getCell(head).isDefined => helper(revealed + head, tail)
        case _ :: tail => helper(revealed, tail)
      }
    }

    copy(revealed = helper(revealed, board.getNeighbours(pos)) + pos)
  }

  def looseGame: Game = {
    val allRevealed: Set[Position] = (for {
      r <- 0 until board.size.value
      c <- 0 until board.size.value
      x <- NonNegativeInt.fromInt(r)
      y <- NonNegativeInt.fromInt(c)
    } yield Position(x, y)).toSet
    copy(status = GameStatus.Lost, revealed = allRevealed, flagged = Set.empty)
  }

  def toggleFlag(pos: Position): Game = {
    if (canToggleFlag(pos)) {
      copy(flagged = flagged + pos)
    } else {
      copy()
    }
  }

  def cellView(pos: Position): Option[CellType] = {
    if (revealed.contains(pos)) {
      board.getCell(pos) match {
        case None if board.isMine(pos) => Some(CellType.Mine)
        case None => None
        case Some(NonNegativeInt.zero) => Some(CellType.Empty)
        case Some(value) => Some(CellType.Open(value.value))
      }
    } else {
      Some(CellType.Hidden)
    }
  }

  def canReveal(pos: Position): Boolean =
    !revealed.contains(pos) && !flagged.contains(pos) && board.isValidPosition(pos)

  def canToggleFlag(pos: Position): Boolean = !revealed.contains(pos) && board.isValidPosition(pos)

  def totalMines: Int = board.totalMinesNum

  def flaggedCount: Int = flagged.size
}

object Game {
  val MIN_FIELD_SIZE: Int = 3

  def newGame(size: Int, difficulty: Difficulty)(implicit rand: Random): Option[Game] = {
    NonNegativeInt.fromInt(size) match {
      case None => None
      case Some(sz) if sz.value < MIN_FIELD_SIZE => None
      case Some(sz) =>
        val mines = rand.shuffle(for {
          r <- 0 until sz.value
          c <- 0 until sz.value
          actualRow <- NonNegativeInt.fromInt(r)
          actualCol <- NonNegativeInt.fromInt(c)
        } yield Position(actualRow, actualCol)).take(Difficulty.getMinesNum(difficulty, sz).value)
        for {
          board <- Board.create(sz, mines)
        } yield Game(GameStatus.Playing, board, Set.empty, Set.empty)
    }
  }
}
