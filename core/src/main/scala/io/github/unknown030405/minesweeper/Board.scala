package io.github.unknown030405.minesweeper

/** Immutable representation of a Minesweeper board.
  *
  * The board stores only its size and the set of mine positions. All other information (e.g. number of adjacent mines)
  * is computed on demand.
  *
  * @param size
  *   the width and height of the square board; must be positive
  * @param minePositions
  *   the set of positions containing mines; must be within board bounds
  */
case class Board private (size: NonNegativeInt, private val minePositions: Set[Position]) {

  /** Total number of mines, that are on the field
    */
  val totalMinesNum: Int = minePositions.size

  /** Checks whether the given position contains a mine.
    *
    * This method is package-private to prevent external code (e.g. UI layers) from cheating by inspecting mine
    * positions directly.
    *
    * @param pos
    *   the position to check
    * @return
    *   `true` if the position contains a mine, `false` otherwise
    */
  private[minesweeper] def isMine(pos: Position): Boolean = minePositions.contains(pos)

  /** Returns the content of a cell at the given position, if it is not a mine.
    *
    * For non-mine cells, returns the number of adjacent mines (0 to 8) as a `NonNegativeInt`. For mine cells, returns
    * `None` to indicate that the cell cant have a number (e.g. it is a mine, or it is outside of board's bounds).
    *
    * This method is package-private to enforce game rules: only the game logic should be able to query raw cell
    * contents, and only after a cell is revealed.
    *
    * @param pos
    *   the position to inspect
    * @return
    *   `Some(count)` if the cell is safe, `None` if it contains a mine or if `pos` is outside the board's bounds
    */
  private[minesweeper] def getCell(pos: Position): Option[NonNegativeInt] = {
    if (!isValidPosition(pos) || isMine(pos)) {
      None
    } else {
      Some(getNeighbours(pos).foldLeft(NonNegativeInt.zero) {
        case (acc, neighbour) if isMine(neighbour) => acc + NonNegativeInt.one
        case (acc, _)                              => acc
      })
    }
  }

  /** Returns all valid (in-bounds) neighboring positions of the given cell or empty list if the cell is out pf bounds.
    *
    * Uses 8-directional connectivity (Moore neighborhood). Only positions that lie within `[0, size)` for both row and
    * column are included.
    *
    * Invalid neighbor candidates (e.g. negative coordinates or coordinates >= size) are silently discarded using
    * `NonNegativeInt.fromInt` and `isValidPosition`.
    *
    * @param pos
    *   the central position
    * @return
    *   a list of valid neighboring positions, possibly empty
    */
  def getNeighbours(pos: Position): List[Position] = {
    if (isValidPosition(pos)) {
      val allNeighbours = for {
        dr <- -1 to 1
        dc <- -1 to 1
        if dr != 0 || dc != 0 // исключаем (0,0) — саму клетку
        newRow <- NonNegativeInt.fromInt(pos.row.value + dr)
        newCol <- NonNegativeInt.fromInt(pos.col.value + dc)
      } yield Position(newRow, newCol)
      allNeighbours.filter(isValidPosition).toList
    } else {
      List.empty[Position]
    }
  }

  /** Checks whether a given position lies within the board boundaries.
    *
    * Since `Position` uses `NonNegativeInt` for coordinates, only the upper bound (i.e. `row < size` and `col < size`)
    * needs to be checked.
    *
    * @param pos
    *   the position to validate
    * @return
    *   `true` if the position is inside the board, `false` otherwise
    */
  def isValidPosition(pos: Position): Boolean = pos.row < size && pos.col < size

  /** Checks whether a given flag set covers all the mines
    *
    * This method is package-private to prevent external code (e.g. UI layers) from cheating by inspecting mine
    * positions indirectly.
    *
    * @param flags
    *   the set of flags to validate
    * @return
    *   `true` if sets are equal, `false` otherwise
    */
  private[minesweeper] def checkFlags(flags: Set[Position]): Boolean = flags == minePositions
}

object Board {
  def create(size: NonNegativeInt, mines: Seq[Position]): Option[Board] = {
    val potentialBoard = Board(size, mines.toSet)
    mines.find(potentialBoard.isValidPosition) match {
      case None    => Some(potentialBoard) // All mines in set were valid
      case Some(_) => None                 // Found an invalid mine, so couldn't build Board using this values
    }
  }
}
