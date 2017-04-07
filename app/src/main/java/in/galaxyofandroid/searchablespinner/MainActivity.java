package in.galaxyofandroid.searchablespinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.galaxyofandroid.spinnerdialog.SpinnerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

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
        private SpinnerView spinner;

        @Nullable @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_main, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            spinner = (SpinnerView) view.findViewById(R.id.show);
            spinner.setUp("Select or Search City", items, getFragmentManager(), InitialFragment.this, RC_SELECT_CITY);
            spinner.setWindowAnimations(R.style.DialogAnimations_SmileWindow);
            spinner.setOnChangeListener(new SpinnerView.OnItemSelectedListener() {
                @Override public void onItemSelected(String item, int position) {
                    selectedItems.setText(item == null
                            ? null
                            : String.format(Locale.US, "Selected '%s' at %d", item, position));
                }
            });
            selectedItems = (TextView) view.findViewById(R.id.selectedItems);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            spinner.onActivityResult(requestCode, resultCode, data);
        }
    }

}
