package com.suryojiwandono.coachmark

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by suryo jiwandono on 2/18/2022.
 */

class CoachMark constructor(
    var view: View? = null,
    var title: String? = null,
    var description: String,
    var coachMarkPosition: CoachMarkPosition = CoachMarkPosition.CENTER,
    var isBackground: Boolean = false,
    val scrollView: ViewGroup? = null
) {
    var radius = 0
    var positions: List<Int>? = null

    fun recyclerItem(itemPosition: Int = 0): CoachMark {
        val isRecycler = view is RecyclerView
        if (isRecycler) {
            val recyclerView = view as RecyclerView
            val itemView = recyclerView.findViewHolderForLayoutPosition(itemPosition)?.itemView
            this.view = itemView
            this.isBackground = true // must
        }
        return this
    }

    fun circle(activity: Activity, margin: Int = 20): CoachMark {
        val location = IntArray(2)
        view?.getLocationInWindow(location)

        val width = view?.width ?: 0
        val height = view?.height ?: 0
        val xStart = location[0]
        val yStart = location[1] - CoachMarkDialog.getStatusBarHeight(activity)
        val xEnd: Int = location[0] + width
        val yEnd: Int =
            location[1] + height - CoachMarkDialog.getStatusBarHeight(activity)
        val xCenter = (xStart + xEnd) / 2
        val yCenter = (yStart + yEnd) / 2
        val max = if (width > height) width else height
        val radius: Int = max / 2 + margin
        this.view = activity.findViewById(android.R.id.content)
        this.positions = listOf(xCenter, yCenter)
        this.radius = radius
        this.isBackground = true // must
        return this
    }

    fun square(activity: Activity, margin: Int = 20): CoachMark {
        val location = IntArray(2)
        view?.getLocationInWindow(location)

        val width = view?.width ?: 0
        val height = view?.height ?: 0
        val xStart = location[0]
        val yStart = location[1] - CoachMarkDialog.getStatusBarHeight(activity)
        val xEnd: Int = location[0] + width
        val yEnd: Int =
            location[1] + height - CoachMarkDialog.getStatusBarHeight(activity)
        this.view = activity.findViewById(android.R.id.content)
        this.positions = listOf(xStart - margin, yStart - margin, xEnd + margin, yEnd + margin)
        this.isBackground = true // must
        return this
    }

    open class Builder : Parcelable {
        private var fragmentActivity: FragmentActivity? = null
        private var coachMarks: List<CoachMark>? = null
        var layoutView = R.layout.cm
        var textTitleColor = 0
        var textDescriptionColor = 0
        var shadowColor = 0
        var backgroundColor = 0
        var textTitleSize = 0
        var textDescriptionSize = 0
        var spacing = 0
        var buttonPreviousText: String? = "PREV"
        var buttonNextText: String? = "NEXT"
        var buttonFinishText: String? = "FINISH"
        var buttonSkipText: String? = "SKIP"
        var isCancelable = false
        var isArrow = true
        var isSkip = false
        var isPrevious = false
        var isIndicator = false
        var delimiterText: String? = "/"
        var buttonStyle = 0

        fun coachMark(coachMark: CoachMark): Builder {
            this.coachMarks = listOf(coachMark)
            return this
        }

        fun coachMark(coachMarks: List<CoachMark>): Builder {
            this.coachMarks = coachMarks
            return this
        }

        fun layoutView(@LayoutRes layoutView: Int): Builder {
            this.layoutView = layoutView
            return this
        }

        fun buttonStyle(@StyleRes id: Int): Builder {
            this.buttonStyle = id
            return this
        }

        fun textTitleColor(@ColorRes id: Int): Builder {
            this.textTitleColor = id
            return this
        }

        fun textDescriptionColor(@ColorRes id: Int): Builder {
            this.textDescriptionColor = id
            return this
        }

        fun textTitleSize(@DimenRes id: Int): Builder {
            this.textTitleSize = id
            return this
        }

        fun textDescriptionSize(@DimenRes id: Int): Builder {
            this.textDescriptionSize = id
            return this
        }


        fun spacing(@DimenRes id: Int): Builder {
            this.spacing = id
            return this
        }

        fun shadowColor(@ColorRes id: Int): Builder {
            this.shadowColor = id
            return this
        }

        fun backgroundColor(@ColorRes id: Int): Builder {
            this.backgroundColor = id
            return this
        }

        fun arrow(isArrow: Boolean): Builder {
            this.isArrow = isArrow
            return this
        }

        fun skipButton(isSkip: Boolean): Builder {
            this.isSkip = isSkip
            return this
        }

        fun indicator(delimiterText: String? = "/"): Builder {
            this.isIndicator = true
            this.delimiterText = delimiterText
            return this
        }

        fun previousButton(isButtonPrevious: Boolean): Builder {
            this.isPrevious = isButtonPrevious
            return this
        }

        fun buttonFinishText(buttonFinishText: String): Builder {
            this.buttonFinishText = buttonFinishText
            return this
        }

        fun buttonPreviousText(buttonPreviousText: String): Builder {
            this.buttonPreviousText = buttonPreviousText
            return this
        }

        fun buttonNextText(buttonNextText: String): Builder {
            this.buttonNextText = buttonNextText
            return this
        }

        fun buttonSkipText(buttonSkipText: String): Builder {
            this.buttonSkipText = buttonSkipText
            return this
        }

        fun cancelable(isCancelable: Boolean): Builder {
            this.isCancelable = isCancelable
            return this
        }

        fun build(): CoachMarkDialog {
            return CoachMarkDialog.newInstance(fragmentActivity, coachMarks, this)
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(layoutView)
            dest.writeInt(textTitleColor)
            dest.writeInt(textDescriptionColor)
            dest.writeInt(shadowColor)
            dest.writeInt(textTitleSize)
            dest.writeInt(textDescriptionSize)
            dest.writeInt(spacing)
            dest.writeInt(backgroundColor)
            dest.writeString(buttonPreviousText)
            dest.writeString(buttonNextText)
            dest.writeString(buttonFinishText)
            dest.writeString(buttonSkipText)
            dest.writeString(delimiterText)
            dest.writeByte(if (isCancelable) 1.toByte() else 0.toByte())
            dest.writeByte(if (isArrow) 1.toByte() else 0.toByte())
            dest.writeByte(if (isSkip) 1.toByte() else 0.toByte())
            dest.writeByte(if (isPrevious) 1.toByte() else 0.toByte())
            dest.writeByte(if (isIndicator) 1.toByte() else 0.toByte())
        }

        constructor(fragmentActivity: FragmentActivity?) {
            this.fragmentActivity = fragmentActivity
        }

        protected constructor(parcel: Parcel) {
            layoutView = parcel.readInt()
            textTitleColor = parcel.readInt()
            textDescriptionColor = parcel.readInt()
            shadowColor = parcel.readInt()
            textTitleSize = parcel.readInt()
            textDescriptionSize = parcel.readInt()
            spacing = parcel.readInt()
            backgroundColor = parcel.readInt()
            buttonPreviousText = parcel.readString()
            buttonNextText = parcel.readString()
            buttonFinishText = parcel.readString()
            isCancelable = parcel.readByte().toInt() != 0
            isArrow = parcel.readByte().toInt() != 0
            buttonSkipText = parcel.readString()
            delimiterText = parcel.readString()
            isSkip = parcel.readByte().toInt() != 0
            isPrevious = parcel.readByte().toInt() != 0
            isIndicator = parcel.readByte().toInt() != 0
        }

        companion object CREATOR : Parcelable.Creator<Builder> {
            override fun createFromParcel(parcel: Parcel): Builder {
                return Builder(parcel)
            }

            override fun newArray(size: Int): Array<Builder?> {
                return arrayOfNulls(size)
            }
        }
    }
}