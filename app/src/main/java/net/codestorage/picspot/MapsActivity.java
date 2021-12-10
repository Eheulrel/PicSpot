package net.codestorage.picspot;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.codestorage.picspot.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double lat = getIntent().getDoubleExtra("getLat",0);
        double lon = getIntent().getDoubleExtra("getLon",0);
        String locName = getIntent().getStringExtra("getName");
        // Add a marker and move the camera
        LatLng point = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(point).title(locName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f));

        //지도유형
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }
}