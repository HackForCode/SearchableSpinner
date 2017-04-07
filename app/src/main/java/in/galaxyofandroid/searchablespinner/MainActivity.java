package in.galaxyofandroid.searchablespinner;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import in.galaxyofandroid.spinnerdialog.SpinnerDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new InitialFragment())
                    .commit();
        }
    }

    public static final class InitialFragment extends Fragment {

        private static final int RC_SELECT_CITY = 1;

        private ArrayList<String> items =
                new ArrayList<>(Arrays.asList(
                        "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata", "Surat",
                        "Pune", "Jaipur", "Lucknow", "Kanpur"));

        private TextView selectedItems;

        @Nullable @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_main, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            view.findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    SpinnerDialog.show(InitialFragment.this, RC_SELECT_CITY, getFragmentManager(), "Select or Search City", items);
                }
            });
            selectedItems = (TextView) view.findViewById(R.id.selectedItems);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SELECT_CITY && resultCode == Activity.RESULT_OK) {
                String item = data.getStringExtra("item");
                int position = data.getIntExtra("position", -1);
                selectedItems.setText(String.format(Locale.US, "Selected '%s' at %d", item, position));
            }
        }
    }

}
