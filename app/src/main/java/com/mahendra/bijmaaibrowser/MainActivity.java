package com.mahendra.bijmaaibrowser;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.*;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout root;
    WebView webView;
    TextToSpeech tts;
    EditText addressBox;

    int bg = Color.rgb(7, 10, 25);
    int card = Color.rgb(20, 25, 49);
    int white = Color.WHITE;
    int blue = Color.rgb(0, 190, 255);
    int purple = Color.rgb(175, 45, 255);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        tts = new TextToSpeech(this,
            new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(new Locale("hi", "IN"));
                    }
                }
            });

        showHome();
    }

    int dp(float value) {
        return (int)(value * getResources()
            .getDisplayMetrics().density);
    }

    GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(20));
        d.setStroke(dp(2), stroke);
        return d;
    }

    GradientDrawable gradient(int c1, int c2) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{c1, c2});
        d.setCornerRadius(dp(22));
        d.setStroke(dp(1), purple);
        return d;
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(white);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        return t;
    }

    LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    LinearLayout.LayoutParams params(int w, int h) {
        return new LinearLayout.LayoutParams(
            w < 0 ? w : dp(w),
            h < 0 ? h : dp(h));
    }

    TextView heading(String value) {
        TextView t = text(value, 23);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), dp(20), dp(10), dp(15));
        return t;
    }

    void speak(String message) {
        if (tts != null) {
            tts.speak(message,
                TextToSpeech.QUEUE_FLUSH, null, "bijma");
        }
    }

    void showHome() {
        root = column();
        root.setBackgroundColor(bg);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout page = column();
        page.setPadding(dp(16), dp(10), dp(16), dp(15));

        TextView logo = text("▶  BIJMA AI     👑 PRO", 29);
        logo.setTypeface(null, Typeface.BOLD);
        logo.setTextColor(blue);
        page.addView(logo, params(-1, 65));

        TextView tagline = text(
            "आपका स्मार्ट मनोरंजन ब्राउज़र", 17);
        page.addView(tagline);

        // AI ASSISTANT CARD
        LinearLayout assistant = new LinearLayout(this);
        assistant.setGravity(Gravity.CENTER);
        assistant.setPadding(dp(10), dp(12), dp(10), dp(12));
        assistant.setBackground(gradient(
            Color.rgb(30, 15, 85),
            Color.rgb(90, 15, 120)));

        TextView face = text("👩🏻‍🎤", 58);
        assistant.addView(face, params(115, 125));

        LinearLayout greeting = column();
        TextView hello = text(
            "Hello Mahendra ji 👋\nमैं आपकी क्या मदद कर सकती हूँ?",
            17);
        hello.setTypeface(null, Typeface.BOLD);
        greeting.addView(hello);

        TextView voice = text("🎙️  मुझसे बात करें", 15);
        voice.setBackground(shape(
            Color.rgb(40, 20, 100), blue));
        greeting.addView(voice, params(-1, 45));
        voice.setOnClickListener(v -> speak(
            "Hello Mahendra ji, main aapki kya madad kar sakti hoon"));

        assistant.addView(greeting,
            new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout.LayoutParams ap = params(-1, 150);
        ap.setMargins(0, dp(15), 0, dp(15));
        page.addView(assistant, ap);

        // SEARCH BOX
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(white);
        search.setHintTextColor(Color.LTGRAY);
        search.setHint("🔍  कुछ भी खोजें या वेबसाइट खोलें");
        search.setTextSize(16);
        search.setPadding(dp(18), 0, dp(18), 0);
        search.setBackground(shape(card, blue));
        page.addView(search, params(-1, 55));

        search.setOnEditorActionListener((v, action, event) -> {
            String q = search.getText().toString().trim();
            if (!q.isEmpty()) showBrowser(q);
            return true;
        });

        page.addView(heading("✨ आपके खास फीचर"));

        // FEATURE GRID
        addTileRow(page,
            "▶\nYouTube", "📺\nLive TV", "🎬\nIPTV (M3U)");

        addTileRow(page,
            "🌐\nBrowser", "🛡️\nVPN", "🔎\nVideo Zoom");

        page.addView(heading("🎭 मनोरंजन श्रेणियाँ"));

        addTileRow(page,
            "🎞️\nMovies", "📽️\nWeb Series",
            "📺\nTV Shows");

        addTileRow(page,
            "🎵\nMusic", "⭐\nFavorites",
            "⬇️\nDownloads");

        page.addView(heading("💜 BIJMA AI Assistant"));

        TextView help = text(
            "आपकी पसंद का मनोरंजन, एक ही जगह।\n" +
            "आवाज से बात करें और अपनी पसंद की वेबसाइट खोजें।",
            15);
        help.setBackground(shape(card, purple));
        page.addView(help, params(-1, 95));

        TextView footer = text(
            "BIJMA AI  •  आपका अपना स्मार्ट ब्राउज़र", 13);
        page.addView(footer);

        scroll.addView(page);
        root.addView(scroll);
        setContentView(root);
    }

    void addTileRow(LinearLayout parent,
                    String a, String b, String c) {

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setOrientation(LinearLayout.HORIZONTAL);

        addTile(row, a);
        addTile(row, b);
        addTile(row, c);

        LinearLayout.LayoutParams p = params(-1, 112);
        p.setMargins(0, dp(4), 0, dp(4));
        parent.addView(row, p);
    }

    void addTile(LinearLayout row, String name) {
        TextView tile = text(name, 17);
        tile.setTypeface(null, Typeface.BOLD);

        int color = name.contains("YouTube")
            ? Color.rgb(100, 10, 80) :
            name.contains("Live TV")
            ? Color.rgb(0, 65, 90) :
            Color.rgb(38, 20, 90);

        tile.setBackground(gradient(color, card));

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(0, -1, 1);

        p.setMargins(dp(4), dp(4), dp(4), dp(4));
        row.addView(tile, p);

        tile.setOnClickListener(v -> openFeature(name));
    }

    void openFeature(String name) {
        if (name.contains("Browser")) {
            showBrowser("https://www.google.com");
        } else if (name.contains("YouTube")) {
            showBrowser("https://www.youtube.com");
        } else if (name.contains("Live TV")) {
            showInfo("Live TV",
                "यहाँ लाइव चैनल जोड़ने की सुविधा बनेगी।");
        } else if (name.contains("IPTV")) {
            showInfo("IPTV / M3U",
                "अपनी वैध M3U प्लेलिस्ट जोड़ने की सुविधा।");
        } else if (name.contains("VPN")) {
            showInfo("VPN",
                "VPN स्क्रीन तैयार है। वास्तविक कनेक्शन के लिए VPN सर्वर जरूरी होगा।");
        } else if (name.contains("Zoom")) {
            showBrowser("https://www.youtube.com");
        } else {
            showInfo(name,
                "यह मनोरंजन सुविधा जल्द यहाँ जोड़ी जाएगी।");
        }
    }

    void showInfo(String title, String message) {
        root = column();
        root.setPadding(dp(18), dp(15), dp(18), dp(15));
        root.setBackgroundColor(bg);

        TextView back = text("←  वापस", 18);
        root.addView(back, params(-1, 55));
        back.setOnClickListener(v -> showHome());

        root.addView(heading(title));

        TextView info = text(message, 19);
        info.setBackground(shape(card, purple));
        root.addView(info, params(-1, 160));

        setContentView(root);
    }

    void showBrowser(String address) {
        root = column();
        root.setBackgroundColor(bg);

        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(6), dp(8), dp(6), dp(8));
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("←", 24);
        bar.addView(back, params(42, 48));
        back.setOnClickListener(v -> showHome());

        addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(white);
        addressBox.setHintTextColor(Color.LTGRAY);
        addressBox.setHint("वेबसाइट या खोज लिखें");
        addressBox.setTextSize(14);
        addressBox.setPadding(dp(10), 0, dp(10), 0);
        addressBox.setBackground(shape(card, blue));

        bar.addView(addressBox,
            new LinearLayout.LayoutParams(0, dp(48), 1));

        TextView go = text("GO", 16);
        bar.addView(go, params(48, 48));

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

        webView.setWebViewClient(new WebViewClient());

        root.addView(webView,
            new LinearLayout.LayoutParams(-1, 0, 1));

        View.OnClickListener load = v -> {
            String q = addressBox.getText().toString().trim();
            if (!q.isEmpty()) loadAddress(q);
        };

        go.setOnClickListener(load);

        addressBox.setOnEditorActionListener((v, action, event) -> {
            load.onClick(v);
            return true;
        });

        addressBox.setText(address);
        loadAddress(address);

        setContentView(root);
    }

    void loadAddress(String value) {
        if (value.contains(".") && !value.contains(" ")) {
            if (!value.startsWith("http://") &&
                !value.startsWith("https://")) {
                value = "https://" + value;
            }
        } else {
            value = "https://www.google.com/search?q=" +
                android.net.Uri.encode(value);
        }

        addressBox.setText(value);
        webView.loadUrl(value);
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

