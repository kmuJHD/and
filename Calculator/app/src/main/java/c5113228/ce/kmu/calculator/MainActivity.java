package c5113228.ce.kmu.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText edt_result;
    Button btn_del, btn_divide, btn_mul, btn_sub, btn_add, btn_equal;
    Button btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_dot;

    double prev_number, now_number;
    String mode;
    boolean clear, is_prev;

    public double calc(double a, double b){
        is_prev = true;
        double result = 0.d;
        switch(mode){
            case "divide":
                result = a / b;
                break;
            case "mul":
                result = a * b;
                break;
            case "sub":
                result = a - b;
                break;
            case "add":
                result = a + b;
                break;
        }
        return result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prev_number = 0d;
        clear = false;

        Button.OnClickListener listener_mode = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btn_del){
                    is_prev = false;
                    prev_number = 0d;
                    edt_result.setText("");
                    return;
                }

                if(edt_result.getText().toString().equals("") || clear) return;

                if(is_prev) edt_result.setText(String.valueOf(calc(prev_number, now_number)));

                switch (view.getId()){
                    case R.id.btn_divide:
                        mode = "divide";
                        break;
                    case R.id.btn_mul:
                        mode = "mul";
                        break;
                    case R.id.btn_sub:
                        mode = "sub";
                        break;
                    case R.id.btn_add:
                        mode = "add";
                        break;
                }

                prev_number = Double.parseDouble(edt_result.getText().toString());
                clear = true;
                is_prev = true;
            }
        };
        Button.OnClickListener listener_input = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btn_dot && (edt_result.getText().toString().contains(".") || edt_result.getText().toString().equals(""))) return;
                if(clear){
                    clear = false;
                    edt_result.setText("");
                }
                edt_result.setText(edt_result.getText().toString() + ((Button)view).getText().toString());
                now_number = Double.parseDouble(edt_result.getText().toString());
            }
        };
        Button.OnClickListener listener_calc = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double result = 0d;
                result = calc(prev_number, now_number);
                prev_number = result;
                edt_result.setText(String.valueOf(result));
                is_prev = false;
            }
        };


        edt_result = (EditText)findViewById(R.id.edt_result);
        btn_del = (Button)findViewById(R.id.btn_del);
        btn_divide = (Button)findViewById(R.id.btn_divide);
        btn_mul = (Button)findViewById(R.id.btn_mul);
        btn_sub = (Button)findViewById(R.id.btn_sub);
        btn_add = (Button)findViewById(R.id.btn_add);
        btn_equal = (Button)findViewById(R.id.btn_equal);

        edt_result.setOnClickListener(listener_mode);
        btn_del.setOnClickListener(listener_mode);
        btn_divide.setOnClickListener(listener_mode);
        btn_mul.setOnClickListener(listener_mode);
        btn_sub.setOnClickListener(listener_mode);
        btn_add.setOnClickListener(listener_mode);
        btn_equal.setOnClickListener(listener_calc);


        btn_0 = (Button)findViewById(R.id.btn_0);
        btn_1 = (Button)findViewById(R.id.btn_1);
        btn_2 = (Button)findViewById(R.id.btn_2);
        btn_3 = (Button)findViewById(R.id.btn_3);
        btn_4 = (Button)findViewById(R.id.btn_4);
        btn_5 = (Button)findViewById(R.id.btn_5);
        btn_6 = (Button)findViewById(R.id.btn_6);
        btn_7 = (Button)findViewById(R.id.btn_7);
        btn_8 = (Button)findViewById(R.id.btn_8);
        btn_9 = (Button)findViewById(R.id.btn_9);
        btn_dot = (Button)findViewById(R.id.btn_dot);

        btn_0.setOnClickListener(listener_input);
        btn_1.setOnClickListener(listener_input);
        btn_2.setOnClickListener(listener_input);
        btn_3.setOnClickListener(listener_input);
        btn_4.setOnClickListener(listener_input);
        btn_5.setOnClickListener(listener_input);
        btn_6.setOnClickListener(listener_input);
        btn_7.setOnClickListener(listener_input);
        btn_8.setOnClickListener(listener_input);
        btn_9.setOnClickListener(listener_input);
        btn_dot.setOnClickListener(listener_input);

    }

}
