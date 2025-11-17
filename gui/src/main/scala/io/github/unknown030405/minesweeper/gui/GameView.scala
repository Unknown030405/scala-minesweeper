package io.github.unknown030405.minesweeper.gui

import io.github.unknown030405.minesweeper.{CellView, Difficulty, Game, NonNegativeInt, Position}
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.beans.property.IntegerProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Alert, Button, ButtonType, CheckBox, ChoiceBox, Label, TextField}
import scalafx.scene.image.Image
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.scene.text.Font
import scalafx.util.Duration

import scala.annotation.tailrec

object GameView {
  val gameIcon                     = new Image(getClass.getResource("/icon.png").toExternalForm)
  val timeSeconds: IntegerProperty = IntegerProperty(0)
  private val timerLabel: Label    = new Label("Время: 0") {
    margin = Insets(10)
    font = Font.font(26)
  }
  private val timer: Timeline = new Timeline {
    keyFrames = Seq(
      KeyFrame(
        Duration(1000),
        onFinished = _ => {
          timeSeconds.set(timeSeconds.get() + 1)
          timerLabel.setText(s"Время: ${timeSeconds.get()}")
        }
      )
    )
    cycleCount = -1
  }

  @tailrec
  final def showNewGameDialog(startGame: (NewGameParams, Int) => Unit, callback: () => Unit, prevBest: Int): Unit = {
    val sizeField = new TextField() {
      maxWidth = 80
      text = "5"
    }

    val difficultyChoice = new ChoiceBox[Difficulty] {
      items = ObservableBuffer(Difficulty.values: _*)
      value = Difficulty.Normal
    }

    //    val okButton = new Button("Start") {
    //      disable <== isValidSize.not()
    //    }

    val grid = new GridPane {
      hgap = 10
      vgap = 10
      padding = Insets(20, 150, 10, 10)

      add(new Label("Board size (3–25):"), 0, 0)
      add(sizeField, 1, 0)

      add(new Label("Difficulty:"), 0, 1)
      add(difficultyChoice, 1, 1)
    }
    val dialog = new Alert(Alert.AlertType.Confirmation) {
      title = "New Game"
      contentText = ""
      dialogPane().setContent(grid) // new DialogPane { children = grid }
      buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    }

    val result = dialog.showAndWait()

    result match {
      case Some(ButtonType.OK) =>
        (for {
          size <- sizeField.text.value.toIntOption.flatMap(NonNegativeInt.fromInt)
          difficulty = difficultyChoice.value.value
          if size.value >= Game.MIN_FIELD_SIZE.value && size.value <= 25
        } yield NewGameParams(size, difficulty)) match {
          case Some(params) =>
            println(s"${params.size} - ${params.difficulty}")
            startGame(params, prevBest)
            timer.play()
            println("new game started")
          case None =>
            showError("Invalid size. Please enter a number between 3 and 25")
            showNewGameDialog(startGame, () => callback(), prevBest)
        }
      case _ =>
        println("other Button was pressed")
        callback()
    }
  }

  def showLost(gameViewState: GameViewState): VBox = {
    timer.stop()
    timeSeconds.value = 0
    val alert = new Alert(Alert.AlertType.Information) {
      title = "Game Over"
      contentText = "Sorry, you've exploded"
    }

    alert.show()
    render(gameViewState)
  }

  def showWin(gameViewState: GameViewState): VBox = {
    timer.stop()
    val score: Int = timeSeconds.get() * gameViewState.game.getSize.value * gameViewState.game.totalMines.value
    timeSeconds.value = 0
    val alert = new Alert(Alert.AlertType.Information) {
      title = "Game Over"
      contentText = s"Hooray! You've won\nYour score is: $score"
    }
    alert.show()
    render(gameViewState.copy(bestTime = if (gameViewState.bestTime > score) {
      gameViewState.bestTime
    } else {
      score
    }))
  }

  def showError(message: String): Unit = {
    val alert = new Alert(Alert.AlertType.Error) {
      title = "Input Error"
      contentText = message
    }
    alert.showAndWait()
  }

  def render(gameViewState: GameViewState): VBox = {
    val topBar: HBox = new HBox {
      alignment = Pos.Center
      padding = Insets(10)
      children = List(
        new Button("New Game") {
          minWidth = 160
          onAction = _ => showNewGameDialog(gameViewState.onNewGame, () => (), gameViewState.bestTime)
          margin = Insets(10)
        },
        new Label(s"Мины: ${gameViewState.game.totalMines}") {
          margin = Insets(10)
          font = Font.font(26)
        },
        new CheckBox("🚩") {
          selected = gameViewState.flagMode
          onAction = _ => gameViewState.onToggleFlag(gameViewState.flagMode, gameViewState)
          margin = Insets(10)
          font = Font.font(26)
        }
      )
    }

    val field: List[List[Option[CellView]]] =
      List.tabulate(gameViewState.game.getSize.value, gameViewState.game.getSize.value) { case (x, y) =>
        (for {
          xPos <- NonNegativeInt.fromInt(x)
          yPos <- NonNegativeInt.fromInt(y)
        } yield gameViewState.game.cellView(Position(xPos, yPos))).flatten
      }

    val boardGrid: GridPane = new GridPane {
      hgap = 2
      vgap = 2
      alignment = Pos.Center
      field.zipWithIndex.map { case (lst, row) =>
        lst.zipWithIndex.map { case (cell, col) =>
          add(
            new Button() {
              styleClass = List(s"${cell.getOrElse(CellView.Hidden)}", "button")
              text = cell match {
                case Some(value) => value match {
                    case CellView.Mine         => "\uD83D\uDCA3"
                    case CellView.Hidden       => ""
                    case CellView.Flagged      => "🚩"
                    case CellView.Empty        => ""
                    case CellView.Open(number) => number.toString
                  }
                case None => ""
              }
              onAction = _ => { // TODO make use of RMB/LMB
                println(s"$cell")
                gameViewState.onFieldClick(
                  Position(NonNegativeInt.unsafeFromInt(row), NonNegativeInt.unsafeFromInt(col)),
                  gameViewState.flagMode,
                  gameViewState
                )
              }
            },
            row,
            col
          )
        }
      }
    }
    val bottomBar = new HBox {
      children = List(
        new Label(s"Лучшее: ${if (gameViewState.bestTime == 0) "--" else gameViewState.bestTime.toString}") {
          margin = Insets(10)
          font = Font.font(26)
        },
        timerLabel,
        new Label(s"Флаги: ${gameViewState.game.flaggedCount}") {
          margin = Insets(10)
          font = Font.font(26)
        }
      )
      alignment = Pos.Center
      padding = Insets(10)
    }

    new VBox(10) {
      children = List(topBar, boardGrid, bottomBar)
      alignment = Pos.Center
    }
  }

}
