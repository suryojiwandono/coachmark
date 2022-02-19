package com.suryojiwandono.coachmark

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.airbnb.paris.extensions.style

/**
 * Created by suryo jiwandono on 2/18/2022.
 */

class CoachMarkView(context: Context, builder: CoachMark.Builder?) : FrameLayout(context) {

    private var textIndicator: TextView? = null
    private var textViewTitle: TextView? = null
    private var textDescription: TextView? = null

    private var buttonSkip: Button? = null
    private var buttonNext: Button? = null
    private var buttonPrevious: Button? = null

    private var lastView: View? = null
    private var paintView: Paint? = null

    private var bitmap: Bitmap? = null
    private var viewGroup: ViewGroup? = null

    private var builder: CoachMark.Builder? = null
    private var stepPositionListener: OnStepPositionListener? = null
    private var coachMarkPosition: CoachMarkPosition? = null

    private var isLast = false
    private var isStart = false

    private var spacing = 0
    private var arrowWidth = 0
    private var arrowMargin = 0
    private var highlightLocX = 0
    private var highlightLocY = 0

    private var path: Path? = null
    private var paintArrow: Paint? = null

    init {
        onInit(context, builder)
    }

    private fun onInit(context: Context, builder: CoachMark.Builder? = null) {
        visibility = GONE
        if (isInEditMode) return
        this.builder = builder
        onInitArrow(builder)
        onInitPaint(builder)
        onInitViews(context, builder)
    }

    private fun onInitArrow(builder: CoachMark.Builder?) {
        if (builder == null) return
        spacing =
            if (builder.spacing == 0) 16.dp
            else resources.getDimension(builder.spacing).toInt()
        if (builder.isArrow) {
            arrowMargin = spacing / 3
            arrowWidth = (1.5 * spacing).toInt()
        } else {
            arrowMargin = 0
            arrowWidth = 0
        }

        isClickable = builder.isCancelable
        isFocusable = builder.isCancelable
        if (builder.isCancelable) {
            setOnClickListener { onNextView() }
        }
    }

    private fun onInitPaint(builder: CoachMark.Builder?) {
        setWillNotDraw(false)
        paintView = Paint(Paint.ANTI_ALIAS_FLAG)
        paintView?.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        paintArrow = Paint(Paint.ANTI_ALIAS_FLAG)
        paintArrow?.style = Paint.Style.FILL
        if (builder != null) {
            val bgColor =
                if (builder.backgroundColor == 0) Color.WHITE
                else ContextCompat.getColor(context, builder.backgroundColor)
            paintArrow?.color = bgColor

            val shadowColor =
                if (builder.shadowColor == 0) Color.parseColor("#B0000000")
                else ContextCompat.getColor(context, builder.shadowColor)
            setBackgroundColor(shadowColor)
        }
    }

    private fun onInitViews(context: Context, builder: CoachMark.Builder?) {
        if (builder == null) return
        val layout = LayoutInflater.from(context).inflate(builder.layoutView, this, false)
        if (layout is ViewGroup) {
            viewGroup = layout
            val root = viewGroup?.findViewById<LinearLayoutCompat>(R.id.root)
            textViewTitle = viewGroup?.findViewById(R.id.text_title)
            textDescription = viewGroup?.findViewById(R.id.text_description)
            buttonSkip = viewGroup?.findViewById(R.id.button_skip)
            buttonPrevious = viewGroup?.findViewById(R.id.button_previous)
            buttonNext = viewGroup?.findViewById(R.id.button_next)
            textIndicator = viewGroup?.findViewById(R.id.text_indicator)

            val bgColor =
                if (builder.backgroundColor == 0) Color.WHITE
                else ContextCompat.getColor(context, builder.backgroundColor)
            root?.backgroundColor(bgColor)

            val textTitleColor =
                if (builder.textTitleColor == 0) Color.parseColor("#424242")
                else ContextCompat.getColor(context, builder.textTitleColor)
            val textTitleSize =
                if (builder.textTitleSize == 0) 16f
                else resources.getDimension(builder.textTitleSize)
            textViewTitle?.setTextColor(textTitleColor)
            textViewTitle?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textTitleSize)

            val textDescriptionColor =
                if (builder.textDescriptionColor == 0) Color.parseColor("#424242")
                else ContextCompat.getColor(context, builder.textDescriptionColor)
            val textDescriptionSize =
                if (builder.textDescriptionSize == 0) 14f
                else resources.getDimension(builder.textDescriptionSize)
            textDescription?.setTextColor(textDescriptionColor)
            textDescription?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textDescriptionSize)

            buttonSkip?.text = builder.buttonSkipText
            if (builder.buttonStyle != 0) buttonSkip?.style(builder.buttonStyle)
            else buttonSkip?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textDescriptionSize)
            buttonSkip?.setOnClickListener {
                if (stepPositionListener != null) {
                    if (isStart && builder.isSkip) stepPositionListener?.onComplete()
                }
            }

            buttonPrevious?.text = builder.buttonPreviousText
            if (builder.buttonStyle != 0) buttonPrevious?.style(builder.buttonStyle)
            else buttonPrevious?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textDescriptionSize)
            buttonPrevious?.setOnClickListener {
                if (stepPositionListener != null) {
                    stepPositionListener?.onPrevious()
                }
            }
            buttonNext?.text = builder.buttonNextText
            if (builder.buttonStyle != 0) buttonNext?.style(builder.buttonStyle)
            else buttonNext?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textDescriptionSize)
            buttonNext?.setOnClickListener { onNextView() }
            this.addView(viewGroup)
        } else this.addView(layout)
    }

    private fun onNextView() {
        if (stepPositionListener != null) {
            if (isLast) stepPositionListener?.onComplete()
            else stepPositionListener?.onNext()
        }
    }

    fun setCoachMarkListener(onStepListener: OnStepPositionListener?) {
        this.stepPositionListener = onStepListener
    }

    fun show(
        coachMark: CoachMark?,
        currentPosition: Int,
        coachMarkSize: Int
    ) {
        val view = coachMark?.view
        val title = coachMark?.title
        val description = coachMark?.description
        val coachMarkPosition = coachMark?.coachMarkPosition
        val isBackground = coachMark?.isBackground ?: false
        val location = coachMark?.positions
        val radius = coachMark?.radius ?: 0

        isStart = currentPosition == 0
        isLast = if (coachMarkSize == 0) true else currentPosition == coachMarkSize - 1
        this.coachMarkPosition = coachMarkPosition
        if (bitmap != null) bitmap?.recycle()
        if (title.isNullOrEmpty())
            textViewTitle?.visibility = GONE
        else {
            textViewTitle?.text = title.toHtml()
            textViewTitle?.visibility = VISIBLE
        }
        if (description.isNullOrEmpty())
            textDescription?.visibility = GONE
        else {
            textDescription?.text = description.toHtml()
            textDescription?.visibility = VISIBLE
        }

        buttonSkip?.visibility = if (!isLast && builder?.isSkip == true) VISIBLE else GONE
        buttonPrevious?.visibility = if (!isStart && builder?.isPrevious == true) VISIBLE else GONE

        if (isLast) {
            buttonNext?.text = builder?.buttonFinishText
        } else if (currentPosition < coachMarkSize - 1) {
            buttonNext?.text = builder?.buttonNextText
        }

        if (builder?.isIndicator == true) {
            val value =
                if (coachMarkSize > 1)
                    String.format("%d", (currentPosition + 1))
                        .plus(" ").plus(builder?.delimiterText).plus(" ").plus(coachMarkSize)
                else ""
            textIndicator?.text = value
        } else textIndicator?.visibility = View.GONE

        if (view != null) {
            lastView = view
            bitmap = if (isBackground) {
                val bitmapTemp = view.getBitmap()
                val bigBitmap = Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight, Bitmap.Config.ARGB_8888
                )
                val bigCanvas = Canvas(bigBitmap)
//                bigCanvas.drawColor(Color.WHITE)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.style = Paint.Style.FILL
                paint.color = Color.WHITE
                paint.isAntiAlias = true
                bigCanvas.roundCorners(paint)
                bigCanvas.drawBitmap(bitmapTemp, 0f, 0f, paint)
                bigBitmap
            } else view.getBitmap()

            if (location != null) {
                bitmap = if (radius != 0) bitmap?.croppedBitmap(location, radius)
                else bitmap?.croppedBitmap(location)

                highlightLocX = location[0] - radius
                highlightLocY = location[1] - radius
            } else {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                highlightLocX = location[0]
                highlightLocY = location[1] - CoachMarkDialog.getStatusBarHeight(context)
            }
            this.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (bitmap != null) {
                        val width = bitmap?.width ?: 0
                        val height = bitmap?.height ?: 0
                        positionHighlight(
                            highlightLocX,
                            highlightLocY,
                            highlightLocX + width,
                            highlightLocY + height
                        )
                        this@CoachMarkView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        invalidate()
                    }
                }
            })
        } else {
            lastView = null
            bitmap = null
            highlightLocX = 0
            highlightLocY = 0
            positionCenter()
        }
        this.visibility = VISIBLE
    }

    fun hide() {
        this.visibility = INVISIBLE
    }

    fun close() {
        visibility = GONE
        if (bitmap != null) {
            bitmap?.recycle()
            bitmap = null
        }
        if (lastView != null) {
            lastView = null
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (bitmap != null) {
            bitmap?.recycle()
        }
        bitmap = null
        lastView = null
        paintView = null
    }

    public override fun onDraw(canvas: Canvas) {
        if (bitmap == null || bitmap?.isRecycled == true) return
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, highlightLocX.toFloat(), highlightLocY.toFloat(), paintView)
        if (path != null && viewGroup?.visibility == VISIBLE) {
            canvas.drawPath(path!!, paintArrow!!)
        }
    }

    private fun positionHighlight(
        startHighlightX: Int,
        startHighlightY: Int,
        endHighlightX: Int,
        endHighlightY: Int
    ) {
        if (coachMarkPosition == CoachMarkPosition.CENTER) {
            val widthCenter = this.width / 2
            val heightCenter = this.height / 2
            coachMarkPosition = if (endHighlightY <= heightCenter) CoachMarkPosition.BOTTOM
            else if (startHighlightY >= heightCenter) CoachMarkPosition.TOP
            else if (endHighlightX <= widthCenter) CoachMarkPosition.END
            else if (startHighlightX >= widthCenter) CoachMarkPosition.START
            else {
                if (this.height - endHighlightY > startHighlightY) CoachMarkPosition.BOTTOM
                else CoachMarkPosition.TOP
            }
        }
        val layoutParams: LayoutParams
        when (coachMarkPosition) {
            CoachMarkPosition.END -> {
                val widthExpected = width - endHighlightX - 2 * spacing
                viewGroup?.measure(
                    MeasureSpec.makeMeasureSpec(widthExpected, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val heightViewGroup = viewGroup?.measuredHeight ?: 0
                layoutParams = LayoutParams(
                    widthExpected,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.END
                )
                layoutParams.marginStart = spacing
                layoutParams.marginEnd = spacing
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = spacing
                layoutParams.bottomMargin = 0

                val heightHightlight = endHighlightY - startHighlightY
                val heightDiff = heightHightlight - heightViewGroup

                val expectedTopMargin = startHighlightY + heightDiff / 2
                marginTopBottom(expectedTopMargin, layoutParams, heightViewGroup)
                setViewGroup(layoutParams)
                if (arrowWidth == 0) path = null
                else {
                    val centerHighlightY = (endHighlightY + startHighlightY) / 2
                    val newArrowWidth = newArrowWidth(centerHighlightY, height)
                    if (newArrowWidth == 0) path = null
                    else {
                        path = Path()
                        path?.moveTo(
                            (endHighlightX + arrowMargin).toFloat(),
                            centerHighlightY.toFloat()
                        )
                        path?.lineTo(
                            (endHighlightX + spacing).toFloat(), (
                                    centerHighlightY - arrowWidth / 2).toFloat()
                        )
                        path?.lineTo(
                            (endHighlightX + spacing).toFloat(), (
                                    centerHighlightY + arrowWidth / 2).toFloat()
                        )
                        path?.close()
                    }
                }
            }
            CoachMarkPosition.START -> {
                val widthExpected = startHighlightX - 2 * spacing
                viewGroup?.measure(
                    MeasureSpec.makeMeasureSpec(widthExpected, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val heightViewGroup = viewGroup?.measuredHeight ?: 0
                layoutParams = LayoutParams(
                    widthExpected,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.START
                )
                layoutParams.marginStart = spacing
                layoutParams.marginEnd = spacing
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = spacing
                layoutParams.bottomMargin = 0

                val heightHighlight = endHighlightY - startHighlightY
                val heightDiff = heightHighlight - heightViewGroup

                val expectedTopMargin = startHighlightY + heightDiff / 2
                marginTopBottom(expectedTopMargin, layoutParams, heightViewGroup)
                setViewGroup(layoutParams)
                if (arrowWidth == 0) path = null
                else {
                    val centerHighLightY = (endHighlightY + startHighlightY) / 2
                    val newArrowWidth = newArrowWidth(centerHighLightY, height)
                    if (newArrowWidth == 0) path = null
                    else {
                        path = Path()
                        path?.moveTo(
                            (startHighlightX - arrowMargin).toFloat(),
                            centerHighLightY.toFloat()
                        )
                        path?.lineTo(
                            (startHighlightX - spacing).toFloat(), (
                                    centerHighLightY - arrowWidth / 2).toFloat()
                        )
                        path?.lineTo(
                            (startHighlightX - spacing).toFloat(), (
                                    centerHighLightY + arrowWidth / 2).toFloat()
                        )
                        path?.close()
                    }
                }
            }
            CoachMarkPosition.BOTTOM -> {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.TOP
                )
                layoutParams.marginStart = spacing
                layoutParams.marginEnd = spacing
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = spacing
                layoutParams.topMargin = endHighlightY + spacing
                layoutParams.bottomMargin = 0

                setViewGroup(layoutParams)
                if (arrowWidth == 0) path = null
                else {
                    val centerHighlightX = (endHighlightX + startHighlightX) / 2
                    val newArrowWidth = newArrowWidth(centerHighlightX, width)
                    if (newArrowWidth == 0) path = null
                    else {
                        path = Path()
                        path?.moveTo(
                            centerHighlightX.toFloat(),
                            (endHighlightY + arrowMargin).toFloat()
                        )
                        path?.lineTo(
                            (centerHighlightX - newArrowWidth / 2).toFloat(), (
                                    endHighlightY + spacing).toFloat()
                        )
                        path?.lineTo(
                            (centerHighlightX + newArrowWidth / 2).toFloat(), (
                                    endHighlightY + spacing).toFloat()
                        )
                        path?.close()
                    }
                }
            }
            CoachMarkPosition.TOP -> {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
                layoutParams.marginStart = spacing
                layoutParams.marginEnd = spacing
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = spacing
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = height - startHighlightY + spacing

                setViewGroup(layoutParams)
                if (arrowWidth == 0) path = null
                else {
                    val centerHighlightX = (endHighlightX + startHighlightX) / 2
                    val newArrowWidth = newArrowWidth(centerHighlightX, width)
                    if (newArrowWidth == 0) path = null
                    else {
                        path = Path()
                        path?.moveTo(
                            centerHighlightX.toFloat(),
                            (startHighlightY - arrowMargin).toFloat()
                        )
                        path?.lineTo(
                            (centerHighlightX - newArrowWidth / 2).toFloat(), (
                                    startHighlightY - spacing).toFloat()
                        )
                        path?.lineTo(
                            (centerHighlightX + newArrowWidth / 2).toFloat(), (
                                    startHighlightY - spacing).toFloat()
                        )
                        path?.close()
                    }
                }
            }
            CoachMarkPosition.CENTER -> positionCenter()
        }
    }

    private fun setViewGroup(params: LayoutParams) {
        viewGroup?.visibility = INVISIBLE
        viewGroup?.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                viewGroup?.visibility = VISIBLE
                viewGroup?.removeOnLayoutChangeListener(this)
            }
        })
        viewGroup?.layoutParams = params
        invalidate()
    }

    private fun newArrowWidth(highlightCenter: Int, maxWidthOrHeight: Int): Int {
        val safeArrowWidth = spacing + arrowWidth / 2
        return if (highlightCenter < safeArrowWidth ||
            highlightCenter > maxWidthOrHeight - safeArrowWidth
        ) 0
        else arrowWidth
    }

    private fun positionCenter() {
        coachMarkPosition = CoachMarkPosition.CENTER
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        layoutParams.marginStart = spacing
        layoutParams.marginEnd = spacing
        layoutParams.leftMargin = spacing
        layoutParams.rightMargin = spacing
        layoutParams.topMargin = spacing
        layoutParams.bottomMargin = spacing
        setViewGroup(layoutParams)
        path = null
    }

    private fun marginTopBottom(
        expectedTopMargin: Int,
        layoutParams: LayoutParams,
        viewHeight: Int
    ) {
        if (expectedTopMargin < spacing)
            layoutParams.topMargin = spacing
        else {
            val prevActualHeight = expectedTopMargin + viewHeight + spacing
            if (prevActualHeight > height) {
                val diff = prevActualHeight - height
                layoutParams.topMargin = expectedTopMargin - diff
            } else layoutParams.topMargin = expectedTopMargin
        }
    }

    private fun String.toHtml(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        else {
            @Suppress("DEPRECATION")
            Html.fromHtml(this)
        }
    }

    private fun View.backgroundColor(color: Int) {
        when (val background = this.background) {
            is ShapeDrawable -> background.paint.color = color
            is GradientDrawable -> background.setColor(color)
            else -> this.setBackgroundColor(color)
        }
    }

    private fun Bitmap.croppedBitmap(location: List<Int>, radius: Int): Bitmap {
        val output = Bitmap.createBitmap(
            2 * radius,
            2 * radius, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        val sourceRect = Rect(
            location[0] - radius,
            location[1] - radius,
            location[0] + radius,
            location[1] + radius
        )
        val destRect = Rect(0, 0, 2 * radius, 2 * radius)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(
            radius.toFloat(), radius.toFloat(),
            radius.toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, sourceRect, destRect, paint)
        return output
    }

    private fun Bitmap.croppedBitmap(location: List<Int>): Bitmap {
        val xStart = location[0]
        val yStart = location[1]
        val xEnd = location[2]
        val yEnd = location[3]
        val width = xEnd - xStart
        val height = yEnd - yStart
        val output = Bitmap.createBitmap(
            width,
            height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        val sourceRect = Rect(
            xStart,
            yStart,
            xEnd,
            yEnd
        )
        canvas.roundCorners(paint)
//        canvas.drawARGB(0, 0, 0, 0)
//        canvas.drawRect(destRect, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val destRect = Rect(0, 0, width, height)
        canvas.drawBitmap(this, sourceRect, destRect, paint)
        return output
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    private fun View.getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }

    private fun Canvas.roundCorners(paint: Paint) {
        val offset = 0f
        val rectF = RectF(
            offset,  // left
            offset,  // top
            this.width + offset,  // right
            this.height + offset // bottom
        )
        val cornersRadius = 10f
        this.drawRoundRect(
            rectF,
            cornersRadius,
            cornersRadius,
            paint
        )
    }

    interface OnStepPositionListener {
        fun onPrevious()
        fun onNext()
        fun onComplete()
    }
}
