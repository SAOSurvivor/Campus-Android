package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Observer;

import com.crashlytics.android.Crashlytics;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.tum.in.tumcampusapp.App;
import de.tum.in.tumcampusapp.BuildConfig;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.AuthenticationManager;
import de.tum.in.tumcampusapp.api.tumonline.CacheControl;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseNavigationActivity;
import de.tum.in.tumcampusapp.component.ui.overview.MainActivity;
import de.tum.in.tumcampusapp.service.DownloadWorker;
import de.tum.in.tumcampusapp.service.StartSyncReceiver;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;
import io.fabric.sdk.android.Fabric;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Entrance point of the App.
 */
public class StartupActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 0;
    private static final String[] PERMISSIONS_LOCATION = {ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION};

    private final AtomicBoolean initializationFinished = new AtomicBoolean(false);
    private int tapCounter; // for easter egg

    private Observer<Unit> downloadCompletionHandler = ignored ->
            openMainActivityIfInitializationFinished();

    @Inject
    DownloadWorker.WorkerActions workerActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        ((App) getApplicationContext()).getAppComponent()
                .downloadComponent()
                .inject(this);

        // Only use Crashlytics if we are not compiling debug
        boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        if (!BuildConfig.DEBUG && !isDebuggable) {
            Fabric.with(this, new Crashlytics());
            Crashlytics.setString("TUMID", Utils.getSetting(this, Const.LRZ_ID, ""));
            Crashlytics.setString("DeviceID", AuthenticationManager.getDeviceID(this));
        }

        int savedAppVersion = Utils.getSettingInt(this, Const.SAVED_APP_VERSION, BuildConfig.VERSION_CODE);
        if (savedAppVersion < BuildConfig.VERSION_CODE) {
            Utils.setSetting(this, Const.SHOW_UPDATE_NOTE, true);
            Utils.setSetting(this, Const.UPDATE_MESSAGE, "");
        }
        // Always set current app version, otherwise it will never be initialized and the update note is never displayed
        Utils.setSetting(this, Const.SAVED_APP_VERSION, BuildConfig.VERSION_CODE);

        initEasterEgg();

        new Thread(this::init).start();
    }

    private void initEasterEgg() {
        if (Utils.getSettingBool(this, Const.RAINBOW_MODE, false)) {
            ImageView tumLogo = findViewById(R.id.startupTumLogo);
            tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
        }

        tapCounter = 0;
        View background = findViewById(R.id.container);
        background.setOnClickListener(view -> {
            tapCounter++;
            if (tapCounter % 3 == 0) {
                tapCounter = 0;

                // use the other logo and invert the setting
                boolean rainbowEnabled = Utils.getSettingBool(this, Const.RAINBOW_MODE, false);
                rainbowEnabled = !rainbowEnabled;
                ImageView tumLogo = findViewById(R.id.startupTumLogo);

                if (rainbowEnabled) {
                    tumLogo.setImageResource(R.drawable.tum_logo_rainbow);
                } else {
                    tumLogo.setImageResource(R.drawable.tum_logo_blue);
                }

                Utils.setSetting(this, Const.RAINBOW_MODE, rainbowEnabled);
            }
        });
        background.setSoundEffectsEnabled(false);
    }

    private void init() {
        // Migrate all settingsPrefix - we somehow ended up having two different shared prefs: join them back together
        Utils.migrateSharedPreferences(this.getApplicationContext());

        // Check that we have a private key setup in order to authenticate this device
        AuthenticationManager am = new AuthenticationManager(this);
        am.generatePrivateKey(null);

        // On first setup show remark that loading could last longer than normally
        runOnUiThread(() -> {
            ContentLoadingProgressBar progressBar = findViewById(R.id.startupLoadingProgressBar);
            progressBar.show();
        });

        // DownloadWorker and listen for finalization
        Flowable<Unit> actionsFlowable = Flowable
                .fromCallable(this::performAllWorkerActions)
                .onErrorReturnItem(Unit.INSTANCE)
                .subscribeOn(Schedulers.io());

        runOnUiThread(() -> LiveDataReactiveStreams.fromPublisher(actionsFlowable)
                .observe(this, downloadCompletionHandler)
        );

        // Start background service and ensure cards are set
        sendBroadcast(new Intent(this, StartSyncReceiver.class));

        // Request Permissions for Android 6.0
        requestLocationPermission();
    }

    private Unit performAllWorkerActions() {
        for (DownloadWorker.Action action : workerActions.getActions()) {
            action.execute(CacheControl.USE_CACHE);
        }
        return Unit.INSTANCE;
    }

    /**
     * Request the Location Permission
     */
    private void requestLocationPermission() {
        //Check, if we already have permission
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            // We already got the permissions, to proceed normally
            openMainActivityIfInitializationFinished();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

            // Display an AlertDialog with an explanation and a button to trigger the request.
            runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_Light_Dialog)
                        .setMessage(R.string.permission_location_explanation)
                        .setPositiveButton(R.string.ok, (dialogInterface, id) -> {
                            ActivityCompat.requestPermissions(
                                    this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
                        })
                        .create();

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(
                            R.drawable.rounded_corners_background);
                }
                dialog.show();
            });
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
        }
    }

    /**
     * Callback when the user allowed or denied Permissions
     * We do not care, if we got the permission or not, since the LocationManager needs to handle
     * missing permissions anyway
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        openMainActivityIfInitializationFinished();
    }

    private void openMainActivityIfInitializationFinished() {
        if (initializationFinished.compareAndSet(false, true) || isFinishing()) {
            // If the initialization process is not yet finished or if the Activity is
            // already being finished, there's no need to open MainActivity.
            return;
        }
        openMainActivity();
    }

    /**
     * Animates the TUM logo into place (left upper corner) and animates background up.
     * Afterwards {@link MainActivity} gets started
     */
    private void openMainActivity() {
        Intent intent = new Intent(this, BaseNavigationActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
