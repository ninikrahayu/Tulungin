import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:intl/date_symbol_data_local.dart';

import 'data.dart';
import 'admin_screens.dart';
import 'firebase_options.dart';
import 'models.dart';
import 'screens.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await initializeDateFormatting('id_ID');
    await Firebase.initializeApp(options: FirebaseSetup.options);
    runApp(const TulunginApp());
  } catch (error) {
    runApp(FirebaseSetupError(error: error.toString()));
  }
}

class TulunginApp extends StatelessWidget {
  const TulunginApp({super.key});

  static const primary = Color(0xFF0F282F);
  static const mint = Color(0xFFD7F3F3);

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Tulungin',
    debugShowCheckedModeBanner: false,
    theme: ThemeData(
      useMaterial3: true,
      fontFamily: 'Inter',
      colorScheme: ColorScheme.fromSeed(
        seedColor: primary,
        primary: primary,
        secondary: mint,
        surface: Colors.white,
      ),
      scaffoldBackgroundColor: Colors.white,
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFFF9F9F9),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: Color(0xFFCAC4D0)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: Color(0xFFCAC4D0)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
          borderSide: const BorderSide(color: primary, width: 1.5),
        ),
      ),
    ),
    home: const AuthGate(),
  );
}

class FirebaseSetupError extends StatelessWidget {
  const FirebaseSetupError({super.key, required this.error});
  final String error;

  @override
  Widget build(BuildContext context) => MaterialApp(
    theme: ThemeData(fontFamily: 'Inter'),
    home: Scaffold(
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 560),
          child: Padding(
            padding: const EdgeInsets.all(28),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Image.asset('assets/images/logo.png', width: 82, height: 82),
                const SizedBox(height: 18),
                const Text(
                  'Firebase perlu dikonfigurasi',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                Text(error, textAlign: TextAlign.center),
                const SizedBox(height: 16),
                const Text(
                  'Lihat README.md untuk konfigurasi Android, iOS, dan Web.',
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ),
    ),
  );
}

class AuthGate extends StatelessWidget {
  const AuthGate({super.key});

  @override
  Widget build(BuildContext context) => StreamBuilder(
    stream: TulunginAuth.changes,
    builder: (context, authSnapshot) {
      if (authSnapshot.hasError) {
        return _ErrorPage(message: authSnapshot.error.toString());
      }
      if (authSnapshot.connectionState == ConnectionState.waiting) {
        return const _LoadingPage();
      }
      final user = authSnapshot.data;
      if (user == null) return const AuthPage();
      return StreamBuilder<UserProfile>(
        stream: TulunginData.profile(user.uid),
        builder: (context, profileSnapshot) {
          if (profileSnapshot.hasError) {
            return _ErrorPage(message: profileSnapshot.error.toString());
          }
          if (!profileSnapshot.hasData) return const _LoadingPage();
          final profile = profileSnapshot.data!;
          if (!profile.active) {
            TulunginAuth.signOut();
            return const AuthPage(inactiveAccount: true);
          }
          if (profile.role == 'admin') {
            return AdminDashboardPage(profile: profile);
          }
          if (profile.name.trim().isEmpty ||
              profile.phone.trim().isEmpty ||
              profile.address.trim().isEmpty) {
            return CompleteProfilePage(profile: profile);
          }
          return UserDashboardPage(profile: profile);
        },
      );
    },
  );
}

class AuthPage extends StatefulWidget {
  const AuthPage({super.key, this.inactiveAccount = false});
  final bool inactiveAccount;

  @override
  State<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends State<AuthPage> {
  final _formKey = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _phone = TextEditingController();
  final _address = TextEditingController();
  bool _register = false;
  bool _busy = false;
  bool _passwordVisible = false;

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _password.dispose();
    _phone.dispose();
    _address.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _busy = true);
    try {
      if (_register) {
        await TulunginAuth.register(
          name: _name.text,
          email: _email.text,
          password: _password.text,
          phone: _phone.text,
          address: _address.text,
        );
      } else {
        await TulunginAuth.signIn(_email.text, _password.text);
      }
    } catch (error) {
      if (mounted) _showMessage(_friendlyError(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _googleSignIn() async {
    setState(() => _busy = true);
    try {
      await TulunginAuth.signInWithGoogle();
    } catch (error) {
      if (mounted) _showMessage(_friendlyError(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  void _showMessage(String message) => ScaffoldMessenger.of(
    context,
  ).showSnackBar(SnackBar(content: Text(message)));

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    return Scaffold(
      body: Container(
        width: double.infinity,
        height: double.infinity,
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.center,
            colors: [Color(0xFF8DD7D7), Colors.white],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 460),
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(
                  horizontal: 28,
                  vertical: 32,
                ),
                child: Form(
                  key: _formKey,
                  child: Column(
                    children: [
                      Image.asset(
                        'assets/images/logo.png',
                        width: 76,
                        height: 76,
                      ),
                      const SizedBox(height: 18),
                      Text(
                        _register ? 'Buat Akun' : 'Welcome Back',
                        style: const TextStyle(
                          fontSize: 25,
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF0F1E24),
                        ),
                      ),
                      const SizedBox(height: 10),
                      Text(
                        _register
                            ? 'Bergabung dan saling membantu di komunitasmu.'
                            : widget.inactiveAccount
                            ? 'Akun nonaktif. Hubungi admin untuk bantuan.'
                            : 'Masuk dan temukan bantuan di sekitarmu.',
                        textAlign: TextAlign.center,
                        style: const TextStyle(color: Color(0xFF5F6368)),
                      ),
                      SizedBox(height: size.height < 700 ? 20 : 32),
                      if (_register) ...[
                        _textField(_name, 'Nama lengkap', Icons.person_outline),
                        const SizedBox(height: 14),
                      ],
                      _textField(
                        _email,
                        'Email',
                        Icons.email_outlined,
                        keyboard: TextInputType.emailAddress,
                      ),
                      const SizedBox(height: 14),
                      TextFormField(
                        controller: _password,
                        obscureText: !_passwordVisible,
                        decoration: InputDecoration(
                          labelText: 'Password',
                          prefixIcon: const Icon(Icons.lock_outline),
                          suffixIcon: IconButton(
                            onPressed: () => setState(
                              () => _passwordVisible = !_passwordVisible,
                            ),
                            icon: Icon(
                              _passwordVisible
                                  ? Icons.visibility_off
                                  : Icons.visibility,
                            ),
                          ),
                        ),
                        validator: (value) => (value ?? '').length < 6
                            ? 'Password minimal 6 karakter.'
                            : null,
                      ),
                      if (_register) ...[
                        const SizedBox(height: 14),
                        _textField(
                          _phone,
                          'Nomor HP',
                          Icons.phone_outlined,
                          keyboard: TextInputType.phone,
                        ),
                        const SizedBox(height: 14),
                        _textField(
                          _address,
                          'Alamat lengkap',
                          Icons.location_on_outlined,
                        ),
                      ],
                      const SizedBox(height: 22),
                      SizedBox(
                        width: double.infinity,
                        height: 52,
                        child: FilledButton(
                          onPressed: _busy ? null : _submit,
                          child: _busy
                              ? const SizedBox.square(
                                  dimension: 22,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                  ),
                                )
                              : Text(_register ? 'Daftar' : 'Masuk'),
                        ),
                      ),
                      if (!_register) ...[
                        const Padding(
                          padding: EdgeInsets.symmetric(vertical: 18),
                          child: Row(
                            children: [
                              Expanded(child: Divider()),
                              Padding(
                                padding: EdgeInsets.symmetric(horizontal: 12),
                                child: Text('ATAU'),
                              ),
                              Expanded(child: Divider()),
                            ],
                          ),
                        ),
                        SizedBox(
                          width: double.infinity,
                          height: 50,
                          child: OutlinedButton.icon(
                            onPressed: _busy ? null : _googleSignIn,
                            icon: const Icon(Icons.g_mobiledata, size: 28),
                            label: const Text('Lanjutkan dengan Google'),
                          ),
                        ),
                      ],
                      const SizedBox(height: 18),
                      TextButton(
                        onPressed: _busy
                            ? null
                            : () => setState(() => _register = !_register),
                        child: Text(
                          _register
                              ? 'Sudah punya akun? Masuk'
                              : 'Belum punya akun? Buat akun',
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _textField(
    TextEditingController controller,
    String label,
    IconData icon, {
    TextInputType keyboard = TextInputType.text,
  }) => TextFormField(
    controller: controller,
    keyboardType: keyboard,
    decoration: InputDecoration(labelText: label, prefixIcon: Icon(icon)),
    validator: (value) =>
        (value ?? '').trim().isEmpty ? '$label wajib diisi.' : null,
  );
}

class _LoadingPage extends StatelessWidget {
  const _LoadingPage();
  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: CircularProgressIndicator()));
}

class _ErrorPage extends StatelessWidget {
  const _ErrorPage({required this.message});
  final String message;

  @override
  Widget build(BuildContext context) => Scaffold(
    body: Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.cloud_off_outlined, size: 48),
            const SizedBox(height: 16),
            const Text(
              'Tidak dapat memuat aplikasi',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: TulunginAuth.signOut,
              child: const Text('Keluar'),
            ),
          ],
        ),
      ),
    ),
  );
}

String _friendlyError(Object error) {
  final text = error.toString().replaceFirst('Exception: ', '');
  if (text.contains('email-already-in-use')) return 'Email sudah terdaftar.';
  if (text.contains('invalid-credential') || text.contains('wrong-password')) {
    return 'Email atau password salah.';
  }
  if (text.contains('network-request-failed')) {
    return 'Periksa koneksi internet, lalu coba lagi.';
  }
  return text;
}
