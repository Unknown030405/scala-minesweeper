package io.github.unknown030405.minesweeper

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameSpec extends AnyFlatSpec with Matchers {

  private val size2 = NonNegativeInt.unsafeFromInt(2)

  "Game" should "reveal a mine and transition to Lost state" in {
    val mines = Set(Position(NonNegativeInt.zero, NonNegativeInt.zero))
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val result = game.reveal(Position(NonNegativeInt.zero, NonNegativeInt.zero))

    result match {
      case RevealResult.Exploded(_, finalGame) =>
        finalGame.status shouldBe GameStatus.Lost
        finalGame.cellView(Position(NonNegativeInt.zero, NonNegativeInt.zero)) shouldBe Some(CellView.Mine)
      case _ =>
        fail("Expected Exploded result")
    }
  }

  it should "reveal a number cell and stay in Playing state" in {
    val mines = Set(Position(NonNegativeInt.zero, NonNegativeInt.zero))
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val pos    = Position(NonNegativeInt.zero, NonNegativeInt.one)
    val result = game.reveal(pos)

    result match {
      case RevealResult.Opened(cellView, newGame) =>
        newGame.status shouldBe GameStatus.Playing
        cellView shouldBe CellView.Open(1)
        newGame.cellView(pos) shouldBe Some(CellView.Open(1))
      case _ =>
        fail("Expected Opened result")
    }
  }

  it should "win the game when all safe cells are revealed" in {
    val minePos = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    val mines   = Set(minePos)
    val board   = Board.create(size2, mines).get
    var game    = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val safeCells = List(
      Position(NonNegativeInt.zero, NonNegativeInt.one),
      Position(NonNegativeInt.one, NonNegativeInt.zero),
      Position(NonNegativeInt.one, NonNegativeInt.one)
    )

    // Открываем первые две клетки
    for (pos <- safeCells.take(2)) {
      game = game.reveal(pos) match {
        case RevealResult.Opened(_, g) => g
        case _                         => fail(s"Failed to open $pos")
      }
    }

    // Последняя клетка должна дать победу
    val lastResult = game.reveal(safeCells(2))
    lastResult match {
      case RevealResult.Opened(_, finalGame) =>
        finalGame.status shouldBe GameStatus.Won
        finalGame.cellView(minePos) shouldBe Some(CellView.Mine)
      case _ =>
        fail("Expected win on last reveal")
    }
  }

  it should "ignore reveal on already revealed cell" in {
    val mines = Set.empty[Position]
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val pos         = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    val firstResult = game.reveal(pos)
    firstResult match {
      case RevealResult.Opened(_, revealedGame) =>
        val secondResult = revealedGame.reveal(pos)
        secondResult match {
          case RevealResult.Ignored(_, unchangedGame) =>
            unchangedGame.status shouldBe GameStatus.Won
            unchangedGame.cellView(pos) shouldBe revealedGame.cellView(pos)
          case _ =>
            fail("Expected Ignored on second reveal")
        }
      case _ =>
        fail("Failed to open first cell")
    }
  }

  "toggleFlag" should "show Flagged cell view" in {
    val mines = Set.empty[Position]
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val pos         = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    val flaggedGame = game.toggleFlag(pos)

    flaggedGame.cellView(pos) shouldBe Some(CellView.Flagged)
  }

  it should "not allow flagging a revealed cell" in {
    val mines = Set[Position](Position(NonNegativeInt.one, NonNegativeInt.one))
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val pos          = Position(NonNegativeInt.zero, NonNegativeInt.zero)
    val revealedGame = game.reveal(pos) match {
      case RevealResult.Opened(_, g) => g
      case _                         => fail("Failed to reveal")
    }

    val flaggedGame = revealedGame.toggleFlag(pos)
    flaggedGame.cellView(pos) shouldBe Some(CellView.Open(1)) // остаётся открытой
  }

  it should "not allow actions after game is over" in {
    val mines = Set(Position(NonNegativeInt.zero, NonNegativeInt.zero))
    val board = Board.create(size2, mines).get
    val game  = Game(GameStatus.Playing, board, Set.empty, Set.empty)

    val lostGame = game.reveal(Position(NonNegativeInt.zero, NonNegativeInt.zero)) match {
      case RevealResult.Exploded(_, g) => g
      case _                           => fail("Expected loss")
    }

    // Попытка открыть другую клетку
    val pos       = Position(NonNegativeInt.one, NonNegativeInt.one)
    val afterLoss = lostGame.reveal(pos)
    afterLoss match {
      case RevealResult.Ignored(_, g) => g shouldBe lostGame
      case _                          => fail("Expected ignored action after loss")
    }

    // Попытка поставить флаг
    val flaggedAfterLoss = lostGame.toggleFlag(pos)
    flaggedAfterLoss shouldBe lostGame
  }
}
