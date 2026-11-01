
package com.mahendra.bijmaaibrowser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int BG = Color.rgb(7, 10, 25);
    private final int CARD = Color.rgb(25, 30, 55);
    private final int PURPLE = Color.rgb(115, 45, 255);
    private final int WHITE = Color.WHITE;

    private LinearLayout root;
    private FrameLayout browserFrame;
    private WebView webView;
    private VideoView videoView;
    private TextToSpeech tts;

    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;

    private final ArrayList<String> channelNames = new ArrayList<>();
    private final ArrayList<String> channelUrls = new ArrayList<>();

    private ActivityResultLauncher<Intent> speechLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {
                        ArrayList<String> words = result.getData()
                                .getStringArrayListExtra(
                                        RecognizerIntent.EXTRA_RESULTS);

                        if (words != null && !words.isEmpty()) {
                            voiceCommand(words.get(0));
                        }
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) startListening();
                    else toast("माइक्रोफोन अनुमति नहीं मिली।");
                });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int language = tts.setLanguage(new Locale("hi", "IN"));
                if (language == TextToSpeech.LANG_MISSING_DATA
                        || language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
                speak("Hello Mahendra ji, main aapki kya madad kar sakti hoon?");
            }
        });

        showHome();
    }

    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        d.setStroke(dp(1), Color.rgb(65, 70, 105));
        return d;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackground(shape(PURPLE));
        return b;
    }

    private void page(String title) {
        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(15), dp(12), dp(15), dp(15));
        setContentView(root);

        TextView back = text("‹   " + title, 22, WHITE);
        back.setPadding(0, dp(8), 0, dp(15));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,
                    null, "bijma_voice");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showHome() {
        closeMedia();

        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(15), dp(15), dp(15), dp(15));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = column();

        TextView logo = text("✦ BIJMA AI   👑 PRO", 27,
                Color.rgb(90, 180, 255));
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(15), 0, dp(8));
        content.addView(logo);

        TextView tagline = text("आपका अपना स्मार्ट मनोरंजन ब्राउज़र",
                15, WHITE);
        tagline.setGravity(Gravity.CENTER);
        content.addView(tagline);

        LinearLayout assistant = column();
        assistant.setPadding(dp(15), dp(15), dp(15), dp(15));
        assistant.setBackground(shape(Color.rgb(48, 22, 90)));

        TextView face = text("👩🏻‍💼", 42, WHITE);
        face.setGravity(Gravity.CENTER);
        assistant.addView(face);

        TextView greeting = text(
                "Hello Mahendra ji!\nमैं आपकी क्या मदद कर सकती हूँ?",
                18, WHITE);
        greeting.setGravity(Gravity.CENTER);
        assistant.addView(greeting);

        Button voice = button("🎙 मुझसे बात करें");
        voice.setOnClickListener(v -> requestVoice());
        assistant.addView(voice);

        LinearLayout.LayoutParams ap =
                new LinearLayout.LayoutParams(-1, -2);
        ap.setMargins(0, dp(18), 0, dp(15));
        content.addView(assistant, ap);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(Color.LTGRAY);
        search.setHint("Google पर कुछ भी खोजें");
        search.setBackground(shape(CARD));
        content.addView(search,
                new LinearLayout.LayoutParams(-1, dp(50)));

        Button searchButton = button("Google Search");
        searchButton.setOnClickListener(v -> {
            String q = search.getText().toString().trim();
            if (!q.isEmpty()) showBrowser(searchUrl(q));
        });
        content.addView(searchButton);

        content.addView(section("✦ मुख्य सुविधाएँ"));
        addRow(content, "▶ YouTube", "📺 Live TV", "▶ IPTV / M3U");
        addRow(content, "🌐 Browser", "🛡 VPN", "🔍 Video Zoom");

        content.addView(section("🎬 मनोरंजन"));
        addRow(content, "🎞 Movies", "📺 Web Series", "📡 TV Shows");
        addRow(content, "♫ Music", "⭐ Favorites", "⬇ Downloads");

        TextView footer = text(
                "BIJMA AI • आपका अपना स्मार्ट ब्राउज़र",
                12, Color.LTGRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(20), 0, dp(10));
        content.addView(footer);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);
    }

    private TextView section(String value) {
        TextView t = text(value, 19, WHITE);
        t.setPadding(0, dp(20), 0, dp(10));
        return t;
    }

    private void addRow(LinearLayout parent,
                        String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        addTile(row, a);
        addTile(row, b);
        addTile(row, c);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(4), 0, dp(4));
        parent.addView(row, p);
    }

    private void addTile(LinearLayout row, String name) {
        TextView tile = text(name, 14, WHITE);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(4), dp(8), dp(4), dp(8));
        tile.setBackground(shape(CARD));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, dp(75), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        row.addView(tile, p);

        tile.setOnClickListener(v -> openFeature(name));
    }

    private void openFeature(String name) {
        if (name.contains("YouTube")) {
            showBrowser("https://m.youtube.com");
        } else if (name.contains("Browser")) {
            showBrowser("https://www.google.com");
        } else if (name.contains("IPTV")) {
            showIptv();
        } else if (name.contains("Live TV")) {
            showLiveTv();
        } else if (name.contains("VPN")) {
            showVpn();
        } else if (name.contains("Video Zoom")) {
            showVideoZoom();
        } else {
            showBrowser(searchUrl(name));
        }
    }

    private String searchUrl(String q) {
        if (q.startsWith("http://") || q.startsWith("https://")) {
            return q;
        }
        if (q.contains(".") && !q.contains(" ")) {
            return "https://" + q;
        }
        return "https://www.google.com/search?q=" + Uri.encode(q);
    }

    private void showBrowser(String address) {
        closeMedia();
        page("BIJMA Browser");

        LinearLayout bar = new LinearLayout(this);

        Button home = button("⌂");
        home.setOnClickListener(v -> showHome());
        bar.addView(home, new LinearLayout.LayoutParams(dp(48), dp(48)));

        EditText addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(WHITE);
        addressBox.setHintTextColor(Color.LTGRAY);
        addressBox.setHint("वेबसाइट या खोज लिखें");
        addressBox.setText(address);
        addressBox.setBackground(shape(CARD));
        bar.addView(addressBox,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        Button go = button("GO");
        bar.addView(go, new LinearLayout.LayoutParams(dp(55), dp(48)));
        root.addView(bar);

        browserFrame = new FrameLayout(this);
        webView = new WebView(this);
        webView.setBackgroundColor(WHITE);

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

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view,
                    CustomViewCallback callback) {
                if (fullscreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                fullscreenView = view;
                fullscreenCallback = callback;
                browserFrame.addView(view,
                        new FrameLayout.LayoutParams(-1, -1));

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onHideCustomView() {
                exitFullscreen();
            }
        });

        browserFrame.addView(webView,
                new FrameLayout.LayoutParams(-1, -1));

        root.addView(browserFrame,
                new LinearLayout.LayoutParams(-1, 0, 1));

        View.OnClickListener load = v -> {
            String q = addressBox.getText().toString().trim();
            if (!q.isEmpty()) webView.loadUrl(searchUrl(q));
        };

        go.setOnClickListener(load);
        addressBox.setOnEditorActionListener((v, action, event) -> {
            load.onClick(go);
            return true;
        });

        webView.loadUrl(address);
    }

    private void exitFullscreen() {
        if (fullscreenView != null && browserFrame != null) {
            browserFrame.removeView(fullscreenView);
            fullscreenView = null;
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE);

            if (fullscreenCallback != null) {
                fullscreenCallback.onCustomViewHidden();
                fullscreenCallback = null;
            }
        }
    }

    private void showIptv() {
        closeMedia();
        page("IPTV / M3U Player");

        root.addView(text(
                "अपनी वैध M3U या M3U8 प्लेलिस्ट का लिंक डालें।",
                15, WHITE));

        EditText link = new EditText(this);
        link.setSingleLine(true);
        link.setTextColor(WHITE);
        link.setHintTextColor(Color.LTGRAY);
        link.setHint("https://example.com/playlist.m3u");
        link.setBackground(shape(CARD));
        root.addView(link,
                new LinearLayout.LayoutParams(-1, dp(52)));

        Button load = button("प्लेलिस्ट लोड करें");
        root.addView(load);

        TextView status = text("कोई प्लेलिस्ट लोड नहीं हुई।",
                14, Color.LTGRAY);
        root.addView(status);

        load.setOnClickListener(v -> {
            String url = link.getText().toString().trim();

            if (!url.startsWith("http")) {
                toast("कृपया सही M3U लिंक डालें।");
                return;
            }

            status.setText("प्लेलिस्ट लोड हो रही है...");
            loadM3u(url, status);
        });
    }

    private void loadM3u(String address, TextView status) {
        new Thread(() -> {
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> urls = new ArrayList<>();
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(address).openConnection();
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty(
                        "User-Agent", "BIJMA-AI/1.0");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

                String line;
                String channel = "TV Channel";

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.startsWith("#EXTINF")) {
                        int comma = line.lastIndexOf(',');
                        if (comma >= 0) {
                            channel = line.substring(comma + 1).trim();
                        }
                    } else if (!line.isEmpty()
                            && !line.startsWith("#")) {
                        names.add(channel);
                        urls.add(line);
                        channel = "TV Channel";
                    }
                }

                reader.close();

                runOnUiThread(() -> {
                    channelNames.clear();
                    channelUrls.clear();
                    channelNames.addAll(names);
                    channelUrls.addAll(urls);

                    status.setText(names.size() + " चैनल मिले।");

                    if (!names.isEmpty()) showChannelList();
                    else toast("इस लिंक में चैनल नहीं मिले।");
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        status.setText("लिंक लोड नहीं हुआ। लिंक जाँचें।"));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showChannelList() {
        page("आपके TV चैनल");

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column();

        for (int i = 0; i < channelNames.size(); i++) {
            final int index = i;
            Button item = button("▶  " + channelNames.get(i));
            item.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            item.setOnClickListener(v ->
                    playVideo(channelUrls.get(index),
                            channelNames.get(index)));
            list.addView(item,
                    new LinearLayout.LayoutParams(-1, dp(52)));
        }

        scroll.addView(list);
        root.addView(scroll);
    }

    private void showLiveTv() {
        page("Live TV");

        root.addView(text(
                "Live TV के लिए वैध M3U/M3U8 चैनल सूची जोड़ें।",
                16, WHITE));

        Button open = button("IPTV / M3U खोलें");
        open.setOnClickListener(v -> showIptv());
        root.addView(open);

        root.addView(text(
                "बिना चैनल लिंक के TV चैनल अपने-आप नहीं चलेंगे।",
                13, Color.LTGRAY));
    }

    private void playVideo(String address, String title) {
        closeMedia();
        page(title);

        videoView = new VideoView(this);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        root.addView(videoView,
                new LinearLayout.LayoutParams(-1, 0, 1));

        videoView.setVideoURI(Uri.parse(address));
        videoView.setOnErrorListener((mp, what, extra) -> {
            toast("यह वीडियो स्ट्रीम समर्थित नहीं है।");
            return true;
        });
        videoView.start();
    }

    private void showVideoZoom() {
        closeMedia();
        page("Video Zoom");

        EditText address = new EditText(this);
        address.setSingleLine(true);
        address.setTextColor(WHITE);
        address.setHintTextColor(Color.LTGRAY);
        address.setHint("वीडियो URL डालें");
        address.setBackground(shape(CARD));
        root.addView(address,
                new LinearLayout.LayoutParams(-1, dp(50)));

        FrameLayout frame = new FrameLayout(this);
        videoView = new VideoView(this);
        frame.addView(videoView,
                new FrameLayout.LayoutParams(-1, dp(230)));

        root.addView(frame,
                new LinearLayout.LayoutParams(-1, dp(230)));

        Button play = button("वीडियो चलाएँ");
        play.setOnClickListener(v -> {
            String url = address.getText().toString().trim();
            if (!url.startsWith("http")) {
                toast("सही वीडियो लिंक डालें।");
                return;
            }
            videoView.setVideoURI(Uri.parse(url));
            videoView.start();
        });
        root.addView(play);
    }
}
