package c5113228.ce.kmu.persoanlproject1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class FragMain extends Fragment{

    Button btn_browse;
    EditText edt_url;
    WebView webView;

    interface OnActionEventListener {
        void OnAction(String url);
    }

    public FragMain() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        btn_browse = (Button)view.findViewById(R.id.btn_browse);
        edt_url = (EditText)view.findViewById(R.id.edt_url);
        webView = (WebView)view.findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl(edt_url.getText().toString());
            }
        });

        return view;
    }

    public void setWebView(String url){

        if(!url.startsWith("http")) {
            url = "http://" + url;
        }
        edt_url.setText(url);
        webView.loadUrl(url);
    }

}
