package au.com.mazeit.seedaudit;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by murraywalker on 7/06/2016.
 */
//public class GoogleDrive extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
public class GoogleDrive extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

/**
 * upload a text file from device to Drive */

    private static final String auditResultsFileName = "seedaudits";
    private static final String auditResultsFileNameExt = ".csv";

    private static final String TAG = "upload_file";
    private static final int REQUEST_CODE = 101;
    private File uploadFile;
    private GoogleApiClient googleApiClient;
    public static String drive_id;
    public static DriveId driveID;

    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_upload_to_server);

        setContentView(R.layout.activity_upload_to_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.uploadToolbar);
        setSupportActionBar(toolbar);


        Log.i(TAG, "Legal requirements if you use Google Drive in your app: " + GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));

        //the text file in our device's Download folder

        //textFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "test.txt");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        Date now = new Date();

        String fileDateTime = sdf.format(now).toString();

        uploadFile = new File(getFilesDir().getAbsolutePath() + File.separator + auditResultsFileName + "-" + fileDateTime + auditResultsFileNameExt);

        //PostsDatabaseHelper exportDB = PostsDatabaseHelper.getInstance(this);

        //exportDB.exportSeedAudits(uploadFile.getParent() + File.separator,auditResultsFileName);


    /*build the api client*/
        //buildGoogleApiClient();
    }

    public boolean btnGoSync_Clicked(View v){

        /*build the api client*/

        Toast.makeText(GoogleDrive.this,
                "Exporting data...", Toast.LENGTH_SHORT).show();


        PostsDatabaseHelper exportDB = PostsDatabaseHelper.getInstance(this);

        //exportDB.exportSeedAudits(uploadFile.getParent() + File.separator, uploadFile.getName());
        exportDB.exportSeedAuditsCSV(uploadFile.getParent() + File.separator, uploadFile.getName());

        buildGoogleApiClient();

        Log.i(TAG, "In onStart() - connecting...");
        googleApiClient.connect();

        return true;
    }

    public boolean btnCancel_Clicked(View v){
        // cancel comms here....
        finish();
        return true;
    }

    /*connect client to Google Play Services*/
    @Override
    protected void onStart() {
        super.onStart();
        //Log.i(TAG, "In onStart() - connecting...");
        //googleApiClient.connect();
    }

    /*close connection to Google Play Services*/
    @Override
    protected void onStop() {
        super.onStop();
        //if (googleApiClient != null) {
        //    Log.i(TAG, "In onStop() - disConnecting...");
        //    googleApiClient.disconnect();
        //}
    }

    /*Handles onConnectionFailed callbacks*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mResolvingError=false;
            Log.i(TAG, "In onActivityResult() - connecting..."); googleApiClient.connect();
        }
    }

    /*handles connection callbacks*/
    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "in onConnected() - we're connected, let's do the work in the background...");
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(uploadFileCallback);

        //Drive.DriveApi.getRootFolder(googleApiClient).listChildren(googleApiClient).setResultCallback(listFolderCallback);
        //Drive.DriveApi.getAppFolder(googleApiClient).listChildren(googleApiClient).setResultCallback(listFolderCallback);

    }

    /*handles suspended connection callbacks*/
    @Override
    public void onConnectionSuspended(int cause) {
        switch (cause) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> listFolderCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                    Log.i(TAG,"got root folder");
                    MetadataBuffer buffer = metadataBufferResult.getMetadataBuffer();
                    Log.i(TAG,"Buffer count  " + buffer.getCount());
                    for(Metadata m : buffer) {
                        Log.i(TAG,"Metadata name  " + m.getTitle() + "(" + (m.isFolder() ? "folder" : "file") + ")");
                        String filename = m.getTitle();
                        if (!m.isFolder() && m.getTitle().equals("seedlots.csv")) {
                            //Drive.DriveApi.getFolder(googleApiClient, m.getDriveId())
                            //        .listChildren(googleApiClient)
                            //        .setResultCallback(fileDownloadCallback);
                            Drive.DriveApi.fetchDriveId(googleApiClient, m.getDriveId().toString()).setResultCallback(downloadFileCallback);
                        }
                    }
                }
            };

    /*callback on getting the drive id, contained in result*/
    final private ResultCallback<DriveApi.DriveIdResult> downloadFileCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {

            DriveFile file = Drive.DriveApi.getFile(googleApiClient, result.getDriveId());
            /*use a pending result to get the file contents */
            PendingResult<DriveContentsResult> pendingResult = file.open(googleApiClient, DriveFile.MODE_READ_ONLY, null);
                /*the callback receives the contents in the result*/
                pendingResult.setResultCallback(new ResultCallback<DriveContentsResult>() {
                    public String fileAsString;
                    @Override
                    public void onResult(DriveContentsResult result) {
                        DriveContents fileContents = result.getDriveContents();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(fileContents.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String oneLine;
                        Log.i(TAG, "reading input stream and building string...");
                        try {
                            while ((oneLine = reader.readLine()) != null) {
                                builder.append(oneLine);
                            }
                            fileAsString = builder.toString();
                        } catch (IOException e) {
                            Log.e(TAG, "IOException while reading from the stream", e);
                        }
                        fileContents.discard(googleApiClient);
                        //Intent intent = new Intent(RetrieveContentsActivity.this, DisplayFileActivity.class);
                        //intent.putExtra("text", fileAsString);
                        //startActivity(intent);
                    }
            });
        }
    };



    /*callback on getting the drive contents, contained in result*/
    final private ResultCallback<DriveContentsResult> uploadFileCallback = new ResultCallback<DriveContentsResult>() {

        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.i(TAG, "Error creating new file contents"); return;
            }

            final DriveContents driveContents = result.getDriveContents();

            new Thread() {
                @Override
                public void run() {
                    OutputStream outputStream = driveContents.getOutputStream(); addTextfileToOutputStream(outputStream);
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder() .setTitle(uploadFile.getName())
                            .setMimeType("text/plain")
                            .setDescription("This is a text file uploaded from device") .setStarred(true).build();
                    Drive.DriveApi.getRootFolder(googleApiClient) .createFile(googleApiClient, changeSet, driveContents) .setResultCallback(fileCallback);
                }
            }.start(); }
        };

    /*get input stream from text file, read it and put into the output stream*/
    private void addTextfileToOutputStream(OutputStream outputStream) {
        Log.i(TAG, "adding text file to outputstream..."); byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream( new FileInputStream(uploadFile));
            while ((bytesRead = inputStream.read(buffer)) != -1) { outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.i(TAG, "problem converting input stream to output stream: " + e);
            e.printStackTrace(); }
    }

    /*callback after creating the file, can get file info out of the result*/
    final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
            @Override
            public void onResult(DriveFileResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.i(TAG, "Error creating the file"); Toast.makeText(GoogleDrive.this,
                        "Error adding file to Drive", Toast.LENGTH_SHORT).show(); return;
                }

                Log.i(TAG, "File added to Drive");
                Log.i(TAG, "Created a file with content: "
                        + result.getDriveFile().getDriveId());
                Toast.makeText(GoogleDrive.this,
                        "File successfully added to Drive", Toast.LENGTH_SHORT).show();

                final PendingResult<DriveResource.MetadataResult> metadata = result.getDriveFile().getMetadata(googleApiClient);
                metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                    @Override
                    public void onResult(DriveResource.MetadataResult metadataResult) {
                        Metadata data = metadataResult.getMetadata(); Log.i(TAG, "Title: " + data.getTitle());
                        drive_id = data.getDriveId().encodeToString(); Log.i(TAG, "DrivId: " + drive_id);
                        driveID = data.getDriveId();
                        Log.i(TAG, "Description: " + data.getDescription().toString()); Log.i(TAG, "MimeType: " + data.getMimeType());
                        Log.i(TAG, "File size: " + String.valueOf(data.getFileSize()));
                    }
                });
            }
        };


        /*callback when there there's an error connecting the client to the service.*/
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            if(mResolvingError) {
                return;
            }
            Log.i(TAG, "Connection failed");
            if (!result.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
                return;
            }
            try {
                Log.i(TAG, "trying to resolve the Connection failed error...");
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_CODE);
                //result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            }

        }

        /*build the google api client*/
        private void buildGoogleApiClient(){
            if(googleApiClient==null){
                googleApiClient=new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        }
            }
        }
