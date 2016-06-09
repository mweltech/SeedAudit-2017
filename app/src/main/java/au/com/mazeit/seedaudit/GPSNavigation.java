package au.com.mazeit.seedaudit;

/**
 * Created by mr on 13/03/16.
 */
public class GPSNavigation {

    public class vfOffices {

        private String location;

        private float latitude;

        private float longitute;

//        public VFOffice(String location, String latitude, String longitude) {
//            this.location = location;
//            this.latitude = latitude;
//            this.long=longitude;
//        }
    }

}


//
//    var beaches = [
//            ['Orbost', -37.704023, 148.455213, 4],
//            ['Erica', -37.968199, 146.374601, 5],
//            ['Noojee', -37.889662, 145.996188, 3],
//            ['Powelltown', 17.885241, -77.706949, 2],
//            ['Melbourne CSO', -37.815564, 144.959922, 1],
//            ['Ovens', -36.607240, 146.787029, 6],
//            ['Swifts Creek', -37.262502, 147.723456, 7],
//            ['Bendoc', -37.148042, 148.883903, 8],
//            ['Alexandra', -37.188026, 145.699286, 9],
//            ['Woori Yallock', -37.776886, 145.530452, 9]
//            ];
//
//
///*
//private static Double EARTH_RADIUS = 6371.00; // Radius in Kilometers default
//
//private static final String DEBUG_TAG = "GPS";
//private String[] location;
//private double[] coordinates;
//private double[] gpsOrg;
//private double[] gpsEnd;
//
//private LocationManager lm;
//private LocationListener locationListener;
//
//private double totalDistanceTravel;
//private boolean mPreviewRunning;
//
///** Called when the activity is first created. */
//@Override
//public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.waterspill);
//
//    /*getWindow().setFormat(PixelFormat.TRANSLUCENT);
//    requestWindowFeature(Window.FEATURE_NO_TITLE);
//    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);*/
//
//        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.addCallback(this);
//        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//
//        distanceCal=new LocationUtil(EARTH_RADIUS);
//        totalDistanceTravel=0;
//
//        // ---Additional---
//        //mapView = (MapView) findViewById(R.id.mapview1);
//
//        //mc = mapView.getController();
//        // ----------------
//        txtTimer = (TextView) findViewById(R.id.Timer);
//        gpsOnOff = (TextView) findViewById(R.id.gpsOnOff);
//        disTrav = (TextView) findViewById(R.id.disTrav);
//        startButton = (Button) findViewById(R.id.startButton);
//        startButton.setOnClickListener(startButtonClickListener);
//        stopButton = (Button) findViewById(R.id.stopButton);
//        stopButton.setOnClickListener(stopButtonClickListener);
//        testButton = (Button) findViewById(R.id.testButton);
//        testButton.setOnClickListener(testButtonClickListener);
//
//        startButton.setEnabled(false);
//        stopButton.setEnabled(false);
//
//        getLocation();
//        }
//
//public void getLocation()
//        {
//
//        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        locationListener = new MyLocationListener();
//
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0,locationListener);
//        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,locationListener);
//        }
//
//private OnClickListener startButtonClickListener = new OnClickListener()
//        {
//public void onClick(View v) {
//        // TODO Auto-generated method stub
//        gpsOrg=coordinates;
//        totalDistanceTravel=0;
//        Toast.makeText(getBaseContext(),
//        "Start Location locked : Lat: " + gpsOrg[0] +
//        " Lng: " + gpsOrg[1],
//        Toast.LENGTH_SHORT).show();
//
//        if (!isTimerStarted)
//        {
//        startTimer();
//        isTimerStarted = true;
//        }
//
//        stopButton.setEnabled(true);
//        }
//        };
//private OnClickListener stopButtonClickListener = new OnClickListener()
//        {
//public void onClick(View v) {
//        // TODO Auto-generated method stub
//        gpsEnd=coordinates;
//        //gpsEnd = new double[2];
//        //gpsEnd[0]=1.457899;
//        //gpsEnd[1]=103.828659;
//
//        Toast.makeText(getBaseContext(),
//        "End Location locked : Lat: " + gpsEnd[0] +
//        " Lng: " + gpsEnd[1],
//        Toast.LENGTH_SHORT).show();
//
//        double d = distFrom(gpsOrg[0],gpsOrg[1],gpsEnd[0],gpsEnd[1]);
//        totalDistanceTravel+=d;
//        disTrav.setText(Double.toString(d));
//        }
//        };
//
//public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
//        double earthRadius = EARTH_RADIUS;
//        double dLat = Math.toRadians(lat2-lat1);
//        double dLng = Math.toRadians(lng2-lng1);
//        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
//        Math.sin(dLng/2) * Math.sin(dLng/2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        double dist = earthRadius * c;
//
//        return new Float(dist).floatValue();
//        }
//
//
//public class MyLocationListener implements LocationListener
//{
//    public void onLocationChanged(Location loc) {
//        if(coordinates!=null)
//        {
//            double[] coordinatesPrev=coordinates;
//            double d = distFrom(coordinatesPrev[0],coordinatesPrev[1],coordinates[0],coordinates[1]);
//            totalDistanceTravel+=d;
//        }
//        else
//        {
//            coordinates = getGPS();
//        }
//
//        startButton.setEnabled(true);
//
//    }
//
//    private double[] getGPS() {
//        List<String> providers = lm.getProviders(true);
//
//        double[] gps = new double[2];
//
//        //Loop over the array backwards, and if you get an accurate location, then break out the loop
//
//        Location l = null;
//
//        for (int i=providers.size()-1; i>=0; i--) {
//
//            String s = providers.get(i);
//
//            Log.d("LocServ",String.format("provider (%d) is %s",i,s));
//
//            l = lm.getLastKnownLocation(providers.get(i));
//
//            if (l != null) {
//
//                gps[0] = l.getLatitude();
//                gps[1] = l.getLongitude();
//
//                Log.d("LocServ",String.format("Lat %f, Long %f accuracy=%f",gps[0],gps[1],l.getAccuracy()));
//
//                gpsOnOff.setText("On");
//            }
//        }
//        return gps;
//    }
//
//
//
//        }
