package c5113228.ce.kmu.personalproject2;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExerciseScheduleActivity extends AppCompatActivity implements ExerciseSchDialogFrag.OnFragmentInteractionListener {

    private ArrayList<ScheduleItem> scheduleItems;
    private ScheduleAdapter scheduleAdapter;
    private RecyclerView recyclerView;
    private ExerSchDbHelper exerSchDbHelper;

    private void updateDatabase() {

        SQLiteDatabase db = exerSchDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ExerSchDbHelper.ExerSchContract.FeedEntry._ID,
                ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE,
                ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME,
                ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME
        };

        Cursor c = db.query(
                ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        scheduleItems.clear();
        while(c.moveToNext()){
            ScheduleItem scheduleItem = new ScheduleItem(c.getString(c.getColumnIndexOrThrow(ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE)),
                    c.getString(c.getColumnIndexOrThrow(ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME)),
                    c.getString(c.getColumnIndexOrThrow(ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME)));
            scheduleItems.add(scheduleItem);
        }
        scheduleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex_schedule);

        recyclerView = (RecyclerView)findViewById(R.id.list_exer_sch);

        scheduleItems = new ArrayList<>();
        scheduleItems.clear();

        // RecyclerView 설정
        scheduleAdapter = new ScheduleAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(scheduleAdapter);

        // 일정 목록 불러오기(데이터 베이스 사용)
        exerSchDbHelper = new ExerSchDbHelper(getApplicationContext());
        updateDatabase();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_exer_sch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        ExerciseSchDialogFrag exerciseSchDialogFrag = new ExerciseSchDialogFrag();
        Bundle bundle = new Bundle();

        switch (item.getItemId()){
            case R.id.exer_sch_add:
                // 추가버튼 동작
                bundle.putString("mode", "add");
                exerciseSchDialogFrag.setArguments(bundle);
                exerciseSchDialogFrag.show(fragmentManager, "fragment_exercise");
                break;
            case R.id.exer_sch_edit:
                // 수정 버튼 동작
                if(scheduleAdapter.getLastSelectedPoisition() != -1) {
                    ScheduleItem scheduleItem = scheduleItems.get(scheduleAdapter.getLastSelectedPoisition());
                    bundle.putString("mode", "edit");
                    bundle.putString("date", scheduleItem.getDate());
                    bundle.putString("name", scheduleItem.getName());
                    bundle.putString("time", scheduleItem.getTime());
                    exerciseSchDialogFrag.setArguments(bundle);
                    exerciseSchDialogFrag.show(fragmentManager, "fragment_exercise");
                }else{
                    Toast.makeText(this, "선택된 항목이 없습니다", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.exer_sch_delete:
                // 삭제버튼 동작
                if(scheduleAdapter.getLastSelectedPoisition() != -1) {

                    // 데이터 베이스에서 삭제.
                    SQLiteDatabase sqLiteDatabase = exerSchDbHelper.getWritableDatabase();

                    String selection = ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME + " LIKE ?";
                    int lastselected = scheduleAdapter.getLastSelectedPoisition();
                    String[] selectionArgs = { scheduleItems.get(lastselected).getName() };
                    try {
                        sqLiteDatabase.beginTransaction();
                        sqLiteDatabase.delete(ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME, selection, selectionArgs);
                        sqLiteDatabase.setTransactionSuccessful();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }finally {
                        sqLiteDatabase.endTransaction();
                    }

                    // 현재 배열에서도 삭제
                    scheduleItems.remove(scheduleAdapter.getLastSelectedPoisition());

                    scheduleAdapter.setLastSelectedPoisition(-1);
                    scheduleAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(this, "선택된 항목이 없습니다", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction() {
        updateDatabase();
    }



    // 운동일정 객체
    public static class ScheduleItem {
        private String date, name, time;

        public ScheduleItem(String date, String name, String time) {
            this.date = date;
            this.name = name;
            this.time = time;
        }

        public String getDate() {
            return date;
        }

        public String getName() {
            return name;
        }

        public String getTime() {
            return time;
        }
    }

    // 운동일정 어댑터
    public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
        private int lastSelectedPoisition = -1;

        public int getLastSelectedPoisition(){ return lastSelectedPoisition; }

        public void setLastSelectedPoisition(int lastSelectedPoisition) {
            this.lastSelectedPoisition = lastSelectedPoisition;
        }

        @Override
        public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_schedule, parent, false);
            return new ScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ScheduleViewHolder holder, int position) {
            ScheduleItem scheduleItem = scheduleItems.get(position);
            holder.checkBox.setTag(position);       // 몇번째 항목인지 기억
            holder.date.setText(scheduleItem.getDate());
            holder.name.setText(scheduleItem.getName());
            holder.time.setText(scheduleItem.getTime());

            holder.checkBox.setChecked(position == lastSelectedPoisition);
        }

        @Override
        public int getItemCount() {
            return scheduleItems.size();
        }

        public class ScheduleViewHolder extends RecyclerView.ViewHolder {
            RadioButton checkBox;
            TextView date, name, time;

            public ScheduleViewHolder(View itemView) {
                super(itemView);
                checkBox = (RadioButton)itemView.findViewById(R.id.list_checkBox);
                date = (TextView)itemView.findViewById(R.id.list_tv_date);
                name = (TextView)itemView.findViewById(R.id.list_tv_name);
                time = (TextView)itemView.findViewById(R.id.list_tv_time);

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lastSelectedPoisition = getAdapterPosition();
                        notifyItemRangeChanged(0, scheduleItems.size());
                    }
                });

            }
        }
    }


    public static class ExerSchDbHelper extends SQLiteOpenHelper {
        public static final class ExerSchContract{
            // To prevent someone from accidentally instantiating the contract class,
            // make the constructor private.
            private ExerSchContract() { }

            /* Inner class that defines the table contents */
            public static class FeedEntry implements BaseColumns {
                public static final String TABLE_NAME = "entry";
                public static final String COLUMN_NAME_DATE = "exer_date";
                public static final String COLUMN_NAME_NAME = "exer_name";
                public static final String COLUMN_NAME_TIME = "exer_time";
            }
        }
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "ExerciseSchedule.db";

        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ExerSchContract.FeedEntry.TABLE_NAME + " (" +
                        ExerSchContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        ExerSchContract.FeedEntry.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                        ExerSchContract.FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                        ExerSchContract.FeedEntry.COLUMN_NAME_TIME + TEXT_TYPE + " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ExerSchContract.FeedEntry.TABLE_NAME;

        public ExerSchDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
            onCreate(sqLiteDatabase);
        }
    }

}
