package c5113228.ce.kmu.activitygamemenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartActivity extends AppCompatActivity {

    Button btn_start, btn_setup;
    static final int GET_STRING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn_start = (Button)findViewById(R.id.btn_start);
        btn_setup = (Button)findViewById(R.id.btn_setup);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, IntroActivity.class);
                startActivity(intent);
            }
        });

        btn_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, SetupActivity.class);
                startActivityForResult(intent, GET_STRING);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == GET_STRING){
            if(resultCode == RESULT_OK){
                String setting = data.getStringExtra("setting");
                Toast.makeText(getApplicationContext(), setting, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
