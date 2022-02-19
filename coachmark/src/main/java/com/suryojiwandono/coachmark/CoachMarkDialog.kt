package com.suryojiwandono.coachmark

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

/**
 * Created by suryo jiwandono on 2/18/2022.
 */

class CoachMarkDialog : DialogFragment() {
    private var currentPosition = -1
    private var builder: CoachMark.Builder? = null
    private var isViewGroup = false
    private var stepListener: OnStepListener? = null

    companion object {
        val TAG = CoachMarkDialog::class.java.simpleName
        private const val argsBundles = "COACHMARK"
        const val SCROLL_DELAY = 350
        private var fragmentActivity: FragmentActivity? = null
        private var coachMarks: List<CoachMark>? = null
        fun newInstance(
            fragmentActivity: FragmentActivity?,
            coachMarks: List<CoachMark>?,
            builder: CoachMark.Builder?
        ): CoachMarkDialog {
            this.coachMarks = coachMarks
            this.fragmentActivity = fragmentActivity
            val args = Bundle()
            args.putParcelable(argsBundles, builder)
            val fragment = CoachMarkDialog()
            fragment.arguments = args
            return fragment
        }

        fun getStatusBarHeight(context: Context): Int {
            var height = 0
            val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resId > 0) height = context.resources.getDimensionPixelSize(resId)
            return height
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        builder = requireArguments()[argsBundles] as CoachMark.Builder?
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = CoachMarkView(requireActivity(), builder)
        view.setCoachMarkListener(object : CoachMarkView.OnStepPositionListener {
            override fun onPrevious() {
                this@CoachMarkDialog.onPrevious()
            }

            override fun onNext() {
                this@CoachMarkDialog.onNext()
            }

            override fun onComplete() {
                stepListener?.onComplete()
                onClose()
            }
        })
        isCancelable = builder?.isCancelable == true
        return view
    }

    fun onNext() {
        if (currentPosition + 1 >= coachMarks?.size ?: 0) onClose()
        else this@CoachMarkDialog.show(currentPosition + 1)
    }

    fun onPrevious() {
        if (currentPosition - 1 < 0) currentPosition = 0
        else this@CoachMarkDialog.show(currentPosition - 1)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0f)
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = object : Dialog(requireActivity(), R.style.CoachMark) {
            override fun onBackPressed() {
                if (builder?.isCancelable == true) {
                    onPrevious()
                }
            }
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    fun show(showPosition: Int = 0) {
        if (fragmentActivity == null || fragmentActivity?.isFinishing == true) return
        try {
            var position = showPosition
            if (position < 0 || position >= coachMarks?.size ?: 0) position = 0
            val previousPosition = currentPosition
            currentPosition = position
            isViewGroup = false
            if (stepListener != null) {
                isViewGroup = stepListener?.onStep(
                    previousPosition, currentPosition,
                    coachMarks?.get(currentPosition)
                ) ?: false
            }

            if (isViewGroup) return
            val coachMark = coachMarks?.get(currentPosition)
            val viewGroup = coachMark?.scrollView
            if (viewGroup != null) {
                val viewToFocus = coachMark.view
                isViewGroup = if (viewToFocus != null) {
                    hide()
                    viewGroup.post {
                        if (viewGroup is ScrollView) {
                            val scrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            viewToFocus.relativePosition(
                                viewGroup,
                                relativeLocation
                            )
                            scrollView.smoothScrollTo(0, relativeLocation[1])
                            scrollView.postDelayed(
                                { showView(fragmentActivity, coachMark) },
                                SCROLL_DELAY.toLong()
                            )
                        } else if (viewGroup is NestedScrollView) {
                            val nestedScrollView = viewGroup
                            val relativeLocation = IntArray(2)
                            viewToFocus.relativePosition(
                                viewGroup,
                                relativeLocation
                            )
                            nestedScrollView.smoothScrollTo(0, relativeLocation[1])
                            nestedScrollView.postDelayed(
                                { showView(fragmentActivity, coachMark) },
                                SCROLL_DELAY.toLong()
                            )
                        }
                    }
                    true
                } else false
            }
            if (!isViewGroup) showView(fragmentActivity, coachMarks?.get(currentPosition))
        } catch (e: Exception) {
            try {
                dismiss()
            } catch (e2: Exception) {
            }
        }
    }

    private fun hide() {
        val layout = this@CoachMarkDialog.view as CoachMarkView? ?: return
        layout.hide()
    }

    private fun showView(activity: FragmentActivity?, coachMark: CoachMark?) {
        if (activity == null || activity.isFinishing) return
        val fm = activity.supportFragmentManager
        if (!isVisible) {
            try {
                if (!isAdded) show(fm, TAG)
                else if (isHidden) {
                    val ft = fm.beginTransaction()
                    ft.show(this@CoachMarkDialog)
                    ft.commit()
                }
            } catch (e: IllegalStateException) {
                return
            }
        }

        coachMark?.view?.post {
            showView(coachMark)
        } ?: showView(coachMark)
    }

    private fun showView(coachMark: CoachMark?) {
        try {
            val layout = this@CoachMarkDialog.view as CoachMarkView?
            if (layout == null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    showView(coachMark)
                }, 1000)
                return
            }

            layout.show(coachMark, currentPosition, coachMarks?.size ?: 0)
        } catch (t: Throwable) {
        }
    }

    fun onClose() {
        try {
            dismiss()
            val view = this@CoachMarkDialog.view as CoachMarkView? ?: return
            view.close()
        } catch (e: Exception) {
        }
    }

    private fun View.relativePosition(root: ViewParent, location: IntArray) {
        if (this.parent === root) {
            location[0] += this.left
            location[1] += this.top
        } else {
            location[0] += this.left
            location[1] += this.top
            this.relativePosition(root, location)
        }
    }

    interface OnStepListener {
        fun onStep(
            previous: Int,
            current: Int,
            coachMark: CoachMark?
        ): Boolean

        fun onComplete()
    }

    fun addOnStepListener(listenerChange: OnStepListener?) {
        this.stepListener = listenerChange
    }
}