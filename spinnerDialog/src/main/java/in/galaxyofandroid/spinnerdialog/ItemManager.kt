package `in`.galaxyofandroid.spinnerdialog

import android.app.Application
import android.content.res.Resources
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
     * @return either a tuple of (data, last page) or an exception which is occurred while loading
     */
    @WorkerThread
    fun load(app: Application, filter: String?, page: Int): Either<Throwable, Pair<List<E>, Int>>

    /**
     * Returns string representation of the given item.
     */
    fun toString(item: E?): String

    /**
     * Checks whether these items are equal or not.
     */
    fun equals(one: E, another: E): Boolean

    /**
     * Returns error message for specified throwable
     */
    fun getErrorMessage(resources: Resources, throwable: Throwable): CharSequence
}