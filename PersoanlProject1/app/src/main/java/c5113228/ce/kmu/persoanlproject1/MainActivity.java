package c5113228.ce.kmu.persoanlproject1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    final static int REQUEST_ACTION = 10001;
    Button btn_browse;
    EditText edt_url;
    WebView webView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater  = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()){
            case R.id.navigation_view:
                intent = new Intent(this, FragList.class);
                intent.putExtra("mode", "view");
                startActivityForResult(intent, REQUEST_ACTION);

                break;
            case R.id.navigation_add:
                intent = new Intent(this, FragAdd.class);
                startActivity(intent);
                break;
            case R.id.navigation_delete:
                intent = new Intent(this, FragList.class);
                intent.putExtra("mode", "delete");
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_browse = (Button)findViewById(R.id.btn_browse);
        edt_url = (EditText)findViewById(R.id.edt_url);
        webView = (WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl(edt_url.getText().toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == REQUEST_ACTION){
            String url = data.getStringExtra("url");
            if(!url.startsWith("http")) {
                url = "http://" + url;
            }
            edt_url.setText(url);
            webView.loadUrl(url);
        }
    }
}
