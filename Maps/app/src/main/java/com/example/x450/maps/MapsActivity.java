package com.example.x450.maps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener,SensorEventListener,ResponseHandlerInterface {

    private GoogleMap gMap;
    private RW rw;
    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private JSONObject req_loc;
    private String longitude;
    private String latitude;
    private String token;
    private Marker mark;
    private String currLong;
    private String currLat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("in","on create");
        rw = new RW();
        req_loc = new JSONObject();
        try {
            req_loc.put("com","req_loc");
            req_loc.put("nim","13513087");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mPointer = (ImageView) findViewById(R.id.compass);
        rw.generateNoteOnSD(this,"Client: "+req_loc.toString());
        new AsyncAction(this,this).execute(req_loc.toString());

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
            Double getlat =location.getLatitude();
            currLat = getlat.toString();
            Double getlong = location.getLongitude();
            currLong = getlong.toString();
            Log.d("latitude", currLat);
            Log.d("longitude",currLong);
        }

        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        gMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {

        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        currLat = latitude.toString();
        currLong = longitude.toString();
        LatLng latLng = new LatLng(latitude, longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    public void toSubmit(View view){
        Intent intent = new Intent(this,Submit.class);
        intent.putExtra("longitude", longitude);
        Log.d("currLong", currLong);
        Log.d("currLat",currLat);
        intent.putExtra("latitude", latitude);
        intent.putExtra("token", token);
        startActivityForResult(intent,1);
    }

    public void toCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(intent);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);

            ra.setFillAfter(true);

            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResponse(JSONObject result) {
        try {
            this.longitude=result.getString("longitude");
            this.latitude=result.getString("latitude");
            this.token = result.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast t = Toast.makeText(this,result.toString(), Toast.LENGTH_LONG);
        t.show();
        rw.generateNoteOnSD(this,"Server: " + result.toString());
        double lat = Double.parseDouble(latitude);
        double longi = Double.parseDouble(longitude);
        LatLng latlongi = new LatLng(lat,longi);
        mark = gMap.addMarker(new MarkerOptions().position(latlongi).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

//        gMap.addMarker(new MarkerOptions().position(latlongi).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String status = null;
        Log.d("after","Submit");
        if (requestCode == 1) {
            String nextLong="";
            String nextLat="";
            if(resultCode == Activity.RESULT_OK){
                status = data.getStringExtra("status");
                if (status.equals("ok")){
                    nextLong=data.getStringExtra("nextLong");
                    nextLat=data.getStringExtra("nextLat");
                    longitude = nextLong;
                    latitude = nextLat;
                }
                String tokenn=data.getStringExtra("token");
                token = tokenn;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
            if (status.equals("ok")){
                double longi = Double.parseDouble(nextLong);
                double lat= Double.parseDouble(nextLat);
                LatLng latlongi = new LatLng(lat,longi);
                Log.d("its", "ok");
                mark.remove();
                mark = gMap.addMarker(new MarkerOptions().position(latlongi).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }
            else{
                Log.d("its", "finish");
                mark.remove();
            }


        }
    }//onActivityResult

}
