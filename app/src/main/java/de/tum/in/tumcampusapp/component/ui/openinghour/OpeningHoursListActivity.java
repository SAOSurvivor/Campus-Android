package de.tum.in.tumcampusapp.component.ui.openinghour;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.smartmic.BarcodeActivity;
import de.tum.in.tumcampusapp.component.ui.smartmic.SmartMicSend;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link OpeningHoursDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link OpeningHoursListFragment} and the item details (if present) is a
 * {@link OpeningHoursDetailFragment}.
 */
public class OpeningHoursListActivity extends BaseActivity {

    public OpeningHoursListActivity() {
        super(R.layout.activity_openinghourslist);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Log.d("rishabh", "in oncreate");
            Intent intent = new Intent(this, SmartMicSend.class);
// To pass any data to next activity
//            intent.putExtra("keyIdentifier", value)
// start your next activity
            startActivity(intent);
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.contentFrame, OpeningHoursListFragment.newInstance())
//                    .commit();
        }
    }

}
