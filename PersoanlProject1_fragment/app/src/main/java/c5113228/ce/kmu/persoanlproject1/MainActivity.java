package c5113228.ce.kmu.persoanlproject1;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements FragMain.OnActionEventListener {
    FragmentManager manager;
    FragmentTransaction fragmentTransaction;
    FragMain fragMain;
    LinearLayout layout_main, layout_container;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater  = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 메인 프래그먼트 숨기고 메뉴 프래그먼트 표시
        layout_container.setVisibility(View.VISIBLE);
        layout_main.setVisibility(View.INVISIBLE);

        // popBackStack() 가능하도록 스택 추가
        fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.addToBackStack(null);

        FragList fragList;
        Bundle bundle = new Bundle();

        switch(item.getItemId()){
            case R.id.navigation_view:
                fragList = new FragList();
                bundle.putString("mode", "View");
                fragList.setArguments(bundle);

                fragmentTransaction.replace(R.id.start_fragContainer, fragList);
                break;

            case R.id.navigation_add:
                fragmentTransaction.replace(R.id.start_fragContainer, new FragAdd());
                break;

            case R.id.navigation_delete:
                fragList = new FragList();
                bundle.putString("mode", "Delete");
                fragList.setArguments(bundle);

                fragmentTransaction.replace(R.id.start_fragContainer, fragList);
                break;
        }

        fragmentTransaction.commit();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        layout_main = (LinearLayout)findViewById(R.id.start_fragMain);
        layout_container = (LinearLayout)findViewById(R.id.start_fragContainer);

        fragMain = new FragMain();

        manager = getSupportFragmentManager();
        fragmentTransaction = manager.beginTransaction();

        fragmentTransaction.replace(R.id.start_fragMain, fragMain);
        fragmentTransaction.commit();
    }

    @Override
    public void OnAction(String url) {
        // 메뉴 프래그먼트 숨기고 메인 프래그먼트 표시
        layout_container.setVisibility(View.INVISIBLE);
        layout_main.setVisibility(View.VISIBLE);
        if(url != null) {
            // 메인 프래그먼트를 지우는게 아닌 가려놓아야 객체가 남아있어 제어가 가능함
            fragMain.setWebView(url);
        }
    }

}
