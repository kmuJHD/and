package c5113228.ce.kmu.persoanlproject1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragList extends AppCompatActivity {

    private String mode;
    private Button close, action;
    private RecyclerView listView;
    private ArrayList<SiteListItem> siteListItems;
    private SiteAdapter siteAdapter;
    private int selected_item;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mode = getIntent().getStringExtra("mode");
        siteListItems = new ArrayList<>();
        close = (Button)findViewById(R.id.btn_list_close);
        action = (Button)findViewById(R.id.btn_list_action);
        listView = (RecyclerView) findViewById(R.id.list_sites);

        // 사이트 목록 저장된 데이터에서 불러오기
        siteListItems.clear();
        final SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        try {
            String arrayString = sharedPreferences.getString("SiteList", null);
            if(arrayString != null) {
                JSONArray jsonArray = new JSONArray(arrayString);
                for(int i = 0; i < jsonArray.length(); ++i){
                    JSONObject object = jsonArray.getJSONObject(i);
                    SiteListItem listItem = new SiteListItem(object.getString("Title"), object.getString("Url"), object.getString("Date"));
                    siteListItems.add(listItem);
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }

        // RecycleListView 설정
        siteAdapter = new SiteAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(linearLayoutManager);
        listView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if(child != null){
                    selected_item = rv.getChildAdapterPosition(child);
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        listView.setAdapter(siteAdapter);
        siteAdapter.notifyDataSetChanged();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        View.OnClickListener onClickListener;
        // 삭제 모드일경우 버튼 이름 변경및 이벤트 수정
        if(mode.equals("delete")){
            action.setText("삭제");
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 삭제모드일 경우 동작
                    try{
                        // 목록에서 해당 값 삭제
                        siteListItems.remove(selected_item);

                        // 나머지 목록 JSON 형식으로 변환
                        JSONArray jsonArray = new JSONArray();
                        for(SiteListItem item : siteListItems){
                            JSONObject object = new JSONObject();
                            object.put("Title", item.getTitle());
                            object.put("Url", item.getUrl());
                            object.put("Date", item.getDate());

                            jsonArray.put(object);
                        }

                        // 데이터 저장
                        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("SiteList", jsonArray.toString());
                        editor.commit();

                        siteAdapter.notifyDataSetChanged();

                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
            };
        }else{
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 보기 모드일 경우 동작
                    Intent intent = new Intent();
                    intent.putExtra("url", siteListItems.get(selected_item).getUrl());
                    setResult(MainActivity.REQUEST_ACTION, intent);
                    finish();
                }
            };
        }
        action.setOnClickListener(onClickListener);

    }

    public static class SiteListItem{
        private String title, url, date;

        public SiteListItem(String title, String url, String date) {
            this.title = title;
            this.url = url;
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public String getDate() {
            return date;
        }
    }


    public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {
        private View selectedView;
        private int selectedPosition;

        public class SiteViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout;
            TextView title, url, date;

            public SiteViewHolder(View itemView) {
                super(itemView);
                layout = (LinearLayout)itemView.findViewById(R.id.list_linearLayout);
                layout.setClickable(true);
                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 선택되어있지 않은 항목 클릭시
                        if(!view.isSelected()){
                            if(selectedView != null){
                                // 이전 선택 해제
                                selectedView.setSelected(false);
                            }
                            selectedPosition = listView.getChildAdapterPosition(view);
                            selectedView = view;

                            // 선택된 항목 선택
                            view.setSelected(true);
                        }else{
                            // 선택되어 있던 항목 선택 해제
                            selectedPosition = -1;
                            selectedView = null;
                            view.setSelected(false);
                        }

                    }
                });
                title = (TextView)itemView.findViewById(R.id.list_tv_title);
                url = (TextView)itemView.findViewById(R.id.list_tv_url);
                date = (TextView)itemView.findViewById(R.id.list_tv_date);
            }
        }
        @Override
        public SiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_website, parent, false);
            return new SiteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SiteViewHolder holder, int position) {
            SiteListItem item = siteListItems.get(position);
            holder.title.setText(item.getTitle());
            holder.url.setText(item.getUrl());
            holder.date.setText(item.getDate());
        }

        @Override
        public int getItemCount() {
            return siteListItems.size();
        }
    }
}
