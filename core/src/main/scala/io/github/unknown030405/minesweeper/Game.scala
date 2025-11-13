package io.github.unknown030405.minesweeper

import scala.annotation.tailrec
import scala.util.Random

/** An immutable, functional representation of a Minesweeper game state.
  *
  * The game encapsulates:
  *   - the hidden board (mine positions),
  *   - the set of revealed cells,
  *   - the set of flagged cells,
  *   - and the current game status (playing, won, lost).
  *
  * All operations are pure: they return a new [[Game]] instance without modifying the current one. The internal board
  * state is fully encapsulated — no mine positions are exposed to the outside world.
  *
  * @param status
  *   current state of the game: [[GameStatus.Playing]], [[GameStatus.Won]], or [[GameStatus.Lost]]
  * @param board
  *   the immutable game board (hidden from external access)
  * @param revealed
  *   set of positions that have been revealed by the player
  * @param flagged
  *   set of positions marked with a flag by the player
  */
case class Game private (
    status: GameStatus,
    private val board: Board,
    private val revealed: Set[Position],
    private val flagged: Set[Position]
) {

  /** Attempts to reveal a cell at the given position.
    *
    * This method implements the core game logic:
    *   - If the position is invalid, already revealed, or flagged, the action is ignored.
    *   - If the cell contains a mine, the game transitions to the [[GameStatus.Lost]] state.
    *   - If the cell is empty (zero adjacent mines), a flood-fill reveals all connected empty cells.
    *   - If all safe cells are revealed, the game transitions to the [[GameStatus.Won]] state.
    *
    * The result is encoded as a [[RevealResult]] to precisely communicate what happened.
    *
    * @param pos
    *   the position to reveal
    * @return
    *   a [[RevealResult]] describing the outcome of the action
    */
  def reveal(pos: Position): RevealResult = {
    if (revealed.contains(pos)) {
      RevealResult.Ignored(RevealResult.IgnoreReason.AlreadyRevealed, copy())
    } else if (!canReveal(pos)) {
      RevealResult.Ignored(RevealResult.IgnoreReason.InvalidPosition, copy())
    } else if (flagged.contains(pos)) {
      RevealResult.Ignored(RevealResult.IgnoreReason.Flagged, copy())
    } else {
      (board.getCell(pos) match { // return None if it is mine or if it is out of bounds
        case None if board.isMine(pos) => (loseGame, Some(CellView.Mine)) // Mine
        case None => // Out of bounds shouldn't ever happen, but will be here for safety
          (copy(), None)
        case Some(NonNegativeInt.zero) => (revealNeighbours(pos), Some(CellView.Empty))
        case Some(value)               => (copy(revealed = revealed + pos), Some(CellView.Open(value.value)))
      }) match {
        case (result, Some(cellView))
            if result.revealed.size ==
              (result.board.size.value * result.board.size.value) - result.board.totalMinesNum =>
          RevealResult.Opened(cellView, winGame)
        case (result, Some(cellView)) if cellView == CellView.Mine =>
          RevealResult.Exploded(pos, result)
        case (result, Some(cellView)) => RevealResult.Opened(cellView, result)
        case (result, None)           => RevealResult.Ignored(RevealResult.IgnoreReason.InvalidPosition, result)
      }
    }
  }

  /** Recursively reveals all connected empty cells (flood-fill) starting from the given position.
    *
    * This method implements the classic Minesweeper "chording" behavior: when a cell with zero adjacent mines is
    * revealed, all its neighbors are automatically revealed. The process continues recursively for any newly revealed
    * zero cells.
    *
    * @param pos
    *   the starting position (must be a zero cell)
    * @return
    *   a new [[Game]] with all connected empty cells revealed
    */
  private def revealNeighbours(pos: Position): Game = {
    @tailrec
    def helper(revealed: Set[Position], queue: List[Position]): Set[Position] = {
      queue match {
        case Nil                                                               => revealed
        case head :: tail if board.getCell(head).contains(NonNegativeInt.zero) =>
          helper(
            revealed + head,
            board.getNeighbours(head) ++ tail
          )
        case head :: tail if board.getCell(head).isDefined => helper(revealed + head, tail)
        case _ :: tail                                     => helper(revealed, tail)
      }
    }

    copy(revealed = helper(revealed, board.getNeighbours(pos)) + pos)
  }

  /** Lazily computed set of all mine positions on the board.
    *
    * Used only when revealing the entire board upon game over (win or loss).
    */
  private lazy val minesRevealed: Set[Position] = (for {
    r <- 0 until board.size.value
    c <- 0 until board.size.value
    x <- NonNegativeInt.fromInt(r)
    y <- NonNegativeInt.fromInt(c)
    position = Position(x, y)
    if board.isMine(position)
  } yield position).toSet

  /** Transitions the game to the "lost" state.
    *
    * All mines are revealed, flags are cleared, and the status is set to [[GameStatus.Lost]]. This method is typically
    * called when a mine is revealed.
    *
    * @return
    *   a new [[Game]] in the lost state
    */
  def loseGame: Game = {
    copy(status = GameStatus.Lost, revealed = revealed ++ minesRevealed, flagged = Set.empty)
  }

  /** Transitions the game to the "won" state.
    *
    * All mines are revealed (for verification), and the status is set to [[GameStatus.Won]].
    *
    * @return
    *   a new [[Game]] in the won state
    */
  private def winGame: Game = {
    copy(status = GameStatus.Won, revealed = revealed ++ minesRevealed)
  }

  /** Toggles a flag on the given position.
    *
    * A flag can only be placed on a hidden, unflagged cell while the game is active. This operation does not affect the
    * game status (winning condition is checked only on reveal).
    *
    * @param pos
    *   the position to flag/unflag
    * @return
    *   a new [[Game]] with updated flags
    */
  def toggleFlag(pos: Position): Game = {
    if (canToggleFlag(pos)) {
      val newFlags = flagged + pos
      copy(flagged = newFlags)
    } else {
      copy()
    }
  }

  /** Returns the visual representation of a cell as seen by the player.
    *
    * This method respects game rules:
    *   - flagged cells appear as [[CellView.Flagged]],
    *   - revealed cells show their true content ([[CellView.Mine]], [[CellView.Empty]], or [[CellView.Open]]),
    *   - hidden cells appear as [[CellView.Hidden]].
    *
    * Note: mines are only visible after the game is lost or won.
    *
    * @param pos
    *   the position to inspect
    * @return
    *   the player-visible cell state, or `None` if the position is out of bounds
    */
  def cellView(pos: Position): Option[CellView] = {
    if (flagged.contains(pos)) {
      Some(CellView.Flagged)
    } else if (revealed.contains(pos)) {
      board.getCell(pos) match {
        case None if board.isMine(pos) => Some(CellView.Mine)
        case None                      => None
        case Some(NonNegativeInt.zero) => Some(CellView.Empty)
        case Some(value)               => Some(CellView.Open(value.value))
      }
    } else {
      Some(CellView.Hidden)
    }
  }

  /** Checks whether a cell can be revealed.
    *
    * A cell is revealable if it is:
    *   - within board bounds,
    *   - not already revealed,
    *   - not flagged,
    *   - and the game is still active ([[GameStatus.Playing]]).
    *
    * @param pos
    *   the position to check
    * @return
    *   `true` if the cell can be revealed, `false` otherwise
    */
  def canReveal(pos: Position): Boolean =
    !revealed.contains(pos) && !flagged.contains(pos) && board.isValidPosition(pos) && status == GameStatus.Playing

  /** Checks whether a flag can be toggled on a cell.
    *
    * A flag can be placed if the cell is:
    *   - within board bounds,
    *   - not already revealed,
    *   - and the game is still active ([[GameStatus.Playing]]).
    *
    * @param pos
    *   the position to check
    * @return
    *   `true` if a flag can be toggled, `false` otherwise
    */
  def canToggleFlag(pos: Position): Boolean =
    !revealed.contains(pos) && board.isValidPosition(pos) && status == GameStatus.Playing

  /** Returns the total number of mines on the board.
    *
    * @return
    *   the mine count as a regular `Int` (for display purposes)
    */
  def totalMines: Int = board.totalMinesNum

  /** Returns the number of flags currently placed by the player.
    *
    * @return
    *   the flag count as a regular `Int` (for display purposes)
    */
  def flaggedCount: Int = flagged.size
}

/** Companion object for [[Game]], providing factory methods.
  */
object Game {

  /** Minimum allowed board size (inclusive).
    */
  val MIN_FIELD_SIZE: NonNegativeInt = NonNegativeInt.three

  /** Creates a new Minesweeper game with random mine placement.
    *
    * The number of mines is determined by the given [[Difficulty]] level. Note: this implementation does NOT guarantee
    * that the first revealed cell is safe.
    *
    * @param size
    *   board size (width = height); must be >= [[MIN_FIELD_SIZE]]
    * @param difficulty
    *   difficulty level that determines mine count
    * @param rand
    *   implicit random generator (for testability)
    * @return
    *   `Some(game)` if parameters are valid, `None` otherwise
    */
  def newGame(size: NonNegativeInt, difficulty: Difficulty)(implicit rand: Random): Option[Game] = {
    if (size < MIN_FIELD_SIZE) {
      None
    } else {
      val mines = rand.shuffle(for {
        r         <- 0 until size.value
        c         <- 0 until size.value
        actualRow <- NonNegativeInt.fromInt(r)
        actualCol <- NonNegativeInt.fromInt(c)
      } yield Position(actualRow, actualCol)).take(Difficulty.getMinesNum(difficulty, size).value)
      for {
        board <- Board.create(size, mines)
      } yield Game(GameStatus.Playing, board, Set.empty, Set.empty)
    }
  }
}
