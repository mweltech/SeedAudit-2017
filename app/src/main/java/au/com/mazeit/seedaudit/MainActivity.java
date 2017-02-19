package au.com.mazeit.seedaudit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Seedlot currentSeedlot;
    //Float numGross,numBarrel,numNett;
    Double numGross,numBarrel,numNett;
    Boolean verified;
    String changedBy;
    Date changedDate;

    private Intent upload_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentSeedlot = new Seedlot();

        currentSeedlot.slnId = -1;

        //upload_activity = new Intent(this, UploadToServer.class);
        upload_activity = new Intent(this, GoogleDrive.class);
        //upload_activity = new Intent(this, GoogleDriveREST.class);

        int j = 0; j++;

        PostsDatabaseHelper helper = PostsDatabaseHelper.getInstance(this);

        final EditText grossWeight = (EditText) findViewById(R.id.editTextGross);
        final EditText barrelWeight = (EditText)findViewById(R.id.editTextBarrel);
        final TextView nettCalc = (TextView)findViewById(R.id.txtNetCalc);

        TextWatcher doNett = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String sGrossWeight = grossWeight.getText().toString();
                String sBarrelWeight = barrelWeight.getText().toString();

                if (!sGrossWeight.isEmpty() && !sBarrelWeight.isEmpty()) {
                    //numGross = Float.parseFloat(sGrossWeight);
                    numGross = Double.parseDouble(sGrossWeight);
                    //numBarrel = Float.parseFloat(sBarrelWeight);
                    numBarrel = Double.parseDouble(sBarrelWeight);
                    numNett = round(numGross - numBarrel,2);
                    //nettCalc.setText(Float.toString(numNett));

                    nettCalc.setText(Double.toString(numNett));
                }
            }
        };

        grossWeight.addTextChangedListener(doNett);
        barrelWeight.addTextChangedListener(doNett);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void btnSave_Clicked(View v){

        if(currentSeedlot.slnId==-1) {
            Toast.makeText(this, "Please scan a seedlot before saving.",
                    Toast.LENGTH_SHORT).show();

        } else {

            EditText txtgrossWeight = (EditText) findViewById(R.id.editTextGross);
            EditText txtbarrelWeight = (EditText) findViewById(R.id.editTextBarrel);
            TextView txtnettCalc = (TextView) findViewById(R.id.txtNetCalc);

            CheckBox cbverified = (CheckBox) findViewById(R.id.cbVerified);
            if(cbverified.isChecked()) {
                verified = Boolean.TRUE;
            }
            else {
                verified = Boolean.FALSE;
            }
            //String sVerified = cbverified.getText().toString();
            //verified = Boolean.parseBoolean(sVerified);


            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss");
            Calendar cal = Calendar.getInstance();

            SeedAudit record = new SeedAudit();
            record.slnId = currentSeedlot.slnId;
            record.locationId = currentSeedlot.office;
            record.gross = numGross;
            record.barrelWgt = numBarrel;
            record.nett = numNett;
            record.verified = verified;
            record.changedBy = "mr01";
            record.ChangedDate = dateFormat.format(cal.getTime());


            PostsDatabaseHelper helper = PostsDatabaseHelper.getInstance(this);

            helper.addSeedAudit(record);

            currentSeedlot.clear();

            txtgrossWeight.setText("");
            txtbarrelWeight.setText("");
            txtnettCalc.setText("");
            //cbverified.setActivated(false);
            cbverified.setChecked(false);
            setSeedlot("","");
            txtgrossWeight.requestFocus();

        }
    }


    public boolean btnScan_Clicked(View v){
        //https://github.com/journeyapps/zxing-android-embedded/

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();

        return true;
    }

    public boolean btnSync_Clicked(View v){
        startActivity(upload_activity);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            String s = scanResult.getContents();
            if(s==null || s.length()<=0) {
                Toast.makeText(this, "Scan did not work - please try again.",
                        Toast.LENGTH_SHORT).show();
            }
            else {

                PostsDatabaseHelper helper = PostsDatabaseHelper.getInstance(this);
                currentSeedlot = new Seedlot();
                currentSeedlot = helper.getSeedlot(s);
                if(currentSeedlot.slnId==-1) {
                    Toast.makeText(this, "The barcode "+s+" is not a valid location - please try again.",
                            Toast.LENGTH_LONG).show();

                }
                else {
                    setSeedlot(currentSeedlot.number, currentSeedlot.name);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    EditText txtgrossWeight = (EditText) findViewById(R.id.editTextGross);
                    txtgrossWeight.requestFocus();

                }
            }
        }
    }

    public void setSeedlot(String number,String name) {
        TextView txtSeedlotScannedNumber = (TextView) findViewById(R.id.txtSeedlotScannedNumber);
        txtSeedlotScannedNumber.setText(number);
        if(name.length()>0) {
            name = " / " + name;
        }
        TextView txtSeedlotScannedName = (TextView) findViewById(R.id.txtSeedlotScannedName);
        txtSeedlotScannedName.setText(name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,Settings.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
