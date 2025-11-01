package ru.yandexpraktikum.cardsanimation.model

import ru.yandexpraktikum.cardsanimation.AnimationStep

data class CardSwapAnimationState(
    val isAnimating: Boolean = false,
    val animationStep: AnimationStep = AnimationStep.NONE
)
