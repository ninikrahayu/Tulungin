import 'package:cloud_firestore/cloud_firestore.dart';

enum JobStatus {
  available,
  accepted,
  inProgress,
  awaitingConfirmation,
  completed,
  cancelled,
}

extension JobStatusText on JobStatus {
  String get label => switch (this) {
    JobStatus.available => 'Tersedia',
    JobStatus.accepted => 'Job diambil',
    JobStatus.inProgress => 'Sedang dikerjakan',
    JobStatus.awaitingConfirmation => 'Menunggu konfirmasi',
    JobStatus.completed => 'Selesai',
    JobStatus.cancelled => 'Dibatalkan',
  };

  static JobStatus parse(String? value) => switch (value?.toLowerCase()) {
    'accepted' => JobStatus.accepted,
    'in_progress' || 'inprogress' || 'progress' => JobStatus.inProgress,
    'awaiting_confirmation' || 'awaiting' => JobStatus.awaitingConfirmation,
    'completed' => JobStatus.completed,
    'cancelled' => JobStatus.cancelled,
    _ => JobStatus.available,
  };
}

class UserProfile {
  const UserProfile({
    required this.id,
    this.name = '',
    this.email = '',
    this.phone = '',
    this.address = '',
    this.photoUrl = '',
    this.role = 'user',
    this.verified = false,
    this.active = true,
  });

  final String id;
  final String name;
  final String email;
  final String phone;
  final String address;
  final String photoUrl;
  final String role;
  final bool verified;
  final bool active;

  factory UserProfile.fromDocument(DocumentSnapshot<Map<String, dynamic>> doc) {
    final data = doc.data() ?? const <String, dynamic>{};
    return UserProfile(
      id: doc.id,
      name: data['name'] as String? ?? '',
      email: data['email'] as String? ?? '',
      phone: data['phone'] as String? ?? '',
      address: data['address'] as String? ?? '',
      photoUrl: data['photoUrl'] as String? ?? '',
      role: data['role'] as String? ?? 'user',
      verified: data['verified'] as bool? ?? false,
      active: data['active'] as bool? ?? true,
    );
  }
}

class UserJob {
  const UserJob({
    required this.id,
    required this.title,
    required this.category,
    required this.description,
    required this.location,
    required this.scheduledAt,
    required this.fee,
    required this.requesterId,
    required this.requesterName,
    this.distanceKm = 0,
    this.helperId,
    this.helperName,
    this.status = JobStatus.available,
    this.proofUri,
    this.proofName,
    this.paymentProofUri,
    this.paymentProofName,
    this.helperConfirmed = false,
    this.requesterConfirmed = false,
    this.rating = 0,
    this.review = '',
    this.paymentMethod = '',
    this.paid = false,
    this.locationLat,
    this.locationLng,
  });

  final String id;
  final String title;
  final String category;
  final String description;
  final String location;
  final String scheduledAt;
  final int fee;
  final double distanceKm;
  final String requesterId;
  final String requesterName;
  final String? helperId;
  final String? helperName;
  final JobStatus status;
  final String? proofUri;
  final String? proofName;
  final String? paymentProofUri;
  final String? paymentProofName;
  final bool helperConfirmed;
  final bool requesterConfirmed;
  final int rating;
  final String review;
  final String paymentMethod;
  final bool paid;
  final double? locationLat;
  final double? locationLng;

  factory UserJob.fromDocument(DocumentSnapshot<Map<String, dynamic>> doc) {
    final data = doc.data() ?? const <String, dynamic>{};
    return UserJob(
      id: doc.id,
      title: data['title'] as String? ?? '',
      category: data['category'] as String? ?? 'Lainnya',
      description: data['description'] as String? ?? '',
      location: data['location'] as String? ?? '',
      scheduledAt: data['scheduledAt'] as String? ?? '',
      fee: (data['fee'] as num?)?.toInt() ?? 0,
      distanceKm: (data['distanceKm'] as num?)?.toDouble() ?? 0,
      requesterId: data['requesterId'] as String? ?? '',
      requesterName: data['requesterName'] as String? ?? 'Peminta',
      helperId: data['helperId'] as String?,
      helperName: data['helperName'] as String?,
      status: JobStatusText.parse(data['status'] as String?),
      proofUri: data['proofUri'] as String?,
      proofName: data['proofName'] as String?,
      paymentProofUri: data['paymentProofUri'] as String?,
      paymentProofName: data['paymentProofName'] as String?,
      helperConfirmed: data['helperConfirmed'] as bool? ?? false,
      requesterConfirmed: data['requesterConfirmed'] as bool? ?? false,
      rating: (data['rating'] as num?)?.toInt() ?? 0,
      review: data['review'] as String? ?? '',
      paymentMethod: data['paymentMethod'] as String? ?? '',
      paid: data['paid'] as bool? ?? false,
      locationLat: (data['locationLat'] as num?)?.toDouble(),
      locationLng: (data['locationLng'] as num?)?.toDouble(),
    );
  }

  Map<String, dynamic> toMap() => {
    'id': id,
    'title': title,
    'category': category,
    'description': description,
    'location': location,
    'scheduledAt': scheduledAt,
    'fee': fee,
    'distanceKm': distanceKm,
    'requesterId': requesterId,
    'requesterName': requesterName,
    'helperId': helperId,
    'helperName': helperName,
    'status': status.name.replaceAllMapped(
      RegExp(r'[A-Z]'),
      (match) => '_${match.group(0)!.toLowerCase()}',
    ),
    'proofUri': proofUri,
    'proofName': proofName,
    'paymentProofUri': paymentProofUri,
    'paymentProofName': paymentProofName,
    'helperConfirmed': helperConfirmed,
    'requesterConfirmed': requesterConfirmed,
    'rating': rating,
    'review': review,
    'paymentMethod': paymentMethod,
    'paid': paid,
    'locationLat': locationLat,
    'locationLng': locationLng,
  };
}

class JobApplication {
  const JobApplication({
    required this.id,
    required this.jobId,
    required this.applicantId,
    required this.applicantName,
    this.status = 'pending',
  });

  final String id;
  final String jobId;
  final String applicantId;
  final String applicantName;
  final String status;

  factory JobApplication.fromDocument(
    DocumentSnapshot<Map<String, dynamic>> doc,
  ) {
    final data = doc.data() ?? const <String, dynamic>{};
    return JobApplication(
      id: doc.id,
      jobId: data['jobId'] as String? ?? '',
      applicantId: data['applicantId'] as String? ?? '',
      applicantName: data['applicantName'] as String? ?? 'Pengguna',
      status: data['status'] as String? ?? 'pending',
    );
  }
}

class ChatMessage {
  const ChatMessage({
    required this.id,
    required this.senderId,
    required this.senderName,
    required this.text,
    this.createdAt,
  });

  final String id;
  final String senderId;
  final String senderName;
  final String text;
  final DateTime? createdAt;

  factory ChatMessage.fromDocument(DocumentSnapshot<Map<String, dynamic>> doc) {
    final data = doc.data() ?? const <String, dynamic>{};
    final timestamp = data['createdAt'];
    return ChatMessage(
      id: doc.id,
      senderId: data['senderId'] as String? ?? '',
      senderName: data['senderName'] as String? ?? '',
      text: data['message'] as String? ?? '',
      createdAt: timestamp is Timestamp ? timestamp.toDate() : null,
    );
  }
}
