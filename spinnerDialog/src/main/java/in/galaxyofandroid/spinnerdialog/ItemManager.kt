package `in`.galaxyofandroid.spinnerdialog

import android.os.Parcelable
import android.support.annotation.WorkerThread

/**
 * Created by Mike on 07.04.17.
 * This manager is intended for loading items
 * @param E item type
 */
interface ItemManager<E : Parcelable> : Parcelable {
    /**
     * Loads items with the given filter from the specified offset.
     */
    @WorkerThread
    fun load(filter: String?, offset: Int): List<E>

    /**
     * Indicates total item count. -1 while unknown.
     */
    fun getTotal(filter: String?): Int

    /**
     * Returns string representation of the given item.
     */
    fun toString(item: E?): String

    /**
     * Checks whether these items are equal or not.
     */
    fun equals(one: E, another: E): Boolean
}