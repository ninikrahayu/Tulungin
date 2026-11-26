import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';

class FirebaseSetup {
  static const _projectId = String.fromEnvironment('FIREBASE_PROJECT_ID');
  static const _senderId = String.fromEnvironment(
    'FIREBASE_MESSAGING_SENDER_ID',
  );
  static const _webApiKey = String.fromEnvironment('FIREBASE_WEB_API_KEY');
  static const _webAppId = String.fromEnvironment('FIREBASE_WEB_APP_ID');
  static const _iosApiKey = String.fromEnvironment('FIREBASE_IOS_API_KEY');
  static const _iosAppId = String.fromEnvironment('FIREBASE_IOS_APP_ID');
  static const _authDomain = String.fromEnvironment('FIREBASE_AUTH_DOMAIN');
  static const _storageBucket = String.fromEnvironment(
    'FIREBASE_STORAGE_BUCKET',
  );

  static FirebaseOptions? get options {
    if (defaultTargetPlatform == TargetPlatform.android) return null;

    final isWeb = kIsWeb;
    final apiKey = isWeb ? _webApiKey : _iosApiKey;
    final appId = isWeb ? _webAppId : _iosAppId;
    if (apiKey.isEmpty ||
        appId.isEmpty ||
        _projectId.isEmpty ||
        _senderId.isEmpty) {
      throw StateError(
        'Firebase belum dikonfigurasi untuk ${isWeb ? 'Web' : 'iOS'}. '
        'Ikuti langkah konfigurasi di flutter_app/README.md.',
      );
    }

    return FirebaseOptions(
      apiKey: apiKey,
      appId: appId,
      messagingSenderId: _senderId,
      projectId: _projectId,
      authDomain: isWeb ? _authDomain : null,
      storageBucket: _storageBucket.isEmpty ? null : _storageBucket,
      iosBundleId: isWeb ? null : 'com.pemmob.tulungin',
    );
  }
}
