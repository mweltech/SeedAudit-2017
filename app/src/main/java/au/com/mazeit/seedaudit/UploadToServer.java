package au.com.mazeit.seedaudit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UploadToServer extends AppCompatActivity {

    SharedPreferences settings;

    int serverResponseCode = 0;
    ProgressDialog dialog = null;

    String upLoadServerUri = null;


    /**********  File Path *************/
    String uploadFilePath;
    String uploadFileName;

    String prefServer;

    Thread syncThread = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.uploadToolbar);
        setSupportActionBar(toolbar);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        uploadFilePath = getFilesDir().getAbsolutePath() + "/";
        uploadFileName = "seedaudits.xml";

        prefServer = getPreferencesString("Server");
        //String prefRemoteDirectory = getDirectory("RemoteDirectory");
        //String prefLocalDirectory = getDirectory("LocalDirectory");
        String prefUsername = getPreferencesString("Username");
        String prefPassword = getPreferencesString("Password");

        /************* Php script path ****************/
        //upLoadServerUri = "http://192.168.1.73/file_upload.php";
        upLoadServerUri = "http://"+prefServer+"/file_upload.php";

        Toast.makeText(UploadToServer.this, "Press the Play button to send your data",
                Toast.LENGTH_LONG).show();

    }

    public boolean btnCancel_Clicked(View v){
        // cancel comms here....
        finish();
        return true;
    }

    public boolean btnGoSync_Clicked(View v){

        dialog = ProgressDialog.show(UploadToServer.this, "", "Starting...", true);

        syncThread = new Thread(new Runnable() {
            public void run() {
                uploadFile(uploadFilePath, uploadFileName);
                downloadFile("http://"+prefServer+"/images/seedlots.csv", getFilesDir().getAbsolutePath(), "seedlots.csv");
                dialog.dismiss();
            }
        });

        syncThread.start();
        return true;
    }

    public int uploadFile(String srcPath,String srcFile) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        //String lineEnd = "\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;


        PostsDatabaseHelper exportDB = PostsDatabaseHelper.getInstance(this);

        exportDB.exportSeedAudits(srcPath,srcFile);

        File sourceFile = new File(srcPath,srcFile);

        if (!sourceFile.isFile()) {

            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + uploadFileName);

            toastIt("Source File not exist :" + uploadFilePath + "" + uploadFileName);
            return 0;
        }
        else
        {
            long size = sourceFile.length();
            dialog.setMessage("Sending audit results...");
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", sourceFile.getName());
                //conn.setRequestProperty("description", "file for upload");

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + sourceFile.getName() + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    toastIt("File Upload Completed.");
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

                dialog.setMessage("...done.");

            } catch (MalformedURLException ex) {

                ex.printStackTrace();

                toastIt("The website " + upLoadServerUri + " does not exist");

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);

            } catch (SocketTimeoutException e) {
                e.printStackTrace();

                toastIt("Can't connect to website " + upLoadServerUri);

            } catch (Exception e) {

                e.printStackTrace();

                toastIt("Problem with website " + upLoadServerUri + " please try again later.");

                Log.e("Upload file Exception", "Exception : "
                        + e.getMessage(), e);
            }
            return serverResponseCode;

        } // End else block
    }

    private String downloadFile(String src,String dstPath,String dstFile){
        try {
            URL url = new URL(src);
            //internalDir = dstPath;
            //internalFile = dstFile;
            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                InputStream stream = urlConnection.getInputStream();
                stream = new DoneHandlerInputStream(stream);
                dialog.setMessage("Fetching new seedlots...");
                readStream(stream,dstPath,dstFile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                dialog.dismiss();
                e.printStackTrace();

                toastIt("Can't connect to website " + upLoadServerUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(urlConnection!=null) urlConnection.disconnect();
            }
        } catch (Exception e) {
            //this.exception = e;
            return "Failed";
        }
        return "Done";

    }

    //private String readStream(InputStream is) throws IOException {
    private void readStream(InputStream is,String dstPath,String dstFile) throws IOException {
        //StringBuilder sb = new StringBuilder();
        PostsDatabaseHelper importDB = PostsDatabaseHelper.getInstance(this);
        byte[] buf = new byte[1024];
        BufferedInputStream r = new BufferedInputStream(is);
        //BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        File file = new File(dstPath, dstFile);
        BufferedOutputStream outbuf = null;
        try {
            outbuf = new BufferedOutputStream(new FileOutputStream(file));
            int length=r.read(buf);
            while(length!=-1) {
                outbuf.write(buf,0,length);
                length = r.read(buf);
            }
            outbuf.close();

            toastIt("File downloaded - importing... ");

            dialog.setMessage("...now importing seedlots...");

            importDB.importSeedlotsCSV(dstPath, dstFile);

            dialog.setMessage("...finished importing.");

            toastIt("...done. ");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outbuf != null) {
                outbuf.close();
            }
        }
        is.close();
    }

    /**
     * This input stream won't read() after the underlying stream is exhausted.
     * http://code.google.com/p/android/issues/detail?id=14562
     */
    final class DoneHandlerInputStream extends FilterInputStream {
        private boolean done;

        public DoneHandlerInputStream(InputStream stream) {
            super(stream);
        }

        @Override public int read(byte[] bytes, int offset, int count) throws IOException {
            if (!done) {
                int result = super.read(bytes, offset, count);
                if (result != -1) {
                    return result;
                }
            }
            done = true;
            return -1;
        }
    }

    private void toastIt(String msg) {
        final String toastMsg = msg;
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(UploadToServer.this, toastMsg,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String getPreferencesString(String prefName) {
        //SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String prefString = settings.getString(prefName,"");
        return prefString;
    }

}
