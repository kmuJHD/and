package c5113228.ce.kmu.tempconvertor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText et_number;
    Button btn_convert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_number = (EditText)findViewById(R.id.et_number);
        btn_convert = (Button)findViewById(R.id.btn_convert);

        btn_convert.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_convert:

                RadioButton rb_celsius = (RadioButton)findViewById(R.id.rb_c);
                RadioButton rb_fahrenheit = (RadioButton)findViewById(R.id.rb_f);

                if(et_number.getText().length() == 0){
                    Toast.makeText(this, "정확한 값을 입력하시오", Toast.LENGTH_LONG).show();
                    return;
                }
                String inputString = et_number.getText().toString().substring(0, et_number.length() - 1);
                String control = et_number.getText().toString().substring(et_number.length()-1).toLowerCase();

                float inputValue = Float.parseFloat(inputString.toString());


                if(control.equals("f")){

                    et_number.setText(String.valueOf(convertFahrenheitToCelsius(inputValue)));
                    rb_celsius.setChecked(true);
                    rb_fahrenheit.setChecked(false);
                }else if(control.equals("c")){

                    et_number.setText(String.valueOf(convertCelsiusToFahrenheit(inputValue)));
                    rb_celsius.setChecked(false);
                    rb_fahrenheit.setChecked(true);
                }else{
                    Toast.makeText(this, "구분값을 입력하시오", Toast.LENGTH_LONG).show();
                    return;
                }


                break;
        }
    }

    private float convertFahrenheitToCelsius(float fahrenheit){
        return ((fahrenheit - 32) * 5 / 9);
    }

    private float convertCelsiusToFahrenheit(float celsius){
        return ((celsius * 9) / 5) + 32;
    }
}
