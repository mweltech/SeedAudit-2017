package au.com.mazeit.seedaudit;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by maw on 19/04/2016.
 */
public class RetrieveURLTask extends AsyncTask<String, Integer, String> {
    private Exception exception;
    private String internalDir;
    private String internalFile;
    private PostsDatabaseHelper importDB;

    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            internalDir = urls[1];
            internalFile = urls[2];
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                InputStream stream = urlConnection.getInputStream();
                stream = new DoneHandlerInputStream(stream);
                readStream(stream);
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

    protected void onPostExecute() {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    //private String readStream(InputStream is) throws IOException {
    private void readStream(InputStream is) throws IOException {
        //StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[1024];
        BufferedInputStream r = new BufferedInputStream(is);
        //BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        File file = new File(internalDir, internalFile);
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
            importDB.importSeedlots(internalDir,internalFile);
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
