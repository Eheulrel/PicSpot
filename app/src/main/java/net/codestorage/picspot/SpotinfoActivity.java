package net.codestorage.picspot;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.errorprone.annotations.Var;
import com.google.firebase.firestore.DocumentSnapshot;

import net.codestorage.picspot.Classifier.Classifier;
import net.codestorage.picspot.database.spotinfo.FirestoreSpotInfo;
import net.codestorage.picspot.database.spotinfo.SpotInfoReadSuccessInterface;
import net.codestorage.picspot.database.userinfo.FavoriteDeleteSuccessInterface;
import net.codestorage.picspot.database.userinfo.FavoriteReadSuccessInterface;
import net.codestorage.picspot.database.userinfo.FirestoreUserInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator3;

public class SpotinfoActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final int NUM_PAGES = 3;

    private ViewPager2 viewPager;
    private TextView tvPlaceName, tvPercent, tvDescription, ratingNum, addressInfo;
    private RatingBar ratingBar;
    public ImageButton bookmarkBtn;
    private Button infoBtn;
    private AppCompatButton back, userImage;

    private Classifier cls;

    private FragmentStateAdapter pagerAdapter;

    private FirestoreSpotInfo fssi;
    private FirestoreUserInfo fsui;

    private MapView mapView;
    private GoogleMap mMap;
    private ConstraintLayout descript;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private static Bitmap bitmap;

    private boolean isChecked = true;
    private CircleIndicator3 mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotinfo);

        fssi = new FirestoreSpotInfo(this);
        fsui = new FirestoreUserInfo(this);

        viewPager = findViewById(R.id.myviewpager);
        tvPlaceName = findViewById(R.id.placeName);
        tvPercent = findViewById(R.id.percent);
        tvDescription = findViewById(R.id.placeDescription);
        ratingBar = findViewById(R.id.ratingBar);
        ratingNum = findViewById(R.id.ratingNum);
        mapView = findViewById(R.id.mapView);
        bookmarkBtn = findViewById(R.id.bookmark);
        infoBtn = findViewById(R.id.switchBtn);
        descript = findViewById(R.id.description);
        addressInfo = findViewById(R.id.addressInfo);
        back = findViewById(R.id.back);
        userImage = findViewById(R.id.userImage);


        //텐서플로 모델 초기화
        cls = new Classifier(this);

        try{
            cls.init();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        //사용자가 선택한 이미지 정보 byteArray로 받아와서 bitmap, String 형식으로 변환
        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("bitmap");
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        userImage.setBackgroundDrawable(new BitmapDrawable(bitmap));

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChecked){
                    mapView.setVisibility(View.VISIBLE);
                    descript.setVisibility(View.GONE);
                    isChecked = false;
                    infoBtn.setText("정보보기");
                }else{
                    mapView.setVisibility(View.GONE);
                    descript.setVisibility(View.VISIBLE);
                    isChecked = true;
                    infoBtn.setText("지도보기");
                }
            }
        });

        back.setOnClickListener(view -> finish());

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

        Pair<String, Float> output = cls.classify(bitmap);

        String placeID = output.first;

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG,
                Place.Field.RATING,Place.Field.USER_RATINGS_TOTAL,Place.Field.PHOTO_METADATAS,Place.Field.ADDRESS);
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            if(Variables.userID.equals("1")){
                bookmarkBtn.setVisibility(View.GONE);
            }else{
                bookmarkBtn.setVisibility(View.VISIBLE);
                fsui.read(placeID, new FavoriteReadSuccessInterface() {
                    @Override
                    public void readSuccessMethod(DocumentSnapshot result) {
                        if (result.exists()) {
                            bookmarkBtn.setImageResource(R.drawable.bookmarkyes);
                        } else {
                            bookmarkBtn.setImageResource(R.drawable.bookmarkno);
                        }
                    }
                });
            }

            bookmarkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fsui.read(placeID, new FavoriteReadSuccessInterface() {
                        @Override
                        public void readSuccessMethod(DocumentSnapshot result) {
                            if (!result.exists()) {
                                fsui.set(placeID, place.getName(), BitmapToString(bitmap), () -> {/*do something*/
                                    Toast.makeText(getApplicationContext(), "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                    bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmarkyes));
                                });
                            } else {
                                fsui.delete(placeID, () -> {
                                    Toast.makeText(SpotinfoActivity.this, "즐겨찾기에서 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    bookmarkBtn.setImageResource(R.drawable.bookmarkno);
                                });
                            }
                        }
                    });
                }
            });

            tvPlaceName.setText(place.getName());
            addressInfo.setText("\nAddress : " + place.getAddress());

            ratingBar.setRating(place.getRating().floatValue());
            ratingNum.setText("("+place.getUserRatingsTotal().toString()+")");

            String strPercent = String.format(Locale.ENGLISH, " (정확도 : %.2f%%)", output.second*100);
            tvPercent.setText(strPercent);

            //설명 부분 표시
            //들여쓰기 했음
            fssi.read(placeID, new SpotInfoReadSuccessInterface() {
                @Override
                public void readSuccessMethod(Map<String, Object> result) {
                    tvDescription.setText("  " + result.get(Variables.description).toString());
                }
            });

            pagerAdapter = new ScreenSlidePagerAdapter(this, placeID);
            viewPager.setAdapter(pagerAdapter);

//            // Create a FetchPhotoRequest.
//            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
//                    .setMaxWidth(500) // Optional.
//                    .setMaxHeight(300) // Optional.
//                    .build();
//            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
//                Bitmap bitmap2 = fetchPhotoResponse.getBitmap();
//                imageView.setImageBitmap(bitmap2);
//            }).addOnFailureListener((exception) -> {
//                if (exception instanceof ApiException) {
//                    final ApiException apiException = (ApiException) exception;
//                    Log.e(TAG, "Place not found: " + exception.getMessage());
//                    final int statusCode = apiException.getStatusCode();
//                    // TODO: Handle error with given status code.
//                  }
//            });
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        //Indicator

        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(viewPager);
        mIndicator.createIndicators(NUM_PAGES,0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position%NUM_PAGES);
            }
        });
    }

    //Bitmap -> String
    public static String BitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(this);

        Pair<String, Float> output = cls.classify(bitmap);

        String placeID = output.first;

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG);
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            mMap = googleMap;

            LatLng point = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            mMap.addMarker(new MarkerOptions().position(point).title(place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 12.0f));

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private String placeName;

        public ScreenSlidePagerAdapter(FragmentActivity fa, String placeName) {
            super(fa);
            this.placeName = placeName;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new ScreenSlidePageFragment(position, placeName);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ClassifierActivity.class);
        startActivity(intent);
        finish();
    }

}