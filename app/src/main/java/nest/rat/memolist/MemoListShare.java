package nest.rat.memolist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

/**
 * Created by mollyjester on 13.01.2017.
 */

public class MemoListShare implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MemoListShare";
    public static final int REQUEST_CODE_GET_FILE = 1;
    public static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private File mFile;

    public MemoListShare(@NonNull Activity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(mActivity, "OnConnected", Toast.LENGTH_SHORT).show();
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public void pickFile() {
        // TODO: build an output file and save it
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        mActivity.startActivityForResult(intent, REQUEST_CODE_GET_FILE);
    }

    public void connect() {
        if (mGoogleApiClient == null) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(mActivity);
            builder.addApi(Drive.API);
            builder.addScope(Drive.SCOPE_FILE);
            builder.addConnectionCallbacks(this);
            builder.addOnConnectionFailedListener(this);
            mGoogleApiClient = builder.build();
        }

        mGoogleApiClient.connect();
        Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show();
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    private void saveFileToDrive() {
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            return;
                        }

                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        FileInputStream fileStream;

                        try {
                            fileStream = new FileInputStream(mFile);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "FileNotFound");
                            return;
                        }

                        Toast.makeText(mActivity, "Start sending file", Toast.LENGTH_SHORT).show();
                        try {
                            byte[] buffer = new byte[1024];
                            int n;

                            while ((n = fileStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, n);
                            }
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }

                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
                        try {
                            Toast.makeText(mActivity, "Starting sending intent", Toast.LENGTH_SHORT).show();
                            mActivity.startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });

        disconnect();
    }

    public void setFile(File mFile) {
        this.mFile = mFile;
    }

    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
}
