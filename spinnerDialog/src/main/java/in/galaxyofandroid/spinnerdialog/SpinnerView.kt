package `in`.galaxyofandroid.spinnerdialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.StyleRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by Mike on 07.04.17
 */
class SpinnerView : TextView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @Suppress("UNUSED") constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    // must be set up

    private var title: String? = null
    private var items: ArrayList<String>? = null
    private var fragmentManager: FragmentManager? = null
    private var caller: Fragment? = null
    private var requestCode: Int = -1

    var windowAnimations: Int = -1
        @StyleRes get
        @StyleRes set

    // saved state

    var selectedItemPosition: Int = -1
        set(position) {
            val items = items ?: throw IllegalStateException("setup() was not called on this SpinnerView.")
            val item = if (position >= 0) items[position] else null
            this.text = item
            onChangeListener?.onItemSelected(item, position)
            field = position
        }

    init {
        setOnClickListener {
            if (title == null) throw IllegalStateException("setup() was not called on this SpinnerView.")
            SpinnerDialog
                    .create(title!!, items!!)
                    .withWindowAnimations(windowAnimations)
                    .show(fragmentManager, caller, requestCode)
        }
    }

    fun setUp(title: String, items: ArrayList<String>, fragmentManager: FragmentManager, caller: Fragment, requestCode: Int) {
        this.title = title
        this.items = items
        this.fragmentManager = fragmentManager
        this.caller = caller
        this.requestCode = requestCode
    }

    var onChangeListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(item: String?, position: Int)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            val pos = data.getIntExtra("position", -1)
            selectedItemPosition = pos
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return State(super.onSaveInstanceState(), selectedItemPosition)
    }

    override fun onRestoreInstanceState(state: Parcelable?) =
            if (state is State) {
                super.onRestoreInstanceState(state.superState)
                selectedItemPosition = state.selectedItemPosition
            } else {
                super.onRestoreInstanceState(state)
            }

    private class State(val superState: Parcelable, val selectedItemPosition: Int) : Parcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(superState, flags)
            dest.writeInt(selectedItemPosition)
        }
        override fun describeContents(): Int = 0

        companion object {
            @Suppress("UNUSED") @JvmField val CREATOR = object : Parcelable.Creator<State> {
                override fun newArray(size: Int): Array<State?> =
                        arrayOfNulls(size)
                override fun createFromParcel(source: Parcel): State =
                        State(source.readParcelable(javaClass.classLoader), source.readInt())

            }
        }
    }

}