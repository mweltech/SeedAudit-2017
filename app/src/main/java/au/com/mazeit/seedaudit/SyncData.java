package au.com.mazeit.seedaudit;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import java.net.URL;

/**
 * Created by maw on 19/04/2016.
 */
public class SyncData extends AsyncTask<String, Integer, String> {
    private Exception exception;
    private PostsDatabaseHelper importDB;

    protected String doInBackground(String... urls) {
        String result;
        uploadData(urls[3],urls[4],urls[5]);
        result=downloadData(urls[0],urls[1],urls[2]);
        return result;
    }

    protected void onPostExecute() {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    public int uploadData(String uploadUri,String srcPath,String srcFile) {
        int serverResponseCode = 0;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        //String lineEnd = "\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(srcPath,srcFile);

        if (!sourceFile.isFile()) {

            return 0;

        }
        else
        {
            long size = sourceFile.length();
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(uploadUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
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

                            //Toast.makeText(UploadToServer.this, "File Upload Complete.",
                            //        Toast.LENGTH_SHORT).show();
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                //        Toast.makeText(UploadToServer.this, "MalformedURLException",
                //                Toast.LENGTH_SHORT).show();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
                //Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                //                Toast.LENGTH_SHORT).show();
                Log.e("Upload file Exception", "Exception : "
                        + e.getMessage(), e);
            }
            return serverResponseCode;

        } // End else block
    }

    private String downloadData(String src,String dstPath,String dstFile){
        try {
            URL url = new URL(src);
            //internalDir = dstPath;
            //internalFile = dstFile;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                InputStream stream = urlConnection.getInputStream();
                stream = new DoneHandlerInputStream(stream);
                readStream(stream,dstPath,dstFile);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(urlConnection!=null) urlConnection.disconnect();
            }
        } catch (Exception e) {
            this.exception = e;
            return "Failed";
        }
        return "Done";

    }

    //private String readStream(InputStream is) throws IOException {
    private void readStream(InputStream is,String dstPath,String dstFile) throws IOException {
        //StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[1024];
        BufferedInputStream r = new BufferedInputStream(is);
        //BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        File file = new File(dstPath, dstFile);
        BufferedOutputStream outbuf = null;
        try {
            outbuf = new BufferedOutputStream(new FileOutputStream(file));
            //for (String line = r.readLine(); line != null; line = r.readLine()) {
            int length=r.read(buf);
            while(length!=-1) {
                //sb.append(line);
                outbuf.write(buf,0,length);
                length=r.read(buf);
            }
            outbuf.close();
            importDB.importSeedlots(dstPath,dstFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outbuf != null) {
                outbuf.close();
            }
        }
        is.close();
        //return sb.toString();
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

    public void setDatabase(PostsDatabaseHelper db) {
        importDB=db;
    }
}
