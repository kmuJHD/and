package c5113228.ce.kmu.personalproject2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

public class LocationMgrService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public LocationMgrService() {
    }

    private static final String TAG = LocationMgrService.class.getSimpleName();
    private Handler mServiceHandler;
    private GoogleApiClient mGoogleApiClient;   // Google API 진입점
    private Location mLocation;                 // 현재 위치
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 5113228;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * 위치정보 업데이트 주기. 단, 해당 수치보다 빠르거나 느릴 수 있음.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * 위치정보 업데이트 최고 속도. 이 수치보다는 절대 빨라지지 않음.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        createLocationRequest();

        // 서비스를 실행할 스레드 생성. 스레드를 사용하지 않으면 서비스가 메인 프로세스에서 돌아감
        HandlerThread thread = new HandlerThread("LocationMgrServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceHandler = new Handler(thread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started - LocationMgr ", Toast.LENGTH_SHORT).show();

        // startForeground 를 실행하지 않을 경우 프로세스 강제 종료시 서비스도 같이 종료됨
        startForeground(NOTIFICATION_ID, getNotification());

        // LocationMgrService 는 실제 처리를 onLocationChange 에서 처리하므로 커스텀 핸들러는 제외

        // 종료될경우 다시 시작하도록 값 설정
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Removing location updates");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocationMgrService.this);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
        mServiceHandler.removeCallbacksAndMessages(null);
        mGoogleApiClient.disconnect();


        Toast.makeText(this, "Service Destroyed - LocationMgr ", Toast.LENGTH_SHORT).show();
    }

    /**
     *  foreground service 용 NotificationCompat 반환
     */
    private Notification getNotification() {

        CharSequence text = "위치 정보 갱신 기능이 사용중입니다";

        return new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle("PersonalProject2")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // 구글 API 접속시
        Log.i(TAG, "GoogleApiClient connected");
        try {
            // 테스트용 현재위치 받아오기
//            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // LocationServices 등록
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, LocationMgrService.this);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "GoogleApiClient connection failed.");
    }

    /**
     * 위치 변경시 실제 서비스 처리 부분.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        // 알수 없는 장소인 경우 저장하지 않음
        if(mLocation == null) return;

        // 데이터 베이스에 위치 추가.
        LocationDbHelper locationDbHelper = new LocationDbHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = locationDbHelper.getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_DATE, DateFormat.getDateTimeInstance().format(new Date()));
            contentValues.put(LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LONG, mLocation.getLongitude());
            contentValues.put(LocationDbHelper.LocationContract.FeedEntry.COLUMN_NAME_LAT, mLocation.getLatitude());

            sqLiteDatabase.beginTransaction();          // 빠른 결과 처리를 위한 트렌젝션 처리
            sqLiteDatabase.insert(LocationDbHelper.LocationContract.FeedEntry.TABLE_NAME, null, contentValues);
            sqLiteDatabase.setTransactionSuccessful();
        }catch(SQLException e){
            e.printStackTrace();
        }finally {
            sqLiteDatabase.endTransaction();
        }
        sqLiteDatabase.close();
        locationDbHelper.close();
    }

    /**
     * location request 파라메터 설정.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static class LocationDbHelper extends SQLiteOpenHelper {
        public static final class LocationContract{
            // To prevent someone from accidentally instantiating the contract class,
            // make the constructor private.
            private LocationContract() { }

            /* Inner class that defines the table contents */
            public static class FeedEntry implements BaseColumns {
                public static final String TABLE_NAME = "entry";
                public static final String COLUMN_NAME_DATE = "loc_date";
                public static final String COLUMN_NAME_LONG = "loc_long";
                public static final String COLUMN_NAME_LAT = "loc_lat";
            }
        }
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Location.db";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + LocationContract.FeedEntry.TABLE_NAME + " (" +
                        LocationContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        LocationContract.FeedEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                        LocationContract.FeedEntry.COLUMN_NAME_LONG + TEXT_TYPE + COMMA_SEP +
                        LocationContract.FeedEntry.COLUMN_NAME_LAT + TEXT_TYPE + " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + LocationContract.FeedEntry.TABLE_NAME;

        public LocationDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }
    }
}
