package se.sugarest.jane.activityrecognition;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG_MAINACTIVITY = MainActivity.class.getSimpleName();

    private TextView mDetectedActivitesTextView;
    private Button mRequestUpdatesButton;
    private Button mRemoveUpdatesButton;

    protected GoogleApiClient mGoogleApiClient;
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetectedActivitesTextView = (TextView) findViewById(R.id.detectedActivities);
        mRequestUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_activity_updates_button);

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        buildGoogleApiClinet();

    }

    protected synchronized void buildGoogleApiClinet() {
        // Create and setup GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the Client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect the client
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Once the GoogleApiClient instance has connected, onConnected() is called. When this happens,
     * you need to create a PendingIntent that goes to the IntentService you created earlier, and
     * pass it to the ActivityRecognitionApi.
     * You also need to set an interval for how often the API should check the user's activity.
     * For this sample application, we use a value of 3000, or three seconds, though in an actual
     * application you may want to  check less frequently to conserve power.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 3000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG_MAINACTIVITY, "GoogleApiClient connection has been suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG_MAINACTIVITY, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * The Receiver Class
     * <p>
     * Class that extends BroadcastReceiver
     * Works best as a nested class on MainActivity
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {

        private final String TAG_RECEIVER = ActivityDetectionBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.ACTIVITY_EXTRA)) {
                ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);

                String stringStatus = "";
                for (DetectedActivity activity : detectedActivities) {
                    int activityTypeInt = activity.getType();
                    String activityTypeString = convertActivityIntToString(activityTypeInt);
                    int activityConfidence = activity.getConfidence();
                    stringStatus += activityTypeString + activityConfidence + "%\n";
                }
                Log.i(TAG_RECEIVER, stringStatus);
                mDetectedActivitesTextView.setText(stringStatus);
            }
        }
    }

    private String convertActivityIntToString(int activityTypeInt) {
        switch (activityTypeInt) {
            case DetectedActivity.IN_VEHICLE:
                return getResources().getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return getResources().getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return getResources().getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return getResources().getString(R.string.running);
            case DetectedActivity.STILL:
                return getResources().getString(R.string.still);
            case DetectedActivity.TILTING:
                return getResources().getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return getResources().getString(R.string.walking);
            case DetectedActivity.UNKNOWN:
                return getResources().getString(R.string.unknown);
            default:
                return getResources().getString(R.string.unidentifiable_activity);
        }
    }
}
