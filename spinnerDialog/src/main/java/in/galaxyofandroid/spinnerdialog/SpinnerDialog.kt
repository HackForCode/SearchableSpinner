package `in`.galaxyofandroid.spinnerdialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import java.util.*

/**
 * Created by Md Farhan Raja on 2/23/2017
 */

class SpinnerDialog<E : Parcelable> : DialogFragment(), LoaderManager.LoaderCallbacks<Either<Throwable, List<E>>> {

    private lateinit var adapter: SpinnerDialogAdapter<E>
    private lateinit var progress: View
    private lateinit var empty: TextView
    private lateinit var searchBox: TextView

    fun withWindowAnimations(@StyleRes windowAnimations: Int): SpinnerDialog<*> {
        arguments.putInt("animations", windowAnimations)
        return this
    }

    fun show(manager: FragmentManager, caller: Fragment, requestCode: Int) {
        setTargetFragment(caller, requestCode)
        show(manager, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        val title = getString(args.getInt("titleRes"))
        val itemManager = args.getParcelable<ItemManager<E>>("item manager")
        val emptyText = getString(args.getInt("emptyTextRes"))
        val emptyIcon = args.getInt("emptyIconRes").let { if (it > 0) ContextCompat.getDrawable(activity, it) else null }
        val windowAnimations = args.getInt("animations", -1)
        val cancelRes = args.getInt("cancelRes", -1).let { if (it > 0) it else android.R.string.cancel }

        val activity = activity
        val v = activity.layoutInflater.inflate(R.layout.dialog_layout, null, false)

        val recyclerView = v.findViewById(R.id.list) as RecyclerView
        searchBox = v.findViewById(R.id.searchBox) as EditText
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
        return alertDialog
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Either<Throwable, List<E>>> {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                progress.visibility = View.VISIBLE
                val newFilter = searchBox.text.toString().let { if (it.isBlank()) null else it }
                val filter = (loaderManager.getLoader<Nothing>(0) as PagedLoader<*>).filter
                if (filter != newFilter) {
                    loaderManager.restartLoader(0, null, this@SpinnerDialog)
                }
            }
        })

        return PagedLoader(activity,
                searchBox.text.let { if (it.isBlank()) null else it.toString() },
                arguments.getParcelable("item manager"))
    }

    override fun onLoadFinished(loader: Loader<Either<Throwable, List<E>>>?, data: Either<Throwable, List<E>>) =
            data.let({
                Toast.makeText(activity, it.message ?: it.toString(), Toast.LENGTH_LONG).show() // todo: proper error handling
            }, {
                adapter.setData(it)
                progress.visibility = View.GONE
                empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            })

    override fun onLoaderReset(loader: Loader<Either<Throwable, List<E>>>?) {}

    private class PagedLoader<E : Parcelable>
    internal constructor(
            context: Context,
            internal val filter: String?,
            private val itemManager: ItemManager<E>
    ) : AsyncTaskLoader<Either<Throwable, List<E>>>(context) {

        private var list: List<E>? = null
        @Volatile private var loading = false
        private var currentPage = 0
        private var lastPage = Int.MAX_VALUE

        override fun onStartLoading() {
            val list = list
            if (!loading && (list == null || currentPage < lastPage))
                forceLoad() // if not loading yet, and if there's no or insufficient data
            else if (list != null)
                deliverResult(Right(list)) // we have some data
        }

        override fun loadInBackground(): Either<Throwable, List<E>> =
                itemManager.load(context as Application, filter, currentPage + 1).map({ (loadedList, lastPage) ->
                    val aList = ArrayList<E>(list?.size ?: 0 + loadedList.size)
                    list?.let { aList.addAll(it) }
                    aList.addAll(loadedList)
                    loading = false
                    list = aList
                    currentPage++
                    this.lastPage = lastPage
                    aList // we can't just add, Loader won't deliver the same object.
                })

        internal fun onItemBound(position: Int): Boolean {
            val size = list!!.size
            if (position > size - 3 && !loading && currentPage < lastPage) {
                loading = true
                forceLoad()
                return true
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

            val args = Bundle(5)
            args.putInt("titleRes", titleRes)
            args.putParcelable("item manager", itemManager)
            args.putInt("emptyTextRes", emptyTextRes)
            args.putInt("emptyIconRes", emptyIconRes)
            args.putInt("cancelRes", cancelRes)
            dialog.arguments = args

            return dialog
        }
    }
}
