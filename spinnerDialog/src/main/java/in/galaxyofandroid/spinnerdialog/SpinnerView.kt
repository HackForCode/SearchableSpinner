package `in`.galaxyofandroid.spinnerdialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.widget.TextView

/**
 * Created by Mike on 07.04.17
 * @param E item type
 */
class SpinnerView<E : Parcelable> : TextView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @Suppress("UNUSED") constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    // must be set up

    @StringRes
    private var titleRes: Int = -1
    private var itemManager: ItemManager<E>? = null
    @StringRes
    private var emptyTextRes: Int = -1
    @DrawableRes
    private var emptyIconRes: Int = -1
    private var fragmentManager: FragmentManager? = null
    private var caller: Fragment? = null
    private var requestCode: Int = -1

    var windowAnimations: Int = -1
        @StyleRes get
        @StyleRes set

    // saved state

    var selectedItem: E? = null
        set(item) {
            val itemManager = itemManager ?: throw IllegalStateException("setup() was not called on this SpinnerView.")
            this.text = itemManager.toString(item)
            onChangeListener?.onItemSelected(item)
            field = item
        }

    init {
        setOnClickListener {
            if (titleRes <= 0) throw IllegalStateException("setup() was not called on this SpinnerView.")
            SpinnerDialog
                    .create(titleRes, itemManager!!, emptyTextRes, emptyIconRes)
                    .withWindowAnimations(windowAnimations)
                    .show(fragmentManager, caller, requestCode)
        }
    }

    fun setUp(
            @StringRes titleRes: Int, itemManager: ItemManager<E>,
            @StringRes emptyTextRes: Int, /*optional*/ @DrawableRes emptyIconRes: Int,
            fragmentManager: FragmentManager, caller: Fragment, requestCode: Int) {
        if (titleRes <= 0) throw IllegalArgumentException("invalid title resource")
        if (emptyTextRes <= 0) throw IllegalArgumentException("invalid 'empty text' resource")
        this.titleRes = titleRes
        this.itemManager = itemManager
        this.emptyTextRes = emptyTextRes
        this.emptyIconRes = emptyIconRes
        this.fragmentManager = fragmentManager
        this.caller = caller
        this.requestCode = requestCode
    }

    var onChangeListener: OnItemSelectedListener<E>? = null

    interface OnItemSelectedListener<E> {
        fun onItemSelected(item: E?)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            selectedItem = data!!.getParcelableExtra<E?>("item")
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return State(super.onSaveInstanceState(), selectedItem)
    }

    override fun onRestoreInstanceState(state: Parcelable?) =
            if (state is State<*>) {
                super.onRestoreInstanceState(state.superState)
                @Suppress("UNCHECKED_CAST")
                selectedItem = state.selectedItem as E // *$%#&!@ erasure
            } else {
                super.onRestoreInstanceState(state)
            }

    private class State<E : Parcelable?>(val superState: Parcelable, val selectedItem: E) : Parcelable {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(superState, flags)
            dest.writeParcelable(selectedItem, flags)
        }
        override fun describeContents(): Int = 0

        companion object {
            @Suppress("UNUSED") @JvmField val CREATOR = object : Parcelable.Creator<State<*>> {
                override fun newArray(size: Int): Array<State<*>?> =
                        arrayOfNulls(size)
                override fun createFromParcel(source: Parcel): State<*> {
                    val cl = javaClass.classLoader
                    return State(source.readParcelable(cl), source.readParcelable<Parcelable?>(cl))
                }

            }
        }
    }

}