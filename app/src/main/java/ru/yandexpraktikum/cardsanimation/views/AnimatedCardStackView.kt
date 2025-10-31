package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var cardDataList: List<CardData> = emptyList()
    private val cards = mutableListOf<AnimatedCardView>()
    private var isRotated = false

    private var verticalDragOffset = 0f
    private var horizontalDragOffset = 0f

    private val swipeThreshold = 100f

    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    private fun setupCards() {
        clearCards()
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        isRotated = false
        updateCardPositions()
    }

    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    private fun updateCardPositions() {
        val count = cards.size
        cards.forEachIndexed { index, cardView ->
            val baseRotation = if (count > 1) {
                val step = 45f / (count - 1)
                22.5f - index * step
            } else 0f

            val targetRotation = if (isRotated) {
                val step = if (count > 1) 180f / (count - 1) else 0f
                90f - index * step
            } else baseRotation

            val cardW = 100f * resources.displayMetrics.density
            val cardH = 160f * resources.displayMetrics.density
            val x = width / 2f - cardW / 2f
            val y = height / 2f - cardH / 2f

            cardView.x = x
            cardView.y = y
            cardView.pivotX = cardW / 2f
            cardView.pivotY = cardH
//            cardView.rotation = targetRotation
            cardView.animateToRotation(targetRotation, duration = 400)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateCardPositions()
        }
    }

    private var isAnimating = false
    private var animationStep = 0

    private fun startCardSwapAnimation(bottomCard: AnimatedCardView) {
        if (isAnimating) return
        isAnimating = true
        animationStep = 1

        bottomCard.moveCardRight {
            animationStep = 2

            bottomCard.bringAboveAll()
            bottomCard.moveCardToTop {
                animationStep = 3

                bottomCard.adjustToFinalPosition(
                    finalRotation = 0f,
                    finalZOrder = 1000
                ) {
                    cardDataList = reorderCards(cardDataList)
                    setupCards()
                    isAnimating = false
                    animationStep = 0
                }
            }
        }
    }


    // Простая функция перестановки карт
    fun reorderCards(cards: List<CardData>): List<CardData> {
        return cards.drop(1) + cards.first()
    }

    // TODO: [Задание 2] Добавьте обработку жестов
    // Подсказка: Используйте GestureDetector с методом onFling для обработки свайпов

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                horizontalDragOffset -= distanceX
                verticalDragOffset -= distanceY
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                handleDragEnd()
                return true
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimating) return true
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleDragEnd()
        }
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun handleDragEnd() {
        if (isAnimating) {
            resetOffsets(); return
        }

        val absX = kotlin.math.abs(horizontalDragOffset)
        val absY = kotlin.math.abs(verticalDragOffset)

        val verticalDominant = absY > absX
        val horizontalDominant = absX > absY

        when {
            verticalDominant && absY > swipeThreshold -> {
                handleVerticalSwipe(verticalDragOffset)
            }

            horizontalDominant && absX > swipeThreshold -> {
                handleHorizontalSwipe()
            }
        }
        resetOffsets()
    }

    private fun resetOffsets() {
        verticalDragOffset = 0f
        horizontalDragOffset = 0f
    }

    // TODO: [Задание 3] Добавьте обработку вертикальных свайпов (вверх/вниз)
    private fun handleVerticalSwipe(dy: Float) {
        val newState = dy < 0
        if (newState != isRotated) {
            isRotated = newState
            updateCardPositions()
        }
    }


    // TODO: [Задание 4] Добавьте обработку горизонтальных свайпов (влево/вправо)
    private fun handleHorizontalSwipe() {
        val bottomCard = cards.firstOrNull() ?: return
        startCardSwapAnimation(bottomCard)
    }


}