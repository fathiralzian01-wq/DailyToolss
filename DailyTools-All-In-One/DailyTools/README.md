# Daily Tools v2.0 (all-in-one)

Satu zip modul KernelSU berisi: APK Daily Tools (modul LSPosed + Catatan Keuangan) + WebUI.

Fitur
- Auto-copy OTP: NotificationListenerService, TANPA LSPosed/scope.
- Bypass FLAG_SECURE, Copy teks dari mana saja, Paksa dark mode: modul LSPosed (scope System Framework + app target).
- WebUI (KernelSU Next) untuk mengatur semuanya.

Struktur repo: app/ (APK), ksu-module/ (isi modul KernelSU), .github/workflows/build.yml (perakit zip).

Build tanpa Android Studio: upload repo ke GitHub, tab Actions > artifact "DailyTools-Module".
Isi artifact itu langsung berupa modul KernelSU yang bisa di-flash.
