package c5113228.ce.kmu.personalproject2;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class ExerciseSchDialogFrag extends DialogFragment {

    /**
     *  EditText 양식
     *  date : yyyy-MM-dd
     *  name : 문자열
     *  time : hh:mm
     */
    private OnFragmentInteractionListener mListener;
    private Button btn_apply;
    private EditText edit_date, edit_name, edit_time;
    private Button.OnClickListener onAddClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 데이터 베이스에 추가 동작.
            ExerciseScheduleActivity.ExerSchDbHelper exerSchDbHelper = new ExerciseScheduleActivity.ExerSchDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase sqLiteDatabase = exerSchDbHelper.getWritableDatabase();
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE, edit_date.getText().toString());
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME, edit_name.getText().toString());
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME, edit_time.getText().toString());

                sqLiteDatabase.beginTransaction();          // 빠른 결과 처리를 위한 트렌젝션 처리
                sqLiteDatabase.insert(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME, null, contentValues);
                sqLiteDatabase.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally {
                sqLiteDatabase.endTransaction();
            }

            onButtonPressed();          // 부모 액티비티의 리스트 어댑터 갱신
            dismiss();
        }
    };
    private Button.OnClickListener onEditClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 데이터 베이스에 수정 동작.
            ExerciseScheduleActivity.ExerSchDbHelper exerSchDbHelper = new ExerciseScheduleActivity.ExerSchDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase sqLiteDatabase = exerSchDbHelper.getWritableDatabase();
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_DATE, edit_date.getText().toString());
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME, edit_name.getText().toString());
                contentValues.put(ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_TIME, edit_time.getText().toString());

                sqLiteDatabase.beginTransaction();          // 빠른 결과 처리를 위한 트렌젝션 처리
                String selection = ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.COLUMN_NAME_NAME + " LIKE ?";
                String[] selectionArgs = { getArguments().getString("name") };

                sqLiteDatabase.update(
                        ExerciseScheduleActivity.ExerSchDbHelper.ExerSchContract.FeedEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);

                sqLiteDatabase.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally {
                sqLiteDatabase.endTransaction();
            }

            onButtonPressed();          // 부모 액티비티의 리스트 어댑터 갱신
            dismiss();
        }
    };

    public ExerciseSchDialogFrag() { }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_sch_dialog, container, false);

        btn_apply = (Button)view.findViewById(R.id.exdiag_btn_apply);
        edit_date = (EditText)view.findViewById(R.id.exdiag_edit_date);
        edit_name = (EditText)view.findViewById(R.id.exdiag_edit_name);
        edit_time = (EditText)view.findViewById(R.id.exdiag_edit_time);

        String mode = getArguments().getString("mode", "add");
        if(mode.equals("add")){
            // 데이터베이스 추가 동작
            btn_apply.setOnClickListener(onAddClick);
        }else if(mode.equals("edit")){
            // 데이터베이스 수정 동작
            edit_date.setText(getArguments().getString("date"));
            edit_name.setText(getArguments().getString("name"));
            edit_time.setText(getArguments().getString("time"));
            btn_apply.setOnClickListener(onEditClick);
        }
        return view;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
