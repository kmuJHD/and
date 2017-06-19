package c5113228.ce.kmu.menu_context;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private CalendarView calendarView;
    private EditText edt_date, edt_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = (TimePicker)findViewById(R.id.timePicker);
        calendarView = (CalendarView)findViewById(R.id.calendarView);
        edt_date = (EditText)findViewById(R.id.edt_date);
        edt_time = (EditText)findViewById(R.id.edt_time);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                edt_date.setText(" " + i + " : " + i1 + " : " + i2);
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                edt_time.setText(" " + i + " : " + i1);
            }
        });

        registerForContextMenu(edt_time);
        registerForContextMenu(edt_date);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("컨텍스트 메뉴");
        menu.add(0, 1, 0, "배경색 : RED");
        menu.add(0, 2, 0, "배경색 : GREEN");
        menu.add(0, 3, 0, "배경색 : BLUE");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case 1:
                edt_time.setTextColor(Color.RED);
                edt_date.setTextColor(Color.RED);
                return true;
            case 2:
                edt_time.setTextColor(Color.GREEN);
                edt_date.setTextColor(Color.GREEN);
                return true;
            case 3:
                edt_time.setTextColor(Color.BLUE);
                edt_date.setTextColor(Color.BLUE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
