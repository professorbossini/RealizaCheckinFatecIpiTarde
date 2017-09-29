package br.com.bossini.realizacheckinfatecipitarde;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUISICAO_PELA_CAMERA = 1;
    private static final int REQUISICAO_PERMISSAO_GPS = 2;
    private Location localizacaoAtual;
    private LatLng localizacaoAtualLatLng;
    private LocationManager locationManager;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            localizacaoAtual = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager manager =  getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)manager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String []{Manifest.permission.ACCESS_FINE_LOCATION}, REQUISICAO_PERMISSAO_GPS);
        }
        else
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUISICAO_PERMISSAO_GPS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            else
                Toast.makeText(this, getString(R.string.explicacao_gps), Toast.LENGTH_SHORT).show();
        }
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    public void realizarCheckin (View v){
        if (mMap == null){
            Toast.makeText(this, getString(R.string.mapa_nao_esta_pronto), Toast.LENGTH_SHORT).show();
        }
        else{
            if (localizacaoAtual == null){
                Toast.makeText(this, getString(R.string.sem_gps), Toast.LENGTH_SHORT).show();
                localizacaoAtualLatLng = new LatLng(-23.5631338, -46.6543286);
            }
            else{
                double latitude = localizacaoAtual.getLatitude();
                double longitude = localizacaoAtual.getLongitude();
                localizacaoAtualLatLng = new LatLng( latitude, longitude);
            }
            tirarFoto();
        }
    }

    private void tirarFoto (){
        Intent i = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, REQUISICAO_PELA_CAMERA);

        }
        else
            Toast.makeText(this, getString(R.string.sem_app_para_foto), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUISICAO_PELA_CAMERA:
                if (resultCode == Activity.RESULT_OK){
                    Bitmap foto = (Bitmap)data.getExtras().get ("data");
                    mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(foto)).position(localizacaoAtualLatLng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(localizacaoAtualLatLng));
                }
                else
                    Toast.makeText(this, getString(R.string.sem_foto_sem_checkin), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
