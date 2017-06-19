package c5113228.ce.kmu.chapter10_example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Explicit_Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explicit__menu);
    }

    public void mylistener1(View target){
        Intent intent = new Intent(getApplicationContext(), Explicit_Intro.class);
        startActivity(intent);
    }
    public void mylistener2(View target){
        Intent intent = new Intent(getApplicationContext(), Explicit_Setup.class);
        startActivity(intent);
    }
    public void mylistener3(View target){
        Intent intent = new Intent(getApplicationContext(), Explicit_Start.class);
        startActivity(intent);
    }
}
