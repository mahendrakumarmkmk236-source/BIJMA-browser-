package com.mahendra.bijmaaibrowser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int BG = Color.rgb(7, 10, 25);
    private final int CARD = Color.rgb(20, 25, 49);
    private final int PURPLE = Color.rgb(115, 45, 255);
    private final int BLUE = Color.rgb(0, 145, 230);
    private final int WHITE = Color.WHITE;

    private LinearLayout root;
    private WebView webView;
    private VideoView videoView;
    private TextToSpeech tts;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private String currentM3uUrl = "";
    private final ArrayList<String> channelNames = new ArrayList<>();
    private final ArrayList<String> channelUrls = new ArrayList<>();

    private ActivityResultLauncher<Intent> speechLauncher;
    private ActivityResultLauncher<String> audioPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("hi", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.US);
                }
                speak("Hello Mahendra ji, main aapki kya madad kar sakti hoon?");
            }
        });

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) runVoiceCommand(matches.get(0));
                    }
                });

        audioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) startVoiceInput();
                    else toast("माइक की अनुमति नहीं मिली।");
                });

        showHome();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView label(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(color);
        text.setTextSize(size);
        text.setGravity(Gravity.CENTER_VERTICAL);
        return text;
    }

    private GradientBox background(int color) {
        return new GradientBox(color);
    }

    private class GradientBox extends android.graphics.drawable.GradientDrawable {
        GradientBox(int color) {
            setColor(color);
            setCornerRadius(dp(18));
            setStroke(dp(1), Color.rgb(55, 62, 100));
        }
    }

    private LinearLayout page(String title) {
        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(12), dp(16), dp(16));
        setContentView(root);
        TextView back = label("‹   " + title, 22, WHITE);
        back.setPadding(0, dp(8), 0, dp(16));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
        return root;
    }

    private void showHome() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }

        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(16), dp(18), dp(16), dp(18));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = column();
        content.setPadding(0, 0, 0, dp(20));

        TextView logo = label("✦  BIJMA AI     👑 PRO", 27, Color.rgb(80, 185, 255));
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(14), 0, dp(8));
        content.addView(logo);

        TextView tagline = label("आपका स्मार्ट मनोरंजन ब्राउज़र", 16, WHITE);
        tagline.setGravity(Gravity.CENTER);
        content.addView(tagline);

        LinearLayout assistant = column();
        assistant.setPadding(dp(16), dp(16), dp(16), dp(16));
        assistant.setBackground(background(Color.rgb(45, 22, 90)));
        TextView face = label("👩🏻‍💼", 42, WHITE);
        face.setGravity(Gravity.CENTER);
        assistant.addView(face);
        TextView greeting = label("Hello Mahendra ji!\nमैं आपकी क्या मदद कर सकती हूँ?", 18, WHITE);
        greeting.setGravity(Gravity.CENTER);
        greeting.setPadding(0, dp(8), 0, dp(10));
        assistant.addView(greeting);
        Button voice = button("🎙  मुझसे बात करें");
        voice.setOnClickListener(v -> requestVoiceInput());
        assistant.addView(voice);
        LinearLayout.LayoutParams assistantParams =
                new LinearLayout.LayoutParams(-1, -2);
        assistantParams.setMargins(0, dp(18), 0, dp(18));
        content.addView(assistant, assistantParams);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(Color.LTGRAY);
        search.setHint("🔎  Google पर कुछ भी खोजें");
        search.setPadding(dp(14), 0, dp(14), 0);
        search.setBackground(background(CARD));
        content.addView(search, new LinearLayout.LayoutParams(-1, dp(52)));
        Button searchButton = button("Google Search");
        searchButton.setOnClickListener(v -> {
            String q = search.getText().toString().trim();
            if (!q.isEmpty()) showBrowser(makeSearchUrl(q));
        });
        content.addView(searchButton);

        content.addView(sectionTitle("✦  मुख्य सुविधाएँ"));
        addTileRow(content, "▶ YouTube", "📺 Live TV", "▶ IPTV / M3U");
        addTileRow(content, "🌐 Browser", "🛡 VPN", "🔍 Video Zoom");
        content.addView(sectionTitle("🎬  मनोरंजन"));
        addTileRow(content, "🎞 Movies", "📺 Web Series", "📡 TV Shows");
        addTileRow(content, "♫ Music", "⭐ Favorites", "⬇ Downloads");

        TextView note = label("BIJMA AI  •  आपका अपना स्मार्ट ब्राउज़र", 12, Color.LTGRAY);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(22), 0, dp(10));
        content.addView(note);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);
    }

    private TextView sectionTitle(String value) {
        TextView title = label(value, 19, WHITE);
        title.setPadding(0, dp(22), 0, dp(10));
        return title;
    }

    private Button button(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackground(background(PURPLE));
        return b;
    }

    private void addTileRow(LinearLayout parent, String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        addTile(row, a);
        addTile(row, b);
        addTile(row, c);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(4), 0, dp(4));
        parent.addView(row, p);
    }

    private void addTile(LinearLayout row, String name) {
        TextView tile = label(name, 14, WHITE);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(5), dp(12), dp(5), dp(12));
        tile.setBackground(background(CARD));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(76), 1);
        p.setMargins(dp(3), 0, dp(3), 0);
        row.addView(tile, p);
        tile.setOnClickListener(v -> openFeature(name));
    }

    private void openFeature(String name) {
        if (name.contains("YouTube")) showBrowser("https://m.youtube.com");
        else if (name.contains("Browser")) showBrowser("https://www.google.com");
        else if (name.contains("IPTV")) showIptv();
        else if (name.contains("Live TV")) showLiveTv();
        else if (name.contains("VPN")) showVpn();
        else if (name.contains("Video Zoom")) showVideoZoom();
        else if (name.contains("Movies") || name.contains("Web Series") ||
                name.contains("TV Shows")) showBrowser("https://www.google.com/search?q=" +
                Uri.encode(name.replaceAll("[^A-Za-z ]", "")));
        else showInfo(name, "यह सेक्शन अगले अपडेट में जोड़ा जाएगा।");
    }

    private String makeSearchUrl(String q) {
        if (q.startsWith("http://") || q.startsWith("https://")) return q;
        if (q.contains(".") && !q.contains(" ")) return "https://" + q;
        return "https://www.google.com/search?q=" + Uri.encode(q);
    }

    private void showBrowser(String address) {
        page("BIJMA Browser");
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        Button home = button("⌂");
        home.setOnClickListener(v -> showHome());
        bar.addView(home, new LinearLayout.LayoutParams(dp(48), dp(48)));

        EditText addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(WHITE);
        addressBox.setHintTextColor(Color.LTGRAY);
        addressBox.setHint("वेबसाइट या खोज लिखें");
        addressBox.setText(address);
        addressBox.setTextSize(14);
        addressBox.setPadding(dp(8), 0, dp(8), 0);
        addressBox.setBackground(background(CARD));
        bar.addView(addressBox, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button go = button("GO");
        bar.addView(go, new LinearLayout.LayoutParams(dp(60), dp(48)));
        root.addView(bar);

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
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                root.addView(customView, new LinearLayout.LayoutParams(-1, -1));
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                root.removeView(customView);
                customView = null;
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                if (customViewCallback != null) customViewCallback.onCustomViewHidden();
                customViewCallback = null;
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        View.OnClickListener load = v -> {
            String value = addressBox.getText().toString().trim();
            if (!value.isEmpty()) webView.loadUrl(makeSearchUrl(value));
        };
        go.setOnClickListener(load);
        addressBox.setOnEditorActionListener((v, action, event) -> {
            load.onClick(go);
            return true;
        });
        webView.loadUrl(address);
    }

    private void showIptv() {
        page("IPTV / M3U Player");
        TextView help = label("अपनी वैध M3U या M3U8 प्लेलिस्ट का लिंक डालें।", 15, WHITE);
        root.addView(help);
        EditText urlBox = new EditText(this);
        urlBox.setSingleLine(true);
        urlBox.setTextColor(WHITE);
        urlBox.setHintTextColor(Color.LTGRAY);
        urlBox.setHint("https://example.com/playlist.m3u");
        urlBox.setBackground(background(CARD));
        root.addView(urlBox, new LinearLayout.LayoutParams(-1, dp(52)));
        Button load = button("प्लेलिस्ट लोड करें");
        root.addView(load);
        TextView status = label("कोई प्लेलिस्ट लोड नहीं हुई है।", 14, Color.LTGRAY);
        root.addView(status);
        load.setOnClickListener(v -> {
            String link = urlBox.getText().toString().trim();
            if (link.isEmpty() || !link.startsWith("http")) {
                toast("कृपया सही M3U लिंक डालें।");
                return;
            }
            currentM3uUrl = link;
            status.setText("प्लेलिस्ट लोड हो रही है...");
            loadM3u(link, status);
        });
    }

    private void loadM3u(String link, TextView status) {
        new Thread(() -> {
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> urls = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(link).openConnection();
                connection.setConnectTimeout(12000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "BIJMA-AI/1.0");
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                String name = "Channel";
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#EXTINF")) {
                        int comma = line.lastIndexOf(',');
                        name = comma >= 0 ? line.substring(comma + 1).trim() : "Channel";
                    } else if (!line.isEmpty() && !line.startsWith("#")) {
                        names.add(name);
                        urls.add(line);
                        name = "Channel";
                    }
                }
                reader.close();
                runOnUiThread(() -> {
                    channelNames.clear();
                    channelUrls.clear();
                    channelNames.addAll(names);
                    channelUrls.addAll(urls);
                    status.setText(names.size() + " चैनल मिले।");
                    if (names.isEmpty()) toast("इस लिंक में चैनल नहीं मिले।");
                    else showChannelList();
                });
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("प्लेलिस्ट लोड नहीं हुई: लिंक जाँचें।"));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showChannelList() {
        page("आपके TV चैनल");
        if (channelNames.isEmpty()) {
            root.addView(label("कोई चैनल उपलब्ध नहीं।", 16, WHITE));
            return;
        }
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column();
        for (int i = 0; i < channelNames.size(); i++) {
            final int index = i;
            Button item = button("▶  " + channelNames.get(i));
            item.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            item.setOnClickListener(v -> playVideo(channelUrls.get(index), channelNames.get(index)));
            list.addView(item, new LinearLayout.LayoutParams(-1, dp(52)));
        }
        scroll.addView(list);
        root.addView(scroll);
    }

    private void showLiveTv() {
        page("Live TV");
        root.addView(label("Live TV चलाने के लिए अपनी वैध M3U/M3U8 चैनल सूची IPTV सेक्शन में जोड़ें।", 16, WHITE));
        Button go = button("IPTV / M3U खोलें");
        go.setOnClickListener(v -> showIptv());
        root.addView(go);
        root.addView(label("बिना वैध चैनल लिंक के कोई चैनल अपने-आप उपलब्ध नहीं होगा।", 13, Color.LTGRAY));
    }

    private void playVideo(String url, String title) {
        page(title);
        videoView = new VideoView(this);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        root.addView(videoView, new LinearLayout.LayoutParams(-1, 0, 1));
        videoView.setVideoURI(Uri.parse(url));
        videoView.setOnErrorListener((mp, what, extra) -> {
            toast("यह वीडियो फ़ॉर्मेट या स्ट्रीम समर्थित नहीं है।");
            return true;
        });
        videoView.start();
    }

    private void showVideoZoom() {
        page("Video Zoom");
        root.addView(label("वीडियो लिंक डालें। ज़ूम बटन से वीडियो का आकार बदलें।", 15, WHITE));
        EditText url = new EditText(this);
        url.setSingleLine(true);
        url.setTextColor(WHITE);
        url.setHintTextColor(Color.LTGRAY);
        url.setHint("वीडियो URL");
        url.setBackground(background(CARD));
        root.addView(url, new LinearLayout.LayoutParams(-1, dp(50)));
        FrameLayout frame = new FrameLayout(this);
        videoView = new VideoView(this);
        frame.addView(videoView, new FrameLayout.LayoutParams(-1, dp(230)));
        root.addView(frame, new LinearLayout.LayoutParams(-1, dp(230)));
        Button play = button("वीडियो चलाएँ");
        play.setOnClickListener(v -> {
            String link = url.getText().toString().trim();
            if (!link.startsWith("http")) {
                toast("सही वीडियो लिंक डालें।");
                return;
            }
            videoView.setVideoURI(Uri.parse(link));
            videoView.start();
        });
        root.addView(play);
        LinearLayout controls = new LinearLayout(this);
        Button zoomIn = button("＋ ज़ूम");
        Button zoomOut = button("－ ज़ूम");
        controls.addView(zoomIn, new LinearLayout.LayoutParams(0, dp(52), 1));
       
