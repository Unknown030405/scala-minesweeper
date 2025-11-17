package io.github.unknown030405.minesweeper.gui

import io.github.unknown030405.minesweeper.{Difficulty, NonNegativeInt}

case class NewGameParams(size: NonNegativeInt, difficulty: Difficulty)
