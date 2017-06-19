package c5113228.ce.kmu.personalproject2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class LocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLngBounds.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * 구글맵이 준비 된 경우 호출됨
     * @param googleMap 준비된 구글맵 객체
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);

        mMap = googleMap;

        // 데이터베이스에서 위치 기록 조회
        LocationMgrService.LocationDbHelper locationDbHelper = new LocationMgrService.LocationDbHelper(getApplicationContext());
        SQLiteDatabase db = locationDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LocationMgrService.LocationDbHelper.LocationContract.FeedEntry._ID,
                LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_DATE,
                LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LONG,
                LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LAT
        };

        Cursor c = db.query(
                LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                      // The columns to return
                null,                            // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                            // don't group the rows
                null,                            // don't filter by row groups
                null                             // The sort order
        );

        ArrayList<LatLng> points = new ArrayList<>();
        PolylineOptions lineOptions = new PolylineOptions();
        builder = new LatLngBounds.Builder();

        double longitude = 0;
        double latitude = 0;
        while (c.moveToNext()) {
            longitude = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LONG)));
            latitude = Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LAT)));
            LatLng location = new LatLng(latitude, longitude);

            // 위치 목록에 추가
            points.add(location);
            builder.include(location);

            // 마커 추가
            mMap.addMarker(new MarkerOptions().position(location));
        }
        c.close();
        db.close();
        locationDbHelper.close();

        // 모든 위치 lineOptions 에 추가하여 그리기.
        lineOptions.addAll(points);
        lineOptions.width(2);
        lineOptions.color(Color.RED);
        mMap.addPolyline(lineOptions);

        // 모든 위치가 보일 수 있도록 화면 확대
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        });

    }
}
