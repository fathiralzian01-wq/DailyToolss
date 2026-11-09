#!/system/bin/sh
for k in dt_flag_secure dt_auto_otp dt_copy_text dt_force_dark dt_dark_pkgs; do
  settings delete global "$k" >/dev/null 2>&1
done
cmd notification disallow_listener id.dailytools.lsp/id.dailytools.lsp.OtpListenerService >/dev/null 2>&1
