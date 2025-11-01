package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.yandexpraktikum.cardsanimation.AnimationStep
import ru.yandexpraktikum.cardsanimation.Constants
import ru.yandexpraktikum.cardsanimation.model.CardData
import ru.yandexpraktikum.cardsanimation.model.CardSwapAnimationState
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    var currentCards by remember { mutableStateOf(cards) }
    var isRotated by remember { mutableStateOf(false) }

    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var verticalDragOffset by remember { mutableStateOf(0f) }
    var horizontalDragOffset by remember { mutableStateOf(0f) }

    var swipeDirection by remember { mutableStateOf(1f) }
    val scope = rememberCoroutineScope()
    var extraLiftIndex by remember { mutableStateOf(-1) }


    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragEnd = {
                    if (!animationState.isAnimating) {
                        val absX = abs(horizontalDragOffset)
                        val absY = abs(verticalDragOffset)
                        val verticalDominant = absY > absX
                        val horizontalDominant = absX > absY

                        when {
                            verticalDominant && absY > Constants.SWIPE_THRESHOLD -> {
                                isRotated = verticalDragOffset < 0
                            }

                            horizontalDominant && absX > Constants.SWIPE_THRESHOLD -> {
                                swipeDirection = if (horizontalDragOffset > 0) 1f else -1f
                                scope.launch {
                                    animationState = CardSwapAnimationState(true, AnimationStep.MOVE_RIGHT)
                                    delay(250)
                                    animationState = CardSwapAnimationState(true, AnimationStep.MOVE_UP)
                                    delay(250)
                                    animationState = CardSwapAnimationState(true, AnimationStep.RETURN)
                                    delay(250)
                                    currentCards = reorderCards(currentCards)
                                    animationState = CardSwapAnimationState(false, AnimationStep.NONE)
                                    currentCards = reorderCards(currentCards)
                                    extraLiftIndex = 0

                                    delay(150)
                                    extraLiftIndex = -1

                                    animationState = CardSwapAnimationState(false, AnimationStep.NONE)                                }
                            }
                        }
                    }
                    verticalDragOffset = 0f
                    horizontalDragOffset = 0f
                }
            ) { _, dragAmount ->
                horizontalDragOffset += dragAmount.x
                verticalDragOffset += dragAmount.y
            }
        },
        contentAlignment = Alignment.Center
    ) {
        val cardCount = currentCards.size

        currentCards.forEachIndexed { index, cardData ->
            val targetRotation = calculateCardRotation(index, cardCount, isRotated)
            val animatedRotation by animateFloatAsState(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 400),
                label = "rotation"
            )


            val isBottomCard = index == 0
            val isTopLifted = index == extraLiftIndex

            AnimatedCard(
                cardIndex = index,
                targetRotation = animatedRotation,
                cardData = cardData,
                isAnimating = isBottomCard && animationState.isAnimating,
                animationStep = animationState.animationStep,
                swipeDirection = swipeDirection,
                extraLift = if (isTopLifted) -20f else 0f
            )
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}