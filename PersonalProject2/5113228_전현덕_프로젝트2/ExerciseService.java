package c5113228.ce.kmu.personalproject2;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ExerciseService extends Service {

    private static final String TAG = LocationMgrService.class.getSimpleName();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private IBinder iBinder = new ExerBinder();
    private List<ExerciseScheduleActivity.ScheduleItem> scheduleItems;
    private HashMap<String, Integer> checked_schedule;
    private ExerciseScheduleActivity.ExerSchDbHelper exerSchDbHelper;
    private boolean chk_update;

    @Override
    public void onCreate() {
        // 서비스를 실행할 스레드 생성. 스레드를 사용하지 않으면 서비스가 메인 프로세스에서 돌아감
        HandlerThread thread = new HandlerThread("ExerciseServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        scheduleItems = new ArrayList<>();
        checked_schedule = new HashMap<>();
        exerSchDbHelper = new ExerciseScheduleActivity.ExerSchDbHelper(getApplicationContext());
        chk_update = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started - Exercise ", Toast.LENGTH_SHORT).show();

        // startForeground 를 실행하지 않을 경우 프로세스 강제 종료시 서비스도 같이 종료됨
        startForeground(startId, getNotification());

        // start id를 전달하여 종료시 필요한 id 구분
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // 종료될경우 다시 시작하도록 값 설정
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        mServiceLooper.quit();
        Toast.makeText(this, "Service Destroyed - Exercise ", Toast.LENGTH_SHORT).show();
    }

    /**
     *  foreground service 용 NotificationCompat 반환
     */
    private Notification getNotification() {

        CharSequence text = "운동 일정 알람 기능이 사용중입니다";

        return new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle("PersonalProject2")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis()).build();
    }

    // 스레드에서 메세지를 받은 경우 처리
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        private SQLiteDatabase db;

        @Override
        public void handleMessage(Message msg) {
            /*
             * 알람 확인 후 처리 부분.
             * 알람 정보 갱신 / 알람 동작을 서로 번걸아 가면서 반복함.
            */
            if(chk_update) {
                chk_update = false;
                updateDB();
//                Log.i(TAG, "update db...");
            }else {
                chk_update = true;
                checkExercise();
//                Log.i(TAG, "check schedule...");
            }

            // 일정 시간 마다 반복 실행
            Message message = mServiceHandler.obtainMessage();
            message.arg1 = msg.arg1;
            mServiceHandler.sendMessageDelayed(message, 3000);

            // 모든 작업 종료시 stopSelf() 호출하던가 stopService() 로  종료시켜야 함
            // 이 앱에서는 MainActivity 의 스위치로 제어하므로 사용하지 않음
//            stopSelf(msg.arg1);
        }

        public void updateDB(){
            /*
             * 데이터 베이스에서 저장된 일정 값 불러오기
             */
            SQLiteDatabase db = exerSchDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry._ID,
                    ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE,
                    ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME,
                    ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME
            };

            Cursor c = db.query(
                    ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME,                     // The table to query
                    projection,                      // The columns to return
                    null,                            // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                            // don't group the rows
                    null,                            // don't filter by row groups
                    null                             // The sort order
            );

            scheduleItems.clear();
            while(c.moveToNext()){
                ExerciseScheduleActivity.ScheduleItem scheduleItem = new ExerciseScheduleActivity.ScheduleItem(c.getString(c.getColumnIndexOrThrow(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE)),
                        c.getString(c.getColumnIndexOrThrow(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME)),
                        c.getString(c.getColumnIndexOrThrow(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME)));
                scheduleItems.add(scheduleItem);
            }

            c.close();
            db.close();
        }

        public void checkExercise(){
            /*
             * 현재 시간과 비교하여 알람 실행
             */
            Calendar calendar = Calendar.getInstance();
            Date nowDate = calendar.getTime();

            for(ExerciseScheduleActivity.ScheduleItem scheduleItem : scheduleItems){
                // 입력 데이터 Date 객체로 변환
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date();
                try {
                    date = dateFormat.parse((scheduleItem.getDate()) + " " + scheduleItem.getTime());
                    calendar.setTime(date);

                    // 시간값 설정
                    long timenow = nowDate.getTime();
                    long time10min = date.getTime() - (10 * 60 * 1000);
                    long time5min = date.getTime() - (5 * 60 * 1000);
                    long time0min = date.getTime();
                    long time5over = date.getTime() + (5 * 60 * 1000);

                    // 알람 액티비티로 넘길 공통값
                    Intent intent = new Intent(getApplicationContext(), ExerciseAlarmActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Name", scheduleItem.getName());
                    intent.putExtra("Date", scheduleItem.getDate());
                    intent.putExtra("Time", scheduleItem.getTime());


                    // 10분 5분 정각에 맞추어 각각 다른 알람 표시

                    // 10분전 알람 시간 체크. - 1은 간격 중첩 방지용
                    if(time10min < timenow && timenow < (time5min - 1)){
                        // 10분전 알람을 실행한적이 있는지 이력 확인
                        if(checked_schedule.get(scheduleItem.getName()) == null || checked_schedule.get(scheduleItem.getName()) > 10) {
                            checked_schedule.put(scheduleItem.getName(), 10); // 알람 확인용 목록에 10분전 체크이력 입력
                            Log.i(TAG, scheduleItem.getName() + " 10min started");

                            // 알람 액티비티 실행
                            intent.putExtra("Mode", "10min");
                            startActivity(intent);
                        }

                    // 5분전 알람 시간 체크. - 1은 간격 중첩 방지용
                    }else if(time5min < timenow && timenow < (time0min - 1)){
                        // 5분전 알람을 실행한적이 있는지 이력 확인
                        if(checked_schedule.get(scheduleItem.getName()) == null || checked_schedule.get(scheduleItem.getName()) > 5) {
                            checked_schedule.put(scheduleItem.getName(), 5);  // 알람 확인용 목록에 5분전 체크이력 입력
                            Log.i(TAG, scheduleItem.getName() + " 5min started");

                            // 알람 액티비티 실행
                            intent.putExtra("Mode", "5min");
                            startActivity(intent);
                        }

                    // 정각 알람 시간 체크. 알람 시간이 5분 이상 경과한 경우 작동하지 않음 (과거 시간 알람 표시 방지)
                    }else if(time0min < timenow && timenow < time5over){
                        // 데이터 베이스에서 삭제.
                        SQLiteDatabase sqLiteDatabase = exerSchDbHelper.getWritableDatabase();

                        String selection = ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME + " LIKE ?";
                        String[] selectionArgs = {scheduleItem.getName()};
                        try {
                            sqLiteDatabase.beginTransaction();
                            sqLiteDatabase.delete(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME, selection, selectionArgs);
                            sqLiteDatabase.setTransactionSuccessful();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            sqLiteDatabase.endTransaction();
                        }

                        sqLiteDatabase.close();

                        // 알람 실행 이력에서 삭제
                        checked_schedule.remove(scheduleItem.getName());

                        Log.i(TAG, scheduleItem.getName() + " nowtime started");

                        // 알람 액티비티 실행
                        intent.putExtra("Mode", "Now");
                        startActivity(intent);
                    }

                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public class ExerBinder extends Binder{
        ExerciseService getService() {
            return ExerciseService.this;
        }
    }
}
