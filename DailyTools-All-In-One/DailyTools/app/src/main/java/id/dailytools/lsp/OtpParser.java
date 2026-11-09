package id.dailytools.lsp;

import android.app.Notification;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mencari kode OTP di isi notifikasi. */
public final class OtpParser {
    private static final Pattern KEYWORD = Pattern.compile(
            "(?i)(otp|kode|code|verifikasi|verification|verify|sandi|password|passcode|pin\\b|token|one.?time|"
                    + "jangan (beritahu|bagikan|berikan)|do not share|don't share)");
    private static final Pattern CODE = Pattern.compile("(?<![\\d.,])(\\d{3}[- ]\\d{3}|\\d{4,8})(?![\\d]|[.,]\\d)");

    private OtpParser() {
    }

    /** @return kode OTP atau null kalau tidak ada. */
    public static String extract(Bundle extras) {
        if (extras == null) return null;
        String text = collect(extras);
        if (text.isEmpty() || !KEYWORD.matcher(text).find()) return null;
        Matcher m = CODE.matcher(text);
        while (m.find()) {
            int s = m.start(1);
            String before = text.substring(Math.max(0, s - 4), s).toLowerCase();
            if (before.contains("rp") || before.contains("idr")) continue; // nominal uang, bukan OTP
            return m.group(1).replaceAll("[- ]", "");
        }
        return null;
    }

    private static String collect(Bundle e) {
        StringBuilder sb = new StringBuilder();
        add(sb, e.getCharSequence(Notification.EXTRA_TITLE));
        add(sb, e.getCharSequence(Notification.EXTRA_TEXT));
        add(sb, e.getCharSequence(Notification.EXTRA_BIG_TEXT));
        add(sb, e.getCharSequence(Notification.EXTRA_SUB_TEXT));
        CharSequence[] lines = e.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
        if (lines != null) for (CharSequence l : lines) add(sb, l);
        Parcelable[] msgs = e.getParcelableArray(Notification.EXTRA_MESSAGES);
        if (msgs != null) {
            for (Parcelable p : msgs) {
                if (p instanceof Bundle) add(sb, ((Bundle) p).getCharSequence("text"));
            }
        }
        return sb.toString();
    }

    private static void add(StringBuilder sb, CharSequence cs) {
        if (cs != null && cs.length() > 0) sb.append(cs).append('\n');
    }
}
