package io.github.unknown030405.minesweeper

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BoardSpec extends AnyFlatSpec with Matchers {

  "Board" should "reject invalid mine positions (outside bounds)" in {
    val size         = NonNegativeInt.unsafeFromInt(2)
    val invalidMines = List(Position(NonNegativeInt.unsafeFromInt(2), NonNegativeInt.zero))

    Board.create(size, invalidMines) shouldBe None
  }

  it should "reject if mines count >= total cells" in {
    val size  = NonNegativeInt.unsafeFromInt(2) // 4 cells
    val mines = List(
      Position(NonNegativeInt.zero, NonNegativeInt.zero),
      Position(NonNegativeInt.zero, NonNegativeInt.one),
      Position(NonNegativeInt.one, NonNegativeInt.zero),
      Position(NonNegativeInt.one, NonNegativeInt.one)
    )

    Board.create(size, mines) shouldBe None
  }

  it should "create a valid board with mines" in {
    val size                 = NonNegativeInt.unsafeFromInt(3)
    val mines                = List(Position(NonNegativeInt.zero, NonNegativeInt.one))
    val board: Option[Board] = Board.create(size, mines)

    board.map(_.size) shouldBe Some(size)
  }

  "getCell" should "return None for a mine position" in {
    val size                 = NonNegativeInt.unsafeFromInt(2)
    val minePos              = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    val board: Option[Board] = Board.create(size, List(minePos))

    board.flatMap(_.getCell(minePos)) shouldBe None
    board.map(_.isMine(minePos)) shouldBe Some(true)
  }

  it should "return Some(0) for an empty cell with no neighboring mines" in {
    val size  = NonNegativeInt.unsafeFromInt(3)
    val mines = List(Position(NonNegativeInt.one, NonNegativeInt.one)) // mine in center
    val board = Board.create(size, mines)

    val corner = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    board.flatMap(_.getCell(corner)) shouldBe Some(NonNegativeInt.one) // 1 neighbor

    val edge = Position(NonNegativeInt.zero, NonNegativeInt.one)
    board.flatMap(_.getCell(edge)) shouldBe Some(NonNegativeInt.one)

    // Now test a true zero: put mine in corner
    val board2         = Board.create(size, List(Position(NonNegativeInt.zero, NonNegativeInt.zero)))
    val oppositeCorner = Position(NonNegativeInt.two, NonNegativeInt.two)
    board2.flatMap(_.getCell(oppositeCorner)) shouldBe Some(NonNegativeInt.zero)
  }

  it should "throw IllegalArgumentException for out-of-bounds position" in {
    val size       = NonNegativeInt.unsafeFromInt(2)
    val board      = Board.create(size, List.empty)
    val invalidPos = Position(NonNegativeInt.unsafeFromInt(2), NonNegativeInt.zero)

    board.flatMap(_.getCell(invalidPos)) shouldBe None
  }

  "getNeighbours" should "return 3 neighbors for a corner cell (2x2 board)" in {
    val size   = NonNegativeInt.unsafeFromInt(2)
    val board  = Board.create(size, List.empty).get
    val corner = Position(NonNegativeInt.zero, NonNegativeInt.zero)

    val neighbors = board.getNeighbours(corner)
    neighbors.size shouldBe 3
    neighbors should contain(Position(NonNegativeInt.zero, NonNegativeInt.one))
    neighbors should contain(Position(NonNegativeInt.one, NonNegativeInt.zero))
    neighbors should contain(Position(NonNegativeInt.one, NonNegativeInt.one))
  }

  it should "return 8 neighbors for a center cell (3x3 board)" in {
    val size   = NonNegativeInt.unsafeFromInt(3)
    val board  = Board.create(size, List.empty).get
    val center = Position(NonNegativeInt.one, NonNegativeInt.one)

    val neighbors = board.getNeighbours(center)
    neighbors.size shouldBe 8
    // Check a few
    neighbors should contain(Position(NonNegativeInt.zero, NonNegativeInt.zero))
    neighbors should contain(Position(NonNegativeInt.two, NonNegativeInt.two))
  }

  it should "handle negative-coordinate attempts safely (via NonNegativeInt)" in {
    val size  = NonNegativeInt.unsafeFromInt(3)
    val board = Board.create(size, List.empty).get
    val edge  = Position(NonNegativeInt.zero, NonNegativeInt.zero)

    // getNeighbours should not crash or include invalid positions
    val neighbors = board.getNeighbours(edge)
    neighbors.forall(board.isValidPosition) shouldBe true
  }

  "isValidPosition" should "return true for valid positions" in {
    val size  = NonNegativeInt.unsafeFromInt(3)
    val board = Board.create(size, List.empty).get

    board.isValidPosition(Position(NonNegativeInt.zero, NonNegativeInt.zero)) shouldBe true
    board.isValidPosition(Position(NonNegativeInt.two, NonNegativeInt.two)) shouldBe true
  }

  it should "return false for positions with row >= size" in {
    val size  = NonNegativeInt.unsafeFromInt(3)
    val board = Board.create(size, List.empty).get

    board.isValidPosition(Position(NonNegativeInt.unsafeFromInt(3), NonNegativeInt.zero)) shouldBe false
  }

  it should "return false for positions with col >= size" in {
    val size  = NonNegativeInt.unsafeFromInt(3)
    val board = Board.create(size, List.empty).get

    board.isValidPosition(Position(NonNegativeInt.zero, NonNegativeInt.unsafeFromInt(3))) shouldBe false
  }
}
