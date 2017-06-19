package c5113228.ce.kmu.chapter10_example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_implicit, btn_explicit, btn_extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_explicit = (Button)findViewById(R.id.btn_explicit);
        btn_implicit = (Button)findViewById(R.id.btn_implicit);
        btn_extra = (Button)findViewById(R.id.btn_extra);

        btn_implicit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Explicit_Menu.class);
                startActivity(intent);

            }
        });

        btn_explicit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Implicit.class);
                startActivity(intent);
            }
        });
        btn_extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Extra.class);
                startActivity(intent);
            }
        });
    }

}
