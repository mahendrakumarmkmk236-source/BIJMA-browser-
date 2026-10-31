package com.mahendra.bijmaaibrowser;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    EditText addressBox;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(Color.WHITE);
        addressBox.setHintTextColor(Color.GRAY);
        addressBox.setHint("वेबसाइट या कुछ भी सर्च करें");
        addressBox.setPadding(18, 10, 18, 10);
        addressBox.setTextSize(16);

        root.addView(addressBox, new LinearLayout.LayoutParams(
                -1, 55));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);

        Button back = makeButton("←");
        Button forward = makeButton("→");
        Button home = makeButton("⌂");
        Button reload = makeButton("⟳");
        Button go = makeButton("GO");

        bar.addView(back);
        bar.addView(forward);
        bar.addView(home);
        bar.addView(reload);
        bar.addView(go);

        root.addView(bar);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        root.addView(webView, new LinearLayout.LayoutParams(
                -1, 0, 1));

        setContentView(root);

        View.OnClickListener searchAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebsite();
            }
        };

        go.setOnClickListener(searchAction);

        addressBox.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(
                    android.widget.TextView v,
                    int actionId,
                    KeyEvent event) {
                openWebsite();
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://www.google.com");
            }
        });

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        webView.loadUrl("https://www.google.com");

        tts = new TextToSpeech(this,
                new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(new Locale("hi", "IN"));
                    tts.speak(
                            "Hello Mahendra ji, main aapki kya madad kar sakti hun",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "welcome"
                    );
                }
            }
        });
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16);
        button.setBackgroundColor(Color.BLACK);
        return button;
    }

    private void openWebsite() {
        String address = addressBox.getText().toString().trim();

        if (address.isEmpty()) {
            return;
        }

        if (address.contains(".") &&
                !address.contains(" ")) {
            if (!address.startsWith("http://") &&
                    !address.startsWith("https://")) {
                address = "https://" + address;
            }
            webView.loadUrl(address);
        } else {
            webView.loadUrl(
                    "https://www.google.com/search?q=" +
                    android.net.Uri.encode(address)
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}