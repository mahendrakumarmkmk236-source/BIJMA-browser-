
package com.mahendra.bijmaaibrowser;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.webkit.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout root;
    WebView webView;
    EditText addressBox;

    int bg = Color.rgb(15, 18, 28);
    int panel = Color.rgb(29, 35, 51);
    int blue = Color.rgb(65, 145, 255);
    int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(white);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        t.setPadding(12, 14, 12, 14);
        return t;
    }

    Button button(String name, View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(name);
        b.setTextColor(white);
        b.setTextSize(17);
        b.setAllCaps(false);
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(panel)
        );
        b.setOnClickListener(action);
        return b;
    }

    void setup(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        TextView heading = text(title, 25);
        heading.setTextColor(blue);
        root.addView(heading);

        setContentView(root);
    }

    void showHome() {
        setup("BIJMA AI");
        root.addView(text("आपका स्मार्ट मनोरंजन ब्राउज़र", 16));

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(20, 10, 20, 10);

        addMenu(list, "🌐  Browser", "browser");
        addMenu(list, "▶  YouTube", "youtube");
        addMenu(list, "📺  Live TV", "livetv");
        addMenu(list, "🎬  IPTV / M3U", "iptv");
        addMenu(list, "🔒  VPN", "vpn");
        addMenu(list, "🔍  Video Zoom", "zoom");

        ScrollView scroll = new ScrollView(this);
        scroll.addView(list);
        root.addView(scroll);
    }

    void addMenu(LinearLayout list, String name, String page) {
        Button b = button(name, v -> openPage(page));
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                -1, ViewGroup.LayoutParams.WRAP_CONTENT
            );
        lp.setMargins(0, 9, 0, 9);
        list.addView(b, lp);
    }

    void openPage(String page) {
        if (page.equals("browser")) {
            showBrowser("https://www.google.com");
        } else if (page.equals("youtube")) {
            showBrowser("https://www.youtube.com");
        } else if (page.equals("zoom")) {
            showBrowser("https://www.youtube.com");
            Toast.makeText(this,
                "वेब वीडियो में ज़ूम सपोर्ट चालू है",
                Toast.LENGTH_LONG).show();
        } else if (page.equals("livetv")) {
            showFeature("LIVE TV",
                "Live TV चलाने के लिए चैनल स्रोत जोड़ना बाकी है।");
        } else if (page.equals("iptv")) {
            showIPTV();
        } else if (page.equals("vpn")) {
            showFeature("VPN",
                "VPN सर्वर की जानकारी मिलने पर वास्तविक कनेक्शन जोड़ा जाएगा।");
        }
    }

    void showFeature(String title, String message) {
        setup(title);
        root.addView(text(message, 18));
        root.addView(button("होम पर वापस जाएँ", v -> showHome()));
    }

    void showIPTV() {
        setup("IPTV / M3U");

        EditText playlist = new EditText(this);
        playlist.setSingleLine(true);
        playlist.setHint("M3U प्लेलिस्ट URL डालें");
        playlist.setHintTextColor(Color.LTGRAY);
        playlist.setTextColor(white);
        playlist.setPadding(15, 12, 15, 12);
        root.addView(playlist);

        root.addView(text(
            "M3U प्लेलिस्ट और वीडियो प्लेयर का एकीकरण अभी बाकी है।", 16
        ));
        root.addView(button("होम पर वापस जाएँ", v -> showHome()));
    }

    void showBrowser(String url) {
        setup("BIJMA Browser");

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);

        Button home = button("⌂", v -> showHome());
        bar.addView(home, new LinearLayout.LayoutParams(55, 55));

        addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(white);
        addressBox.setHintTextColor(Color.LTGRAY);
        addressBox.setHint("वेबसाइट या खोज लिखें");
        addressBox.setText(url);
        bar.addView(addressBox,
            new LinearLayout.LayoutParams(0, 55, 1));

        Button go = button("GO", v -> openAddress());
        bar.addView(go, new LinearLayout.LayoutParams(65, 55));

        root.addView(bar);

        webView = new WebView(this);
        webView.setBackgroundColor(white);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        root.addView(webView,
            new LinearLayout.LayoutParams(
                -1, 0, 1
            ));

        addressBox.setOnEditorActionListener(
            (v, actionId, event) -> {
                openAddress();
                return true;
            }
        );

        webView.loadUrl(url);
    }

    void openAddress() {
        String address = addressBox.getText().toString().trim();

        if (address.isEmpty()) return;

        if (address.contains(".") && !address.contains(" ")) {
            if (!address.startsWith("http://")
                && !address.startsWith("https://")) {
                address = "https://" + address;
            }
        } else {
            address = "https://www.google.com/search?q="
                + android.net.Uri.encode(address);
        }

        webView.loadUrl(address);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            showHome();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
