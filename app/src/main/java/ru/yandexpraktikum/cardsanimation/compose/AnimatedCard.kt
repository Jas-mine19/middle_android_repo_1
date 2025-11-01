package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.AnimationStep
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float,
    isAnimating: Boolean = false,
    animationStep: AnimationStep = AnimationStep.NONE,
    swipeDirection: Float = 1f,
    extraLift: Float = 0f
) {
    // TODO: [Задание 1] Добавьте анимацию поворота карты
    // Подсказка: используйте animateFloatAsState для плавной анимации
    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 400),
        label = "rotationZ"
    )

    val animatedTranslationX by animateFloatAsState(
        targetValue = if (isAnimating) {
            when (animationStep) {
                AnimationStep.MOVE_RIGHT -> 140f * swipeDirection
                AnimationStep.MOVE_UP -> 60f * swipeDirection
                else -> 0f
            }
        } else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "translationX"
    )

    val animatedTranslationY by animateFloatAsState(
        targetValue = if (isAnimating) {
            when (animationStep) {
                AnimationStep.MOVE_RIGHT -> 40f + extraLift
                AnimationStep.MOVE_UP -> -100f + extraLift
                AnimationStep.RETURN -> -10f + extraLift
                else -> extraLift
            }
        } else extraLift,
        animationSpec = tween(durationMillis = 350),
        label = "translationY"
    )

    val animatedTilt by animateFloatAsState(
        targetValue = if (isAnimating) {
            when (animationStep) {
                AnimationStep.MOVE_RIGHT -> 8f * swipeDirection
                AnimationStep.MOVE_UP -> -6f * swipeDirection
                else -> 0f
            }
        } else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "tilt"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .graphicsLayer {
                rotationZ = animatedRotation + animatedTilt
                translationX = animatedTranslationX
                translationY = animatedTranslationY
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (4 + cardIndex).dp)
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}