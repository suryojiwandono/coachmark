package com.suryojiwandono.coachmark

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat

/**
 * Created by suryo jiwandono on 2/18/2022.
 */

class CoachMarkTriangle : View {
    private var shapeColor = 0
    private var shapeDirection = 0

    constructor(context: Context?) : super(context) {
        stuff(null)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        stuff(attrs)
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        stuff(attrs)
    }

    constructor(
        context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        stuff(attrs)
    }

    private fun stuff(attrs: AttributeSet?) {
        if (attrs != null) {
            val array: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.CoachMarkTriangle)
            val color = array.getColor(
                R.styleable.CoachMarkTriangle_color,
                ContextCompat.getColor(context, android.R.color.black)
            )
            val direction = array.getInt(R.styleable.CoachMarkTriangle_direction, 0)
            setColor(color)
            setDirection(direction)
            array.recycle()
        }
    }

    fun setDirection(direction: Int) {
        shapeDirection = direction
    }

    fun setColor(color: Int) {
        shapeColor = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = (width / 2).toFloat()
        val height = (height / 2).toFloat()
        val path = Path()
        when (shapeDirection) {
            Direction.NORTH -> {
                path.moveTo(0f, (2 * height))
                path.lineTo(width, 0f)
                path.lineTo((2 * width), (2 * height))
                path.lineTo(0f, (2 * height))
            }
            Direction.WEST -> {
                path.moveTo(0f, 0f)
                path.lineTo((2 * width), height)
                path.lineTo(0f, (2 * height))
                path.lineTo(0f, 0f)
            }
            Direction.SOUTH -> {
                path.moveTo(0f, 0f)
                path.lineTo(width, (2 * height))
                path.lineTo((2 * width), 0f)
                path.lineTo(0f, 0f)
            }
            Direction.EAST -> {
                path.moveTo((2 * width), 0f)
                path.lineTo(0f, height)
                path.lineTo((2 * width), (2 * height))
                path.lineTo((2 * width), 0f)
            }
            Direction.TOP_END -> {
                path.moveTo(width, 0f)
                path.lineTo((width * 2), 0f)
                path.lineTo((width * 2), width)
                path.lineTo(width, 0f)
            }
        }
        path.close()
        val p = Paint()
        p.color = shapeColor
        p.isAntiAlias = true
        canvas.drawPath(path, p)
    }

    internal object Direction {
        const val NORTH = 0
        const val SOUTH = 1
        const val EAST = 2
        const val WEST = 3
        const val TOP_END = 4
    }
}