package io.github.unknown030405.minesweeper.gui

import io.github.unknown030405.minesweeper.{Game, GameStatus, Position, RevealResult}
import scalafx.application.JFXApp3
import scalafx.scene.Scene

import scala.sys.exit

object MinesweeperApp extends JFXApp3 {
  private val sceneSize: Int           = 1000
  implicit val rand: scala.util.Random = scala.util.Random
  private val sheets                   = List(getClass.getResource("/minesweeper.css").toExternalForm)

  override def start(): Unit = {
    GameView.showNewGameDialog(
      onNewGame,
      () => {
        stopApp()
        exit()
      }
    )
  }

  def onNewGame(params: NewGameParams): Unit = {
    Game.newGame(params.size, params.difficulty) match {
      case Some(game) =>
        stage = new JFXApp3.PrimaryStage {
          title = "Minesweeper"
          scene = new Scene(sceneSize, sceneSize) {
            root = GameView.render(GameViewState(
              game,
              onClick,
              onToggleFlag,
              onNewGame,
              flagMode = false
            ))

            stylesheets = sheets
          }
        }
      case None =>
        println("[Error] Couldn't start a game")
        GameView.showError("Couldn't start a game")
        GameView.showNewGameDialog(
          onNewGame,
          () => {
            stopApp()
            exit()
          }
        )
    }
  }

  def onToggleFlag(flag: Boolean, gameViewState: GameViewState): Unit = {
    println(s"$flag")
    stage.scene = new Scene(sceneSize, sceneSize) {
      root = GameView.render(gameViewState.copy(flagMode = !flag))
      stylesheets = sheets
    }
  }

  def onClick(pos: Position, flag: Boolean, gameViewState: GameViewState): Unit = {
    println(s"$flag -> (${pos.row};${pos.col}) ")
    if (flag) {
      stage.scene = new Scene(sceneSize, sceneSize) {
        root = GameView.render(gameViewState.copy(game = gameViewState.game.toggleFlag(pos)))
        stylesheets = sheets
      }
    } else {
      val result = gameViewState.game.reveal(pos)
      result match {
        case RevealResult.Ignored(reason, _) => GameView.showError(reason.toString)
        case RevealResult.Opened(cell, game) => stage.scene = new Scene(sceneSize, sceneSize) {
            root = game.status match {
              case GameStatus.Playing => GameView.render(gameViewState.copy(game = game))
              case GameStatus.Won     => GameView.showWin(gameViewState.copy(game = game))
              case GameStatus.Lost    => GameView.showLost(gameViewState.copy(game = game))
            }
            stylesheets = sheets
          }
        case RevealResult.Exploded(minePos, game) => stage.scene = new Scene(sceneSize, sceneSize) {
            root = GameView.showLost(gameViewState.copy(game = game))
            stylesheets = sheets
          }
      }
    }
  }
}
