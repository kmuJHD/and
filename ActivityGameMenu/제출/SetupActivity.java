package c5113228.ce.kmu.activitygamemenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SetupActivity extends AppCompatActivity {

    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                String result = "";

                switch(radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioButton1:
                        result = ((RadioButton)findViewById(R.id.radioButton1)).getText().toString();
                        break;
                    case R.id.radioButton2:
                        result = ((RadioButton)findViewById(R.id.radioButton2)).getText().toString();
                        break;
                    case R.id.radioButton3:
                        result = ((RadioButton)findViewById(R.id.radioButton3)).getText().toString();
                        break;
                }

                Intent intent = new Intent();
                intent.putExtra("setting", result);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
