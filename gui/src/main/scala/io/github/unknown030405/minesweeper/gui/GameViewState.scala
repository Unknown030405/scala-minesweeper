package io.github.unknown030405.minesweeper.gui

import io.github.unknown030405.minesweeper.{Game, Position}

case class GameViewState(
    game: Game,
    onFieldClick: (Position, Boolean, GameViewState) => Unit,
    onToggleFlag: (Boolean, GameViewState) => Unit,
    onNewGame: NewGameParams => Unit,
    flagMode: Boolean
)
