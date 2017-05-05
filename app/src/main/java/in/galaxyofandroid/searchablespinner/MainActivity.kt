package `in`.galaxyofandroid.searchablespinner

import `in`.galaxyofandroid.spinnerdialog.Either
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import `in`.galaxyofandroid.spinnerdialog.ItemManager
import `in`.galaxyofandroid.spinnerdialog.Left
import `in`.galaxyofandroid.spinnerdialog.SpinnerView
import java.lang.Math.ceil

import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, InitialFragment())
                    .commit()
        }
    }

    class InitialFragment : Fragment() {

        private val items = listOf("Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata",
                "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur")

        private lateinit var selectedItems: TextView
        private lateinit var spinner: SpinnerView<ParcelString>

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
                inflater.inflate(R.layout.activity_main, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            @Suppress("UNCHECKED_CAST")
            spinner = view.findViewById(R.id.show) as SpinnerView<ParcelString>
            spinner.setUp(R.string.spinner_title, SimpleItemManager(items), R.string.spinner_empty_text, -1, -1,
                    fragmentManager, this@InitialFragment, RC_SELECT_CITY)
            spinner.windowAnimations = R.style.DialogAnimations_SmileWindow
            spinner.onChangeListener = object : SpinnerView.OnItemSelectedListener<ParcelString> {
                override fun onItemSelected(item: ParcelString?) {
                    selectedItems.text = item?.let { String.format(Locale.US, "Selected '%s'", it) }
                }
            }
            selectedItems = view.findViewById(R.id.selectedItems) as TextView
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            spinner.onActivityResult(requestCode, resultCode, data)
        }

        private companion object {
            private const val RC_SELECT_CITY = 1
        }
    }

    private class SimpleItemManager : ItemManager<ParcelString> {
        private val items: ArrayList<ParcelString>

        internal constructor(items: List<String>) {
            this.items = items.mapTo(ArrayList(items.size), ::ParcelString)
        }

        internal constructor(items: ArrayList<ParcelString>) {
            this.items = items
        }

        override fun load(app: Application, filter: String?, page: Int): Either<Pair<List<ParcelString>, Int>, Throwable> =
                filtered(filter).let { Left(Pair(it.drop(5 * (page-1)).take(5), ceil(it.size / 5.0).toInt())) }.also { Thread.sleep(1000) }

        override fun toString(item: ParcelString?): String =
                item?.toString() ?: ""

        override fun equals(one: ParcelString, another: ParcelString): Boolean =
                one.toString() == another.toString()

        private fun filtered(filter: String?): List<ParcelString> =
                if (filter == null) items else {
                    val lowerFilter = filter.toLowerCase()
                    items.filter { it.toString().toLowerCase().contains(lowerFilter) }
                }

        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeTypedList(items)
        companion object {
            @Suppress("UNUSED") @JvmField val CREATOR: Parcelable.Creator<SimpleItemManager> = parcelableCreator {
                SimpleItemManager(it.createTypedArrayList(ParcelString.CREATOR))
            }
        }
    }

    class ParcelString internal constructor(private val value: String) : Parcelable {

        override fun toString(): String = value

        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(value)
        }
        companion object {
            @JvmField val CREATOR: Parcelable.Creator<ParcelString> = parcelableCreator {
                ParcelString(it.readString())
            }
        }
    }

}

inline fun <reified T> parcelableCreator(
        crossinline create: (Parcel)->T
) = object : Parcelable.Creator<T> {
    override fun newArray(size: Int): Array<T?> =
            arrayOfNulls(size)
    override fun createFromParcel(src: Parcel): T =
            create(src)
}