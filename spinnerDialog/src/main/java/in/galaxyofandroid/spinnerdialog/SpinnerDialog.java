package in.galaxyofandroid.spinnerdialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Md Farhan Raja on 2/23/2017
 */

public final class SpinnerDialog extends DialogFragment {

    public static SpinnerDialog create(String title, ArrayList<String> items) {
        SpinnerDialog dialog = new SpinnerDialog();

        Bundle args = new Bundle(2);
        args.putString("title", required(title, "title"));
        args.putStringArrayList("items", required(items, "items"));
        dialog.setArguments(args);

        return dialog;
    }

    public SpinnerDialog withWindowAnimations(@StyleRes int windowAnimations) {
        getArguments().putInt("animations", windowAnimations);
        return this;
    }

    public void show(Fragment caller, int requestCode, FragmentManager manager) {
        setTargetFragment(required(caller, "caller fragment"), requestCode);
        show(required(manager, "fragment manager"), null);
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title;
        final ArrayList<String> items;
        final int windowAnimations;
        {
            Bundle args = getArguments();
            title = required(args.getString("title"), "title");
            items = required(args.getStringArrayList("items"), "items");
            windowAnimations = args.getInt("animations", -1);
        }

        Activity context = getActivity();
        View v = context.getLayoutInflater().inflate(R.layout.dialog_layout, null, false);

        final ListView listView = (ListView) v.findViewById(R.id.list);
        final EditText searchBox = (EditText) v.findViewById(R.id.searchBox);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
        final AlertDialog alertDialog =
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setView(v)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

        if (windowAnimations > 0) alertDialog.getWindow().getAttributes().windowAnimations = windowAnimations;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Intent data = new Intent();
                data.putExtra("item", items.get(pos));
                data.putExtra("position", pos);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
                alertDialog.dismiss();
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(searchBox.getText().toString());
            }
        });
        return alertDialog;
    }

    private static <T> T required(T t, String tag) {
        if (t == null) throw new NullPointerException(tag + " is required");
        return t;
    }

}
