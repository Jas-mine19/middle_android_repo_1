package ru.yandexpraktikum.cardsanimation.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData

class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val cardView: CardView
    private val cardImageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)

        cardView = this.getChildAt(0) as CardView
        cardImageView = findViewById(R.id.cardImage)

        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pivotX = w / 2f
        pivotY = h.toFloat()
    }

    fun setCardData(cardData: CardData) {
        cardImageView.setImageResource(cardData.imageResId)
    }

    fun setStackPosition(index: Int) {
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }

    // TODO: [Задание 1] Добавьте методы для анимации
    // Подсказка: используйте ObjectAnimator для плавной анимации
    // TODO: [Задание 1] Добавьте метод для анимации поворота карты (чтобы был плавный эффект раскрытия/закрытия колоды)
    fun animateToRotation(targetRotation: Float, duration: Long = 300) {
        ObjectAnimator.ofFloat(this, "rotation", rotation, targetRotation).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // TODO: [Задание 5, шаг 1] Добавьте метод для анимации перетасовки карт (первым шагом нижняя карта двигается вправо)
    fun moveCardRight(onComplete: (() -> Unit)? = null) {
        val distance = 200f * resources.displayMetrics.density
        val currentRotationRad = Math.toRadians(rotation.toDouble())

        val deltaX = distance * Math.cos(currentRotationRad).toFloat()
        val deltaY = distance * Math.sin(currentRotationRad).toFloat() + 40f

        val animatorX = ObjectAnimator.ofFloat(this, "translationX", translationX, translationX + deltaX)
        val animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, translationY + deltaY)

        animatorX.duration = 300
        animatorY.duration = 300
        animatorX.interpolator = AccelerateDecelerateInterpolator()
        animatorY.interpolator = AccelerateDecelerateInterpolator()

        animatorX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete?.invoke()
            }
        })

        animatorX.start()
        animatorY.start()
    }



    // TODO: [Задание 5, шаг 2] Добавьте метод для анимации выдвижения нижней карты наверх
    fun moveCardToTop(onComplete: (() -> Unit)? = null) {
        val animatorX = ObjectAnimator.ofFloat(this, "translationX", translationX, 0f)
        val animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, -150f)

        animatorX.duration = 300
        animatorY.duration = 300
        animatorX.interpolator = AccelerateDecelerateInterpolator()
        animatorY.interpolator = AccelerateDecelerateInterpolator()

        animatorX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete?.invoke()
            }
        })

        animatorX.start()
        animatorY.start()
    }

    // TODO: [Задание 5, шаг 3] Добавьте анимацию перемещения всей колоды карты в желаемую позицию
    fun adjustToFinalPosition(
        finalRotation: Float,
        finalZOrder: Int,
        onComplete: (() -> Unit)? = null
    ) {
        val animatorX = ObjectAnimator.ofFloat(this, "translationX", translationX, 0f)
        val animatorY = ObjectAnimator.ofFloat(this, "translationY", translationY, 0f)
        val rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", rotation, finalRotation)
        val zAnimator = ObjectAnimator.ofFloat(this, "translationZ", translationZ, finalZOrder.toFloat())

        val duration = 400L
        listOf(animatorX, animatorY, rotationAnimator, zAnimator).forEach {
            it.duration = duration
            it.interpolator = AccelerateDecelerateInterpolator()
        }

        rotationAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete?.invoke()
            }
        })

        animatorX.start()
        animatorY.start()
        rotationAnimator.start()
        zAnimator.start()
    }

    fun bringAboveAll() {
        translationZ = 1000f
        cardView.cardElevation = 16f * resources.displayMetrics.density
    }

}