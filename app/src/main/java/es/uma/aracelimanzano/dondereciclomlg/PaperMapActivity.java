package es.uma.aracelimanzano.dondereciclomlg;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;

public class PaperMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int REQUEST_LOCATION_PER = 1;
    public static final String PREF_NAME = "GPS";
    public static final String LAT_NAME = "Latitude";
    public static final String LNG_NAME = "Longitude";

    GoogleMap paperMap = null;
    ArrayList<RecPoint> recyclePoints;
    LocationListener locationListener;
    LocationManager locationManager;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_map);
        SharedPreferences settings = getSharedPreferences(PREF_NAME, 0);
        String latString = settings.getString(LAT_NAME,"");
        String lngString = settings.getString(LNG_NAME,"");

        currentLocation = new Location("Default");
        currentLocation.setLatitude(36.71853911463124);
        currentLocation.setLongitude(-4.496980905532837);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListenerRecPoint();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PER);
        } else {

            Location locationNetwork =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationNetwork != null)
                currentLocation = locationNetwork;
        }

        recyclePoints = MainActivity.recyclePointsPaper;

        MapFragment paperMap = (MapFragment) getFragmentManager().findFragmentById(R.id.paperMap);
        paperMap.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        paperMap = googleMap;

        for (RecPoint rp : recyclePoints) {

            LatLng rpLatLng = new LatLng(rp.getLatitude(), rp.getLongitude());
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_bin);
            paperMap.addMarker(new MarkerOptions()
                    .title("ID " + rp.getIDString())
                    .snippet("Tipo " + rp.getType() + " Volumen " + rp.getVolume() + " Cantidad " + rp.getQuantity())
                    .position(rpLatLng)
                    .icon(icon));
        }

        int zoom = 17;

        Log.d("UNO",recyclePoints.get(0).getType());

        moveCamera(recyclePoints.get(0).getLatitude(), recyclePoints.get(0).getLongitude(), zoom);

    }


    private void moveCamera(double lat, double lng, int zoom) {

        LatLng point = new LatLng(lat, lng);
        paperMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));
    }

    private Location convert(RecPoint rp) {

        Location rpLocation = new Location(rp.getIDString());

        rpLocation.setLongitude(rp.getLongitude());
        rpLocation.setLatitude(rp.getLatitude());

        return rpLocation;
    }

    public void onClick(View v) {

        if (v.getId() == R.id.buttonNearest) {


            float distance = Float.MAX_VALUE, aux;
            int c = 0, selected = -1;
            RecPoint selectedRecyclePoint;
            Location rpLocation;

            for (RecPoint rp : recyclePoints) {

                rpLocation = convert(rp);
                aux = currentLocation.distanceTo(rpLocation);

                if (aux < distance) {
                    distance = aux;
                    selected = c;
                }

                c++;
            }

            RecPoint select = recyclePoints.get(selected);

            if (paperMap != null) {
                int zoom = 17;
                moveCamera(select.getLatitude(), select.getLongitude(), zoom);
            }
        }
    }

    protected void onStop(){
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREF_NAME,0);
        SharedPreferences.Editor editor = settings.edit();

        for (RecPoint rp: recyclePoints) {
            editor.putString("",rp.getIDString());
        }

        editor.putString(LAT_NAME,Double.toString(currentLocation.getLatitude()));
        editor.putString(LNG_NAME,Double.toString(currentLocation.getLongitude()));

        editor.commit();
    }

    private class LocationListenerRecPoint implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            currentLocation = location;

            Toast.makeText(getApplicationContext(),
                    "Location has changed: " + location.getLatitude() + " " + location.getLongitude(),
                    Toast.LENGTH_SHORT).show();

            SharedPreferences settings = getSharedPreferences(PREF_NAME,0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString(LAT_NAME,Double.toString(currentLocation.getLatitude()));
            editor.putString(LNG_NAME,Double.toString(currentLocation.getLongitude()));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }

    public void onRequestPermissionsResult(int requestCode, String permisssions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PER:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Permisos Concedidos",Toast.LENGTH_SHORT).show();

                    long minTime      = 5000;  //5 sg
                    float minDistance = 1000;  //1 km

                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(),
                                "Error", Toast.LENGTH_SHORT).show();

                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_LOCATION_PER);
                    }

                    else {

                        Location locationNetwork =
                                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (locationNetwork!=null)
                            currentLocation = locationNetwork;


                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                minTime,
                                minDistance,
                                locationListener);
                    }
                }

                break;
        }

    }

}
