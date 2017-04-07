package in.galaxyofandroid.spinnerdialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Md Farhan Raja on 2/23/2017
 */

public final class SpinnerDialog extends DialogFragment {

    public static void show(Fragment caller, int requestCode, FragmentManager manager, String title, ArrayList<String> items) {
        SpinnerDialog dialog = new SpinnerDialog();

        Bundle args = new Bundle(2);
        args.putString("title", required(title, "title"));
        args.putStringArrayList("items", required(items, "items"));
        dialog.setArguments(args);

        dialog.setTargetFragment(required(caller, "caller fragment"), requestCode);
        dialog.show(required(manager, "fragment manager"), null);
    }

    private String getTitle() { return getArguments().getString("title"); }
    private ArrayList<String> getItems() { return getArguments().getStringArrayList("items"); }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity context = getActivity();
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        View v = context.getLayoutInflater().inflate(R.layout.dialog_layout, null);

        TextView close = (TextView) v.findViewById(R.id.close);
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(getTitle());

        final ListView listView = (ListView) v.findViewById(R.id.list);
        final EditText searchBox = (EditText) v.findViewById(R.id.searchBox);
        final List<String> items = getItems();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.items_view, items);
        listView.setAdapter(adapter);
        adb.setView(v);
        final AlertDialog alertDialog = adb.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimations_SmileWindow;
//        alertDialog.setCancelable(false);

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

        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }

    private static <T> T required(T t, String tag) {
        if (t == null) throw new NullPointerException(tag + " is required");
        return t;
    }

}
