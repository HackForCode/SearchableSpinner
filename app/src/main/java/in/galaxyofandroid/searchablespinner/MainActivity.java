package in.galaxyofandroid.searchablespinner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.galaxyofandroid.spinnerdialog.ItemManager;
import in.galaxyofandroid.spinnerdialog.SpinnerView;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

        private List<String> items = Arrays.asList(
                "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata", "Surat", "Pune",
                "Jaipur", "Lucknow", "Kanpur");

        private TextView selectedItems;
        private SpinnerView<ParcelString> spinner;

        @Nullable @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.activity_main, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            spinner = (SpinnerView<ParcelString>) view.findViewById(R.id.show);
            spinner.setUp(R.string.spinner_title,
                    new SimpleItemManager(items),
                    R.string.spinner_empty_text, -1,
                    getFragmentManager(), InitialFragment.this, RC_SELECT_CITY);
            spinner.setWindowAnimations(R.style.DialogAnimations_SmileWindow);
            spinner.setOnChangeListener(new SpinnerView.OnItemSelectedListener<ParcelString>() {
                @Override public void onItemSelected(ParcelString item) {
                    selectedItems.setText(item == null
                            ? null
                            : String.format(Locale.US, "Selected '%s'", item));
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

    private static class SimpleItemManager implements ItemManager<ParcelString> {
        private final ArrayList<ParcelString> items;
        SimpleItemManager(List<String> items) {
            ArrayList<ParcelString> ps = new ArrayList<>(items.size());
            for (String item : items) {
                ps.add(new ParcelString(item));
            }
            this.items = ps;
        }
        SimpleItemManager(ArrayList<ParcelString> items) {
            this.items = items;
        }
        @NotNull @Override
        public List<ParcelString> load(@Nullable String filter, int offset) {
            try { Thread.sleep(1000); } catch (Exception e) { throw new AssertionError(e); }

            List<ParcelString> list = filtered(filter);

            Iterator<ParcelString> itr = list.iterator();
            for (int i = 0; i < offset; i++) { // drop offset
                itr.next();
            }

            List<ParcelString> result = new ArrayList<>();
            for (int i = 0; i < 5 && itr.hasNext(); i++) { // take five
                result.add(itr.next());
            }

            return result;
        }
        @NotNull @Override
        public String toString(@Nullable ParcelString item) {
            return item == null ? "" : item.toString();
        }
        @Override public int getTotal(@Nullable String filter) {
            return filtered(filter).size();
        }

        @Override
        public boolean equals(@NotNull ParcelString one, @NotNull ParcelString another) {
            return one.toString().equals(another.toString());
        }

        private List<ParcelString> filtered(String filter) {
            if (filter == null) {
                return items;
            } else {
                List<ParcelString> filtered = new ArrayList<>();
                for (ParcelString item : items) {
                    if (item.toString().toLowerCase().contains(filter.toLowerCase())) {
                        filtered.add(item);
                    }
                }
                return filtered;
            }
        }

        @Override public int describeContents() {
            return 0;
        }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedList(items);
        }
        public static final Creator<SimpleItemManager> CREATOR = new Creator<SimpleItemManager>() {
            @Override public SimpleItemManager createFromParcel(Parcel source) {
                return new SimpleItemManager(source.createTypedArrayList(ParcelString.CREATOR));
            }
            @Override public SimpleItemManager[] newArray(int size) {
                return new SimpleItemManager[size];
            }
        };
    }

    public static class ParcelString implements Parcelable {
        private final String value;
        ParcelString(String value) {
            this.value = value;
        }
        @Override public String toString() {
            return value;
        }

        @Override public int describeContents() {
            return 0;
        }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(value);
        }
        public static final Parcelable.Creator<ParcelString> CREATOR = new Parcelable.Creator<ParcelString>() {
            @Override public ParcelString createFromParcel(Parcel source) {
                return new ParcelString(source.readString());
            }
            @Override public ParcelString[] newArray(int size) {
                return new ParcelString[size];
            }
        };
    }

}
