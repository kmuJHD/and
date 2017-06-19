package c5113228.ce.kmu.personalproject2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

public class SafeMsgService extends Service {
    public SafeMsgService() {
    }

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // 스레드에서 메세지를 받은 경우 처리
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            // 일정 시간 마다 반복 실행
            Message message = mServiceHandler.obtainMessage();
            message.arg1 = msg.arg1;
            mServiceHandler.sendMessageDelayed(message, 3000);

            // 작업 종료시 stopSelf 호출하여 종료시켜야 함
//            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // 서비스를 실행할 스레드 생성. 스레드를 사용하지 않으면 서비스가 메인 프로세스에서 돌아감
        HandlerThread thread = new HandlerThread("ExerciseServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started - Exercise ", Toast.LENGTH_SHORT).show();

        // start id를 전달하여 종료시 필요한 id 구분
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // 종료될경우 이곳으로 리턴되어 다시 시작함
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed - Exercise ", Toast.LENGTH_SHORT).show();
    }
}
