package `in`.galaxyofandroid.spinnerdialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.annotation.StyleRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.util.*

/**
 * Created by Md Farhan Raja on 2/23/2017
 */

class SpinnerDialog<E : Parcelable> : DialogFragment(), LoaderManager.LoaderCallbacks<List<E>> {

    private var filter: String? = null
    private lateinit var adapter: SpinnerDialogAdapter<E>
    private lateinit var progress: View
    private lateinit var empty: TextView

    fun withWindowAnimations(@StyleRes windowAnimations: Int): SpinnerDialog<*> {
        arguments.putInt("animations", windowAnimations)
        return this
    }

    fun show(manager: FragmentManager, caller: Fragment, requestCode: Int) {
        setTargetFragment(required(caller, "caller fragment"), requestCode)
        show(required(manager, "fragment manager"), null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title: String
        val itemManager: ItemManager<E>
        val emptyText: String
        val emptyIcon: Drawable?
        val windowAnimations: Int
        val cancelRes: Int

        val args = arguments
        title = getString(args.getInt("titleRes"))
        itemManager = required(args.getParcelable<ItemManager<E>>("item manager"), "item manager")
        emptyText = getString(args.getInt("emptyTextRes"))
        val emptyIconRes = args.getInt("emptyIconRes")
        emptyIcon = if (emptyIconRes > 0) ContextCompat.getDrawable(activity, emptyIconRes) else null
        windowAnimations = args.getInt("animations", -1)
        val _cancelRes = args.getInt("cancelRes", -1)
        cancelRes = if (_cancelRes > 0) _cancelRes else android.R.string.cancel

        val activity = activity
        val v = activity.layoutInflater.inflate(R.layout.dialog_layout, null, false)

        val recyclerView = v.findViewById(R.id.list) as RecyclerView
        val searchBox = v.findViewById(R.id.searchBox) as EditText
        adapter = SpinnerDialogAdapter(getActivity(), itemManager)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        progress = v.findViewById(R.id.progress)
        empty = v.findViewById(R.id.empty) as TextView
        empty.text = emptyText
        empty.setCompoundDrawablesWithIntrinsicBounds(emptyIcon, null, null, null)

        val alertDialog = AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(v)
                .setNegativeButton(cancelRes, null)
                .create()

        if (windowAnimations > 0) {
            alertDialog.window.attributes.windowAnimations = windowAnimations
        }

        adapter.listener = object : SpinnerDialogAdapter.OnItemClickListener<E> {
            override fun onItemClick(e: E) {
                targetFragment.onActivityResult(
                        targetRequestCode, Activity.RESULT_OK, Intent().also { it.putExtra("item", e) })
                alertDialog.dismiss()
            }

            override fun onItemBound(position: Int) {
                if ((loaderManager.getLoader<Any>(0) as PagedLoader<*>).onItemBound(position)) {
                    progress.visibility = View.VISIBLE
                }
            }
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                progress.visibility = View.VISIBLE
                filter = searchBox.text.toString()
                loaderManager.restartLoader(0, null, this@SpinnerDialog)
            }
        })
        return alertDialog
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<E>> {
        return createLoader(activity, filter, arguments.getParcelable<ItemManager<E>>("item manager"))
    }

    override fun onLoadFinished(loader: Loader<List<E>>?, data: List<E>) {
        adapter.setData(data)
        progress.visibility = View.GONE
        empty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onLoaderReset(loader: Loader<List<E>>?) {}

    private class PagedLoader<E : Parcelable>
    internal constructor(
            context: Context,
            private val filter: String?,
            private val itemManager: ItemManager<E>
    ) : AsyncTaskLoader<List<E>>(context) {

        private var list = emptyList<E>()
        @Volatile private var loading = false

        override fun onStartLoading() {
            val total = itemManager.getTotal(context as Application, filter)
            if (list.isEmpty() && total != 0 || total > list.size && takeContentChanged())
                forceLoad() // total size is unknown or greater than available size
            else
                deliverResult(list) // we've got it all
        }

        override fun loadInBackground(): List<E> {
            val loaded = itemManager.load(context as Application, filter, list.size) // fixme: error handling!!!11
            val aList = ArrayList<E>(list.size + loaded.size)
            aList.addAll(list)
            aList.addAll(loaded)
            loading = false
            list = aList
            return aList // we can't just add, Loader won't deliver the same object.
        }

        internal fun onItemBound(position: Int): Boolean {
            val size = list.size
            if (position > size - 3 && !loading) {
                val total = itemManager.getTotal(context as Application, filter)
                if (total < 0 || total > size) {
                    loading = true
                    onContentChanged()
                    return true
                }
            }
            return false
        }
    }

    companion object {

        fun <E : Parcelable> create(@StringRes titleRes: Int, itemManager: ItemManager<E>,
                                    @StringRes emptyTextRes: Int,
                                    @DrawableRes emptyIconRes: Int,
                                    @StringRes cancelRes: Int): SpinnerDialog<E> {
            if (titleRes <= 0) throw IllegalArgumentException("invalid title resource")
            if (emptyTextRes <= 0) throw IllegalArgumentException("invalid 'empty text' resource")

            val dialog = SpinnerDialog<E>()

            val args = Bundle(2)
            args.putInt("titleRes", titleRes)
            args.putParcelable("item manager", required(itemManager, "item manager"))
            args.putInt("emptyTextRes", emptyTextRes)
            args.putInt("emptyIconRes", emptyIconRes)
            args.putInt("cancelRes", cancelRes)
            dialog.arguments = args

            return dialog
        }

        private fun <T> required(t: T?, tag: String): T {
            if (t == null) throw NullPointerException(tag + " is required")
            return t
        }

        private fun <E : Parcelable> createLoader(
                context: Context, filter: String?, manager: ItemManager<E>): Loader<List<E>> {
            return PagedLoader(context, filter, manager)
        }
    }
}
