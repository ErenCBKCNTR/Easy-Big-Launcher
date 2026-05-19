# Easy Big Launcher

This is the finalized source code for Easy Big Launcher with all translations, crashes, intent issues, and localization strings resolved.

### Play Protect Warning Information
Since the application uses high-risk permissions such as `CALL_PHONE`, `SEND_SMS`, `READ_SMS`, and `ACCESS_FINE_LOCATION`, and the debug build is currently an unsigned APK, Google Play Protect might flag it during installation.

To resolve this issue for a production release:
1. Make sure to digitally sign your application with a release keystore before distribution.
2. In the Google Play Developer Console, thoroughly fill out the declaration form explaining why the app needs these specific permissions for its core functionality.
3. Keep the Privacy Policy screen active within the app, as this explicitly informs end-users why these data points are processed locally on the device.

### Feature Completion
- Full strict localizations have been applied to Turkish and English.
- Address field editing cursor jumps have been solved.
- Dialer Haptic feedback implemented.
- BottomSheet used for SMS.
- Gemini Live integrations updated to Deep Link directly.
