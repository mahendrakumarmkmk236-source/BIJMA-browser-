
package com.mahendra.bijmaaibrowser;

import android.Manifest;
import android.app.Activity;
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
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int BG = Color.rgb(7, 10, 25);
    private final int CARD = Color.rgb(25, 30, 55);
    private final int PURPLE = Color.rgb(115, 45, 255);
    private final int WHITE = Color.WHITE;

    private LinearLayout root;
    private WebView webView;
    private VideoView videoView;
    private FrameLayout browserFrame;
    private TextToSpeech tts;

    private View fullscreenView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;

    private final ArrayList<IptvParser.Channel> channels =
            new ArrayList<>();

    private ActivityResultLauncher<Intent> speechLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {
                        ArrayList<String> words =
                                result.getData().getStringArrayListExtra(
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
                tts.setLanguage(new Locale("hi", "IN"));
                speak("नमस्ते, मैं आपकी क्या मदद कर सकती हूँ?");
            }
        });

        showHome();
    }

    private GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(16));
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

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH,
                    null, "bijma_voice");
        }
    }

    private void page(String title) {
        closeMedia();

        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(12), dp(14), dp(14));
        setContentView(root);

        TextView back = text("‹   " + title, 21, WHITE);
        back.setPadding(0, dp(8), 0, dp(14));
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showHome() {
        closeMedia();

        root = column();
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), dp(14), dp(14), dp(14));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = column();

        TextView logo = text("✦ BIJMA AI  PRO", 28,
                Color.rgb(90, 180, 255));
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, dp(18), 0, dp(8));
        content.addView(logo);

        TextView subtitle = text(
                "आपका अपना स्मार्ट मनोरंजन ब्राउज़र",
                15, WHITE);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        LinearLayout assistant = column();
        assistant.setPadding(dp(15), dp(15), dp(15), dp(15));
        assistant.setBackground(shape(Color.rgb(48, 22, 90)));

        TextView greeting = text(
                "👩🏻‍💼\nनमस्ते!\nमैं आपकी क्या मदद कर सकती हूँ?",
                20, WHITE);
        greeting.setGravity(Gravity.CENTER);
        assistant.addView(greeting);

        Button voice = button("🎙 बोलकर बताइए");
        voice.setOnClickListener(v -> requestVoice());
        assistant.addView(voice);

        LinearLayout.LayoutParams ap =
                new LinearLayout.LayoutParams(-1, -2);
        ap.setMargins(0, dp(18), 0, dp(12));
        content.addView(assistant, ap);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE);
        search.setHintTextColor(Color.LTGRAY);
        search.setHint("वेबसाइट या Google खोज लिखें");
        search.setBackground(shape(CARD));
        content.addView(search,
                new LinearLayout.LayoutParams(-1, dp(52)));

        Button searchButton = button("Google Search");
        searchButton.setOnClickListener(v -> {
            String q = search.getText().toString().trim();
            if (!q.isEmpty()) showBrowser(searchUrl(q));
        });
        content.addView(searchButton);

        content.addView(section("मुख्य सुविधाएँ"));
        addRow(content, "🌐 Browser", "▶ YouTube", "📺 Live TV");
        addRow(content, "📡 IPTV / M3U", "🛡 VPN", "🎬 Video Player");

        content.addView(section("अन्य सुविधाएँ"));
        addRow(content, "🎵 Music", "⭐ Favorites", "⬇ Downloads");

        TextView footer = text(
                "BIJMA AI • आपका अपना ब्राउज़र",
                12, Color.LTGRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(22), 0, dp(10));
        content.addView(footer);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);
    }

    private TextView section(String value) {
        TextView t = text(value, 19, WHITE);
        t.setPadding(0, dp(18), 0, dp(8));
        return t;
    }

    private void addRow(LinearLayout parent,
                        String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        addTile(row, a);
        addTile(row, b);
        addTile(row, c);

        parent.addView(row,
                new LinearLayout.LayoutParams(-1, -2));
    }

    private void addTile(LinearLayout row, String name) {
        TextView tile = text(name, 13, WHITE);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(3), dp(8), dp(3), dp(8));
        tile.setBackground(shape(CARD));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, dp(76), 1);
        p.setMargins(dp(3), dp(3), dp(3), dp(3));
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
        } else if (name.contains("Video")) {
            showVideoPlayer();
        } else {
            showBrowser(searchUrl(name));
        }
    }

    private String searchUrl(String q) {
        if (q.startsWith("https://") || q.startsWith("http://")) {
            return q;
        }

        if (q.contains(".") && !q.contains(" ")) {
            return "https://" + q;
        }

        return "https://www.google.com/search?q=" + Uri.encode(q);
    }

    private void showBrowser(String address) {
        page("BIJMA Browser");

        LinearLayout bar = new LinearLayout(this);

        EditText addressBox = new EditText(this);
        addressBox.setSingleLine(true);
        addressBox.setTextColor(WHITE);
        addressBox.setHintTextColor(Color.LTGRAY);
        addressBox.setHint("वेबसाइट लिखें");
        addressBox.setText(address);
        addressBox.setBackground(shape(CARD));

        bar.addView(addressBox,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        Button go = button("GO");
        bar.addView(go,
                new LinearLayout.LayoutParams(dp(65), dp(48)));

        root.addView(bar);

        browserFrame = new FrameLayout(this);
        webView = new WebView(this);

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
        page("IPTV / M3U");

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

            if (!url.startsWith("http://")
                    && !url.startsWith("https://")) {
                toast("कृपया सही M3U लिंक डालें।");
                return;
            }

            status.setText("प्लेलिस्ट लोड हो रही है...");
            loadM3u(url, status);
        });
    }

    private void loadM3u(String address, TextView status) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(address).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty(
                        "User-Agent", "BIJMA-AI/1.0");

                StringBuilder data = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line).append('\n');
                }
                reader.close();

                List<IptvParser.Channel> result =
                        IptvParser.parse(data.toString());

                runOnUiThread(() -> {
                    channels.clear();
                    channels.addAll(result);

                    status.setText(channels.size()
                            + " चैनल मिले।");

                    if (!channels.isEmpty()) {
                        showChannelList();
                    } else {
                        toast("इस प्लेलिस्ट में चैनल नहीं मिले।");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        status.setText(
                                "प्लेलिस्ट लोड नहीं हुई। लिंक जाँचें।"));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showChannelList() {
        page("TV चैनल");

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = column();

        for (int i = 0; i < channels.size(); i++) {
            final IptvParser.Channel channel = channels.get(i);

            Button item = button("▶  " + channel.name);
            item.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

            item.setOnClickListener(v ->
                    playVideo(channel.url, channel.name));

            list.addView(item,
                    new LinearLayout.LayoutParams(-1, dp(54)));
        }

        scroll.addView(list);
        root.addView(scroll);
    }

    private void showLiveTv() {
        page("Live TV");

        root.addView(text(
                "Live TV चलाने के लिए वैध चैनल प्लेलिस्ट चाहिए।",
                16, WHITE));

        Button open = button("IPTV / M3U खोलें");
        open.setOnClickListener(v -> showIptv());
        root.addView(open);
    }

    private void showVideoPlayer() {
        page("Video Player");

        EditText address = new EditText(this);
        address.setSingleLine(true);
        address.setTextColor(WHITE);
        address.setHintTextColor(Color.LTGRAY);
        address.setHint("वीडियो URL डालें");
        address.setBackground(shape(CARD));

        root.addView(address,
                new LinearLayout.LayoutParams(-1, dp(52)));

        Button play = button("वीडियो चलाएँ");
        play.setOnClickListener(v -> {
            String url = address.getText().toString().trim();

            if (!url.startsWith("http")) {
                toast("सही वीडियो URL डालें।");
                return;
            }

            playVideo(url, "Video Player");
        });
        root.addView(play);
    }

    private void playVideo(String address, String title) {
        page(title);

        videoView = new VideoView(this);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        root.addView(videoView,
                new LinearLayout.LayoutParams(-1, 0, 1));

        videoView.setVideoURI(Uri.parse(address));

        videoView.setOnErrorListener((mp, what, extra) -> {
            toast("यह वीडियो स्ट्रीम नहीं चल सकी।");
            return true;
        });

        videoView.start();
    }

    private void showVpn() {
        page("BIJMA AI VPN");

        TextView title = text("🛡 BIJMA VPN", 25, WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView status = text(
                "स्थिति: अभी कनेक्ट नहीं है",
                17, Color.LTGRAY);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        Button setup = button("VPN सेटअप जानकारी");
        setup.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("VPN सर्वर आवश्यक")
                        .setMessage(
                                "वास्तविक VPN चलाने के लिए सर्वर पता, " +
                                "प्रोटोकॉल और प्रमाणित टनल कॉन्फ़िगरेशन " +
                                "आवश्यक हैं। अभी कोई कनेक्शन सक्रिय नहीं है।")
                        .setPositiveButton("ठीक है", null)
                        .show());
        root.addView(setup);
    }

    private void requestVoice() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else {
            permissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startListening() {
        try {
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
  
