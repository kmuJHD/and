package c5113228.ce.kmu.personalproject2;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ExerciseAlarmActivity extends AppCompatActivity {

    private TextView tv_text;
    private Button btn_ok;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_alarm);

        // 화면 꺼진 상태에서 동작 할 수 있도록 플래그 설정
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON, WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        tv_text = (TextView)findViewById(R.id.exalarm_tv);
        btn_ok = (Button)findViewById(R.id.exalarm_btn);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, (60 * 1000));
    }

    @Override
    protected void onResume() {
        super.onResume();

        String mode = getIntent().getStringExtra("Mode");   // 10min, 5min, Now 세종류

        switch (mode){
            case "10min":
                tv_text.setText("운동 시작 시간 10분 전");
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.apple_ring_0);
                break;
            case "5min":
                tv_text.setText("운동 시작 시간 5분 전");
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.apple_ring_20);
                break;
            case "Now":
                tv_text.setText("운동 시작 시간");
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.apple_ring_40);
                break;
        }

        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        String name = getIntent().getStringExtra("Name");
        String date = getIntent().getStringExtra("Date");
        String time = getIntent().getStringExtra("Time");

        tv_text.append("\n운동명 : " + name);
        tv_text.append("\n일시 : " + date);
        tv_text.append("\n시간 : " + time);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
