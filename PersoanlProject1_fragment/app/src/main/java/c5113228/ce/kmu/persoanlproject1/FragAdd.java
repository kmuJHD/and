package c5113228.ce.kmu.persoanlproject1;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragAdd extends Fragment {

    private Button btn_add, btn_close;
    private EditText edit_title, edit_url, edit_date;
    public FragMain.OnActionEventListener onActionEventListener;

    public FragAdd() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragMain.OnActionEventListener) {
            onActionEventListener = (FragMain.OnActionEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onActionEventListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        btn_add = (Button)view.findViewById(R.id.btn_add_action);
        btn_close = (Button)view.findViewById(R.id.btn_add_close);
        edit_title = (EditText)view.findViewById(R.id.edit_add_title);
        edit_url = (EditText)view.findViewById(R.id.edit_add_url);
        edit_date = (EditText)view.findViewById(R.id.edit_add_date);

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActionEventListener.OnAction(null);
                getFragmentManager().popBackStack();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Data", Context.MODE_PRIVATE);
                try {
                    ArrayList<FragList.SiteListItem> siteListItems = new ArrayList<>();
                    String arrayString = sharedPreferences.getString("SiteList", null);

                    // 저장된 데이터 로드
                    if(arrayString != null) {
                        JSONArray jsonArray = new JSONArray(arrayString);
                        for(int i = 0; i < jsonArray.length(); ++i){
                            JSONObject object = jsonArray.getJSONObject(i);
                            FragList.SiteListItem listItem = new FragList.SiteListItem(object.getString("Title"), object.getString("Url"), object.getString("Date"));
                            siteListItems.add(listItem);
                        }
                    }

                    // 입력값 추가
                    siteListItems.add(new FragList.SiteListItem(edit_title.getText().toString(), edit_url.getText().toString(), edit_date.getText().toString()));

                    // 데이터 저장용 JSON String 생성
                    JSONArray jsonArray = new JSONArray();
                    for(FragList.SiteListItem item : siteListItems){
                        JSONObject object = new JSONObject();
                        object.put("Title", item.getTitle());
                        object.put("Url", item.getUrl());
                        object.put("Date", item.getDate());

                        jsonArray.put(object);
                    }

                    // 데이터 저장
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("SiteList", jsonArray.toString());
                    editor.apply();
                    editor.commit();

                    // 폼 초기화
                    edit_title.setText("");
                    edit_url.setText("");
                    edit_date.setText("");

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        return view;
    }
}
