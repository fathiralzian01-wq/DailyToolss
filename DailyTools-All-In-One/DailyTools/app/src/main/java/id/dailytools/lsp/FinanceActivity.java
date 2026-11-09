package id.dailytools.lsp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Catatan pemasukan & pengeluaran. UI = WebView (assets/finance), data = finance.json di storage app. */
public class FinanceActivity extends Activity {
    private static final int BG = 0xFF0A0E1A;
    private WebView web;
    private File dataFile;

    @Override
    @SuppressWarnings("SetJavaScriptEnabled")
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        dataFile = new File(getFilesDir(), "finance.json");
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        web = new WebView(this);
        web.setBackgroundColor(BG);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(false);
        ws.setAllowContentAccess(false);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return !r.getUrl().toString().startsWith("file:///android_asset/");
            }
        });
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        web.loadUrl("file:///android_asset/finance/index.html");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // beri kesempatan JS menutup sheet/form dulu
        web.evaluateJavascript("(window.__back&&window.__back())===true", v -> {
            if (!"true".equals(v)) FinanceActivity.super.onBackPressed();
        });
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("Android");
            web.destroy();
        }
        super.onDestroy();
    }

    private class Bridge {
        @JavascriptInterface
        public String load() {
            try {
                if (!dataFile.exists()) return "[]";
                return new String(Files.readAllBytes(dataFile.toPath()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                return "[]";
            }
        }

        @JavascriptInterface
        public boolean save(String json) {
            try {
                if (json == null || json.length() > 5_000_000) return false;
                new JSONArray(json); // pastikan valid
                File tmp = new File(getFilesDir(), "finance.json.tmp");
                Files.write(tmp.toPath(), json.getBytes(StandardCharsets.UTF_8));
                Files.move(tmp.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @JavascriptInterface
        public void copy(String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("keuangan", text));
                Toast.makeText(FinanceActivity.this, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public void openModule() {
            runOnUiThread(() -> startActivity(new Intent(FinanceActivity.this, SettingsActivity.class)));
        }
    }
}
