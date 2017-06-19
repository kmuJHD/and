package c5113228.ce.kmu.personalproject2;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Switch sw_exercise, sw_safeMsg, sw_location;
    TextView tv_footCount, tv_length;
    Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // 마시멜로우 버전 이상인 경우 위치정보 권한 따로 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            }
        }

        sw_exercise = (Switch)findViewById(R.id.sw_exercise);
        sw_safeMsg = (Switch)findViewById(R.id.sw_safeMsg);
        sw_location = (Switch)findViewById(R.id.sw_location);
        tv_footCount = (TextView)findViewById(R.id.main_tv_footCount);
        tv_length = (TextView)findViewById(R.id.main_tv_length);
        btn_delete = (Button)findViewById(R.id.main_btn_delete);

        sw_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ExerciseService.class);
                if(((Switch)view).isChecked() && !isMyServiceRunning(ExerciseService.class)){
                    startService(intent);
                }else{
                    stopService(intent);
                }
            }
        });

        sw_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationMgrService.class);
                if(((Switch)view).isChecked() && !isMyServiceRunning(LocationMgrService.class)){
                    startService(intent);
                }else{
                    stopService(intent);
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 데이터 베이스에서 삭제.
                LocationMgrService.LocationDbHelper locationDbHelper = new LocationMgrService.LocationDbHelper(getApplicationContext());
                SQLiteDatabase db = locationDbHelper.getWritableDatabase();

                db.delete(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.TABLE_NAME, null, null);

                try {
                    db.beginTransaction();
                    db.delete(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.TABLE_NAME, null, null);
                    db.setTransactionSuccessful();
                }catch (SQLException e){
                    e.printStackTrace();
                }finally {
                    db.endTransaction();
                }
                db.close();
                locationDbHelper.close();

                updateLocationInfo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 현재 실행중인 경우 스위치 상태 체크됨으로 변경
        if(isMyServiceRunning(ExerciseService.class)){
            sw_exercise.setChecked(true);
        }else{
            sw_exercise.setChecked(false);
        }

        if(isMyServiceRunning(LocationMgrService.class)){
            sw_location.setChecked(true);
        }else{
            sw_location.setChecked(false);
        }

        updateLocationInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()){
            case R.id.main_exercise:
                intent = new Intent(this, ExerciseScheduleActivity.class);
                startActivity(intent);
                break;
            case R.id.main_safeMsg:
                // TODO : 문자전송 관리 화면 작성.
                break;
            case R.id.main_location:
                intent = new Intent(this, LocationMapsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1000:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 권한 취득 성공할 경우 동작 없음
                }else{
                    // 권한 취득 실패시 서비스 실행 불가 상태로 변경
                    sw_location.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "위치정보 체크를 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void updateLocationInfo(){

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

        Location prevloc = new Location("");
        long distance = 0;

        if(c.moveToNext()) {
            // 한개 이상의 위치가 저장되어 있을경우 대입
            prevloc.setLongitude(Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LONG))));
            prevloc.setLatitude(Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LAT))));

            while (c.moveToNext()) {
                Location location = new Location("");
                location.setLongitude(Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LONG))));
                location.setLatitude(Double.parseDouble(c.getString(c.getColumnIndexOrThrow(LocationMgrService.LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LAT))));

                // 이전 위치와 현재 위치의 거리값 계산
                distance += prevloc.distanceTo(location);
                prevloc = location;
            }
        }

        // 걸음 수 = 이동 거리(m) / 보폭(cm)
        int step = ((int)distance * 100) / 75;

        tv_length.setText("총 이동 거리 : " + String.valueOf(distance) + "m");
        tv_footCount.setText("걸음 수 : " + String.valueOf(step) + " 걸음");

        c.close();
        db.close();
        locationDbHelper.close();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        // 서비스가 현재 실행중인지 체크
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
