package c5113228.ce.kmu.chapter6_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VolumeControlView view = (VolumeControlView)findViewById(R.id.volume);
        view.setKnobListener(new VolumeControlView.KnobListener() {
            @Override
            public void onChanged(double angle) {
                if (angle > 0)
                    ;
                else
                    ;
            }
        });
    }
}
