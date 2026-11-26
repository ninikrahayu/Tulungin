import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;
import 'package:uuid/uuid.dart';

import 'models.dart';

class TulunginAuth {
  TulunginAuth._();

  static final _auth = FirebaseAuth.instance;
  static final _db = FirebaseFirestore.instance;

  static Stream<User?> get changes => _auth.authStateChanges();

  static Future<void> signIn(String email, String password) async {
    final credential = await _auth.signInWithEmailAndPassword(
      email: email.trim(),
      password: password,
    );
    await _ensureActive(credential.user!);
  }

  static Future<void> register({
    required String name,
    required String email,
    required String password,
    required String phone,
    required String address,
  }) async {
    if (name.trim().length < 2) throw Exception('Nama minimal 2 karakter.');
    if (!RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(email.trim())) {
      throw Exception('Format email belum benar.');
    }
    if (password.length < 6) throw Exception('Password minimal 6 karakter.');
    if (!RegExp(r'^\+?[0-9]{10,15}$').hasMatch(phone.trim())) {
      throw Exception('Nomor HP harus 10–15 digit.');
    }
    if (address.trim().length < 5) throw Exception('Lengkapi alamatmu.');

    final credential = await _auth.createUserWithEmailAndPassword(
      email: email.trim(),
      password: password,
    );
    final user = credential.user!;
    await user.updateDisplayName(name.trim());
    await _db.collection('users').doc(user.uid).set({
      'name': name.trim(),
      'email': email.trim(),
      'phone': phone.trim(),
      'address': address.trim(),
      'photoUrl': '',
      'role': 'user',
      'verified': false,
      'active': true,
      'verificationRequested': false,
      'createdAt': FieldValue.serverTimestamp(),
      'updatedAt': FieldValue.serverTimestamp(),
    });
  }

  static Future<void> signInWithGoogle() async {
    if (kIsWeb) {
      final result = await _auth.signInWithPopup(GoogleAuthProvider());
      await _syncGoogleUser(result.user!);
      return;
    }

    const iosClientId = String.fromEnvironment('GOOGLE_IOS_CLIENT_ID');
    const webClientId = String.fromEnvironment('GOOGLE_WEB_CLIENT_ID');
    await GoogleSignIn.instance.initialize(
      clientId: iosClientId.isEmpty ? null : iosClientId,
      serverClientId: webClientId.isEmpty ? null : webClientId,
    );
    final account = await GoogleSignIn.instance.authenticate();
    final credential = GoogleAuthProvider.credential(
      idToken: account.authentication.idToken,
    );
    final result = await _auth.signInWithCredential(credential);
    await _syncGoogleUser(result.user!);
  }

  static Future<void> _syncGoogleUser(User user) async {
    final ref = _db.collection('users').doc(user.uid);
    final doc = await ref.get();
    if (!doc.exists) {
      await ref.set({
        'name': user.displayName ?? '',
        'email': user.email ?? '',
        'phone': '',
        'address': '',
        'photoUrl': user.photoURL ?? '',
        'role': 'user',
        'verified': user.emailVerified,
        'active': true,
        'verificationRequested': false,
        'createdAt': FieldValue.serverTimestamp(),
        'updatedAt': FieldValue.serverTimestamp(),
      });
    } else {
      await _ensureActive(user);
    }
  }

  static Future<void> _ensureActive(User user) async {
    final ref = _db.collection('users').doc(user.uid);
    final doc = await ref.get();
    if (!doc.exists) {
      await ref.set({
        'name': user.displayName ?? '',
        'email': user.email ?? '',
        'phone': '',
        'address': '',
        'photoUrl': user.photoURL ?? '',
        'role': 'user',
        'verified': user.emailVerified,
        'active': true,
        'verificationRequested': false,
        'createdAt': FieldValue.serverTimestamp(),
        'updatedAt': FieldValue.serverTimestamp(),
      });
      return;
    }
    if (doc.data()?['active'] == false) {
      await _auth.signOut();
      throw Exception('Akun nonaktif. Hubungi admin.');
    }
  }

  static Future<void> signOut() => _auth.signOut();

  static Future<void> saveProfile(UserProfile profile) async {
    await _db.collection('users').doc(profile.id).set({
      'name': profile.name.trim(),
      'email': profile.email.trim(),
      'phone': profile.phone.trim(),
      'address': profile.address.trim(),
      'photoUrl': profile.photoUrl,
      'updatedAt': FieldValue.serverTimestamp(),
    }, SetOptions(merge: true));
  }

  static Future<void> sendVerificationEmail() async {
    final user = _auth.currentUser;
    if (user == null) throw Exception('Sesi login tidak ditemukan.');
    await user.sendEmailVerification();
    await _db.collection('users').doc(user.uid).set({
      'verificationRequested': true,
      'updatedAt': FieldValue.serverTimestamp(),
    }, SetOptions(merge: true));
  }

  static Future<bool> refreshEmailVerification() async {
    final user = _auth.currentUser;
    if (user == null) throw Exception('Sesi login tidak ditemukan.');
    await user.reload();
    final refreshedUser = _auth.currentUser!;
    if (!refreshedUser.emailVerified) {
      await sendVerificationEmail();
      return false;
    }
    await _db.collection('users').doc(refreshedUser.uid).set({
      'verified': true,
      'verificationRequested': false,
      'updatedAt': FieldValue.serverTimestamp(),
    }, SetOptions(merge: true));
    return true;
  }
}

class TulunginData {
  TulunginData._();

  static final db = FirebaseFirestore.instance;
  static final _ids = const Uuid();

  static Stream<UserProfile> profile(String uid) => db
      .collection('users')
      .doc(uid)
      .snapshots()
      .map((doc) => UserProfile.fromDocument(doc));

  static Stream<List<UserJob>> jobs() => db
      .collection('jobs')
      .snapshots()
      .map((query) => query.docs.map(UserJob.fromDocument).toList());

  static Stream<List<JobApplication>> applications() => db
      .collection('applications')
      .snapshots()
      .map((query) => query.docs.map(JobApplication.fromDocument).toList());

  static Stream<List<UserProfile>> users() => db
      .collection('users')
      .snapshots()
      .map((query) => query.docs.map(UserProfile.fromDocument).toList());

  static Stream<List<Map<String, dynamic>>> categories() => db
      .collection('categories')
      .snapshots()
      .map(
        (query) =>
            query.docs.map((doc) => {'id': doc.id, ...doc.data()}).toList(),
      );

  static Future<String> createJob({
    required UserProfile requester,
    required String title,
    required String category,
    required String description,
    required String location,
    required String scheduledAt,
    required int fee,
    double? latitude,
    double? longitude,
  }) async {
    if (title.trim().length < 4) throw Exception('Judul minimal 4 karakter.');
    if (!jobCategories.contains(category)) {
      throw Exception('Pilih kategori bantuan.');
    }
    if (description.trim().length < 10) {
      throw Exception('Detail kebutuhan minimal 10 karakter.');
    }
    if (location.trim().isEmpty || scheduledAt.trim().isEmpty) {
      throw Exception('Lengkapi lokasi serta waktu.');
    }
    if (fee < 1000 || fee > 100000000) {
      throw Exception('Upah harus antara Rp1.000 dan Rp100.000.000.');
    }
    final ref = db.collection('jobs').doc();
    await ref.set({
      'id': ref.id,
      'title': title.trim(),
      'category': category,
      'description': description.trim(),
      'location': location.trim(),
      'locationLat': latitude,
      'locationLng': longitude,
      'scheduledAt': scheduledAt,
      'fee': fee,
      'distanceKm': 0.0,
      'requesterId': requester.id,
      'requesterName': requester.name,
      'helperId': null,
      'helperName': null,
      'status': 'AVAILABLE',
      'proofUri': null,
      'proofName': null,
      'paymentProofUri': null,
      'paymentProofName': null,
      'helperConfirmed': false,
      'requesterConfirmed': false,
      'rating': 0,
      'review': '',
      'paymentMethod': '',
      'paid': false,
      'createdAt': FieldValue.serverTimestamp(),
    });
    return ref.id;
  }

  static Future<void> applyForJob(UserJob job, UserProfile applicant) async {
    if (job.requesterId == applicant.id) {
      throw Exception('Peminta tidak dapat melamar job sendiri.');
    }
    if (job.status != JobStatus.available || job.helperId != null) {
      throw Exception('Job sudah tidak tersedia.');
    }
    if (!applicant.verified) {
      throw Exception('Akun harus terverifikasi untuk mengambil job.');
    }
    final existing = await db
        .collection('applications')
        .where('jobId', isEqualTo: job.id)
        .where('applicantId', isEqualTo: applicant.id)
        .get();
    if (existing.docs.isNotEmpty) throw Exception('Lamaranmu sudah dikirim.');
    await db.collection('applications').add({
      'jobId': job.id,
      'applicantId': applicant.id,
      'applicantName': applicant.name,
      'status': 'pending',
      'createdAt': FieldValue.serverTimestamp(),
    });
  }

  static Future<void> selectApplicant(
    UserJob job,
    JobApplication application,
    UserProfile requester,
  ) async {
    if (job.requesterId != requester.id || application.jobId != job.id) {
      throw Exception('Hanya pemilik job yang dapat memilih helper.');
    }
    if (job.status != JobStatus.available || application.status != 'pending') {
      throw Exception('Lamaran ini sudah diproses.');
    }
    final batch = db.batch();
    batch.update(db.collection('jobs').doc(job.id), {
      'helperId': application.applicantId,
      'helperName': application.applicantName,
      'status': 'IN_PROGRESS',
    });
    batch.update(db.collection('applications').doc(application.id), {
      'status': 'accepted',
    });
    final otherApplications = await db
        .collection('applications')
        .where('jobId', isEqualTo: job.id)
        .where('status', isEqualTo: 'pending')
        .get();
    for (final other in otherApplications.docs) {
      if (other.id != application.id) {
        batch.update(other.reference, {'status': 'rejected'});
      }
    }
    await batch.commit();
  }

  static Future<void> startJob(UserJob job, UserProfile helper) async {
    if (job.helperId != helper.id ||
        !{JobStatus.accepted, JobStatus.inProgress}.contains(job.status)) {
      throw Exception('Job belum dapat dimulai.');
    }
    await db.collection('jobs').doc(job.id).update({'status': 'IN_PROGRESS'});
  }

  static Future<void> submitProof(
    UserJob job,
    UserProfile profile,
    String url,
    String name, {
    required bool payment,
  }) async {
    if (payment) {
      if (job.requesterId != profile.id) {
        throw Exception('Hanya peminta yang dapat mengirim bukti pembayaran.');
      }
      if ((job.proofUri ?? '').isEmpty) {
        throw Exception(
          'Penulung harus mengirim bukti pekerjaan terlebih dahulu.',
        );
      }
    } else {
      if (job.helperId != profile.id || job.status != JobStatus.inProgress) {
        throw Exception('Mulai pekerjaan sebelum mengirim bukti.');
      }
    }
    if (url.isEmpty || name.isEmpty) {
      throw Exception('Pilih berkas bukti terlebih dahulu.');
    }
    await db.collection('jobs').doc(job.id).update({
      if (payment) 'paymentProofUri': url else 'proofUri': url,
      if (payment) 'paymentProofName': name else 'proofName': name,
      if (payment) 'status': 'AWAITING_CONFIRMATION',
    });
  }

  static Future<void> confirmCompletion(
    UserJob job,
    UserProfile profile,
  ) async {
    if ((job.proofUri ?? '').isEmpty || (job.paymentProofUri ?? '').isEmpty) {
      throw Exception(
        'Bukti pekerjaan dan pembayaran harus dikirim terlebih dahulu.',
      );
    }
    final isHelper = job.helperId == profile.id;
    final isRequester = job.requesterId == profile.id;
    if (!isHelper && !isRequester) {
      throw Exception('Hanya pihak yang terlibat yang dapat mengonfirmasi.');
    }
    final helperConfirmed = job.helperConfirmed || isHelper;
    final requesterConfirmed = job.requesterConfirmed || isRequester;
    await db.collection('jobs').doc(job.id).update({
      'helperConfirmed': helperConfirmed,
      'requesterConfirmed': requesterConfirmed,
      if (helperConfirmed && requesterConfirmed) 'status': 'COMPLETED',
    });
  }

  static Future<void> submitReview(
    UserJob job,
    UserProfile requester,
    int rating,
    String text,
  ) async {
    if (job.requesterId != requester.id || job.status != JobStatus.completed) {
      throw Exception('Ulasan diberikan peminta setelah pekerjaan selesai.');
    }
    if (job.rating != 0) throw Exception('Ulasan sudah dikirim.');
    if (rating < 1 || rating > 5 || text.trim().isEmpty) {
      throw Exception('Pilih bintang dan tulis ulasanmu.');
    }
    await db.collection('jobs').doc(job.id).update({
      'rating': rating,
      'review': text.trim(),
    });
  }

  static Future<void> pay(
    UserJob job,
    UserProfile requester,
    String method,
  ) async {
    if (job.requesterId != requester.id || job.status != JobStatus.completed) {
      throw Exception('Pembayaran tersedia setelah pekerjaan selesai.');
    }
    if (job.paid) {
      throw Exception('Job sudah dibayar.');
    }
    if (!paymentMethods.contains(method)) {
      throw Exception('Pilih metode pembayaran.');
    }
    await db.collection('jobs').doc(job.id).update({
      'paid': true,
      'paymentMethod': method,
    });
  }

  static Stream<List<ChatMessage>> messages(String collection, String id) => db
      .collection(collection)
      .doc(id)
      .collection('messages')
      .orderBy('createdAt')
      .snapshots()
      .map((query) => query.docs.map(ChatMessage.fromDocument).toList());

  static Future<void> sendJobMessage(
    UserJob job,
    UserProfile sender,
    String text,
  ) async {
    final message = text.trim();
    if (message.isEmpty || message.length > 2000) {
      throw Exception('Pesan harus berisi 1–2.000 karakter.');
    }
    await db.collection('jobs').doc(job.id).collection('messages').add({
      'jobId': job.id,
      'senderId': sender.id,
      'senderName': sender.name,
      'message': message,
      'createdAt': FieldValue.serverTimestamp(),
    });
  }

  static String get adminConversationId =>
      'chat_${FirebaseAuth.instance.currentUser!.uid}';

  static Future<void> sendAdminMessage(
    UserProfile sender,
    String text, {
    required bool admin,
  }) async {
    if (text.trim().isEmpty || text.length > 2000) {
      throw Exception('Pesan harus berisi 1–2.000 karakter.');
    }
    final conversationId = admin
        ? _activeAdminConversation
        : adminConversationId;
    if (conversationId == null) {
      throw Exception('Percakapan admin tidak tersedia.');
    }
    final ref = db.collection('conversations').doc(conversationId);
    final messageRef = ref.collection('messages').doc();
    final batch = db.batch();
    batch.set(messageRef, {
      'id': messageRef.id,
      'conversationId': conversationId,
      'senderId': sender.id,
      'senderRole': admin ? 'admin' : 'user',
      'senderName': sender.name,
      'message': text.trim(),
      'createdAt': FieldValue.serverTimestamp(),
      'read': false,
    });
    batch.set(ref, {
      'id': conversationId,
      if (!admin) 'userId': sender.id,
      if (!admin) 'userName': sender.name,
      if (!admin) 'userEmail': sender.email,
      if (admin && _activeAdminUserId != null) 'userId': _activeAdminUserId,
      if (admin && _activeAdminUserName != null)
        'userName': _activeAdminUserName,
      if (admin && _activeAdminEmail != null) 'userEmail': _activeAdminEmail,
      'lastMessage': text.trim(),
      'lastMessageAt': FieldValue.serverTimestamp(),
      'updatedAt': FieldValue.serverTimestamp(),
      'createdAt': FieldValue.serverTimestamp(),
    }, SetOptions(merge: true));
    await batch.commit();
  }

  static String? _activeAdminConversation;
  static String? _activeAdminUserId;
  static String? _activeAdminUserName;
  static String? _activeAdminEmail;

  static void openAdminConversation(Map<String, dynamic> conversation) {
    _activeAdminConversation = conversation['id'] as String?;
    _activeAdminUserId = conversation['userId'] as String?;
    _activeAdminUserName = conversation['userName'] as String?;
    _activeAdminEmail = conversation['userEmail'] as String?;
  }

  static Future<String> sendSupport(String uid, String message) async {
    if (message.trim().isEmpty || message.length > 2000) {
      throw Exception('Pesan bantuan harus berisi 1–2.000 karakter.');
    }
    final ref = db.collection('support_tickets').doc();
    await ref.set({
      'id': ref.id,
      'userId': uid,
      'message': message.trim(),
      'createdAt': FieldValue.serverTimestamp(),
    });
    return ref.id;
  }

  static Future<String> uploadImage(Uint8List bytes, String filename) async {
    const cloudName = String.fromEnvironment('CLOUDINARY_CLOUD_NAME');
    const uploadPreset = String.fromEnvironment('CLOUDINARY_UPLOAD_PRESET');
    if (cloudName.isEmpty || uploadPreset.isEmpty) {
      throw Exception(
        'Unggahan belum dikonfigurasi. Isi CLOUDINARY_CLOUD_NAME dan '
        'CLOUDINARY_UPLOAD_PRESET sesuai preset unsigned Cloudinary.',
      );
    }
    final request =
        http.MultipartRequest(
            'POST',
            Uri.https('api.cloudinary.com', '/v1_1/$cloudName/image/upload'),
          )
          ..fields['upload_preset'] = uploadPreset
          ..files.add(
            http.MultipartFile.fromBytes('file', bytes, filename: filename),
          );
    final response = await request.send();
    final body = await response.stream.bytesToString();
    if (response.statusCode < 200 || response.statusCode >= 300) {
      final decoded = jsonDecode(body);
      final errorData = decoded is Map<String, dynamic>
          ? decoded['error']
          : null;
      final detail =
          errorData is Map<String, dynamic> && errorData['message'] is String
          ? errorData['message'] as String
          : null;
      throw Exception(
        'Cloudinary menolak unggahan (${response.statusCode})'
        '${detail == null ? '.' : ': $detail'}',
      );
    }
    final decoded = jsonDecode(body);
    final url =
        decoded is Map<String, dynamic> && decoded['secure_url'] is String
        ? decoded['secure_url'] as String
        : null;
    if (url == null || url.isEmpty) {
      throw Exception('Respons unggahan Cloudinary tidak valid.');
    }
    return url;
  }

  static Future<void> setUserActive(UserProfile user, bool active) async {
    if (user.id == FirebaseAuth.instance.currentUser?.uid) {
      throw Exception('Tidak dapat menonaktifkan akun sendiri.');
    }
    await db.collection('users').doc(user.id).update({'active': active});
  }

  static Future<void> deleteUserProfile(UserProfile user) async {
    if (user.id == FirebaseAuth.instance.currentUser?.uid) {
      throw Exception('Tidak dapat menghapus akun sendiri.');
    }
    await db.collection('users').doc(user.id).delete();
  }

  static Future<void> deleteJob(String id) =>
      db.collection('jobs').doc(id).delete();

  static Future<void> saveCategory(String? id, String name) async {
    final trimmed = name.trim();
    if (trimmed.isEmpty) throw Exception('Nama kategori wajib diisi.');
    if (id == null) {
      await db.collection('categories').add({
        'name': trimmed,
        'createdAt': FieldValue.serverTimestamp(),
      });
    } else {
      await db.collection('categories').doc(id).update({'name': trimmed});
    }
  }

  static Future<void> deleteCategory(String id) =>
      db.collection('categories').doc(id).delete();

  static Future<void> ensureDefaultCategories() async {
    final existing = await db.collection('categories').limit(1).get();
    if (existing.docs.isNotEmpty) return;
    final batch = db.batch();
    for (final name in jobCategories) {
      batch.set(db.collection('categories').doc(_ids.v4()), {
        'name': name,
        'createdAt': FieldValue.serverTimestamp(),
      });
    }
    await batch.commit();
  }

  static const jobCategories = [
    'Kebersihan',
    'Pengantaran',
    'Perbaikan',
    'Jasa Rumah',
    'Lainnya',
  ];
  static const paymentMethods = ['QRIS', 'Transfer Bank', 'Tunai'];
}
