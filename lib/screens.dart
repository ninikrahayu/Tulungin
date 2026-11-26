import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:intl/intl.dart';
import 'package:latlong2/latlong.dart';

import 'data.dart';
import 'models.dart';

const _mint = Color(0xFFD7F3F3);
const _ink = Color(0xFF0F282F);
const _muted = Color(0xFF5F6368);
const _categories = TulunginData.jobCategories;
final _rupiah = NumberFormat.currency(
  locale: 'id_ID',
  symbol: 'Rp',
  decimalDigits: 0,
);

class UserDashboardPage extends StatefulWidget {
  const UserDashboardPage({super.key, required this.profile});
  final UserProfile profile;

  @override
  State<UserDashboardPage> createState() => _UserDashboardPageState();
}

class _UserDashboardPageState extends State<UserDashboardPage> {
  int _tab = 0;

  @override
  Widget build(BuildContext context) => StreamBuilder<List<UserJob>>(
    stream: TulunginData.jobs(),
    builder: (context, jobsSnapshot) {
      if (jobsSnapshot.hasError) {
        return _ErrorScaffold(error: jobsSnapshot.error.toString());
      }
      if (!jobsSnapshot.hasData) return const _LoadingScaffold();
      return StreamBuilder<List<JobApplication>>(
        stream: TulunginData.applications(),
        builder: (context, applicationSnapshot) {
          if (applicationSnapshot.hasError) {
            return _ErrorScaffold(error: applicationSnapshot.error.toString());
          }
          if (!applicationSnapshot.hasData) return const _LoadingScaffold();
          final jobs = jobsSnapshot.data!;
          final applications = applicationSnapshot.data!;
          final views = [
            _HomeTab(
              profile: widget.profile,
              jobs: jobs,
              applications: applications,
              onCreate: _createJob,
              onOpen: (job) => _openJob(job, jobs, applications),
              onBrowse: () => setState(() => _tab = 1),
            ),
            _BrowseTab(
              profile: widget.profile,
              jobs: jobs,
              applications: applications,
              onOpen: (job) => _openJob(job, jobs, applications),
            ),
            _HistoryTab(
              profile: widget.profile,
              jobs: jobs,
              applications: applications,
              onOpen: (job) => _openJob(job, jobs, applications),
            ),
            _ProfileTab(
              profile: widget.profile,
              onLogout: _confirmLogout,
              onEdit: () => _editProfile(),
              onSupport: () => _openSupport(),
              onChats: () => _openChats(),
            ),
          ];
          return Scaffold(
            appBar: AppBar(
              title: Text(
                const ['Beranda', 'Job Available', 'Histori', 'Profil'][_tab],
                style: const TextStyle(fontWeight: FontWeight.w700),
              ),
              actions: [
                if (_tab == 3)
                  IconButton(
                    tooltip: 'Keluar',
                    onPressed: _confirmLogout,
                    icon: const Icon(Icons.logout),
                  ),
              ],
            ),
            body: SafeArea(
              top: false,
              child: IndexedStack(index: _tab, children: views),
            ),
            floatingActionButton: _tab == 3
                ? null
                : FloatingActionButton(
                    onPressed: _createJob,
                    backgroundColor: _ink,
                    foregroundColor: Colors.white,
                    tooltip: 'Buat Permintaan',
                    child: const Icon(Icons.add),
                  ),
            bottomNavigationBar: NavigationBar(
              selectedIndex: _tab,
              onDestinationSelected: (index) => setState(() => _tab = index),
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.home_outlined),
                  selectedIcon: Icon(Icons.home),
                  label: 'Beranda',
                ),
                NavigationDestination(
                  icon: Icon(Icons.work_outline),
                  label: 'Job Avail',
                ),
                NavigationDestination(
                  icon: Icon(Icons.history),
                  label: 'Histori',
                ),
                NavigationDestination(
                  icon: Icon(Icons.person_outline),
                  label: 'Profil',
                ),
              ],
            ),
          );
        },
      );
    },
  );

  Future<void> _openJob(
    UserJob job,
    List<UserJob> jobs,
    List<JobApplication> applications,
  ) async {
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => JobDetailPage(
          profile: widget.profile,
          job: job,
          jobs: jobs,
          applications: applications,
        ),
      ),
    );
  }

  Future<void> _createJob() async {
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => CreateJobPage(profile: widget.profile),
      ),
    );
  }

  Future<void> _editProfile() async {
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => EditProfilePage(profile: widget.profile),
      ),
    );
  }

  Future<void> _openSupport() async {
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => SupportPage(profile: widget.profile),
      ),
    );
  }

  Future<void> _openChats() async {
    await Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => ChatListPage(profile: widget.profile),
      ),
    );
  }

  Future<void> _confirmLogout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Keluar dari akun?'),
        content: const Text('Sesi Anda akan diakhiri.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Batal'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Keluar'),
          ),
        ],
      ),
    );
    if (confirmed == true) await TulunginAuth.signOut();
  }
}

class CompleteProfilePage extends StatefulWidget {
  const CompleteProfilePage({super.key, required this.profile});
  final UserProfile profile;

  @override
  State<CompleteProfilePage> createState() => _CompleteProfilePageState();
}

class _CompleteProfilePageState extends State<CompleteProfilePage> {
  final _formKey = GlobalKey<FormState>();
  late final _name = TextEditingController(text: widget.profile.name);
  late final _phone = TextEditingController(text: widget.profile.phone);
  late final _address = TextEditingController(text: widget.profile.address);
  bool _busy = false;

  @override
  void dispose() {
    _name.dispose();
    _phone.dispose();
    _address.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text('Lengkapi Profil'),
      actions: [
        IconButton(
          tooltip: 'Keluar',
          onPressed: TulunginAuth.signOut,
          icon: const Icon(Icons.logout),
        ),
      ],
    ),
    body: SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 480),
          child: Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.all(22),
              shrinkWrap: true,
              children: [
                const Text(
                  'Satu langkah lagi',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Tambahkan nomor telepon dan alamat agar komunitas bisa berkoordinasi.',
                  style: TextStyle(color: _muted),
                ),
                const SizedBox(height: 18),
                _Field(controller: _name, label: 'Nama Lengkap'),
                const SizedBox(height: 14),
                _Field(
                  controller: _phone,
                  label: 'Nomor HP',
                  keyboard: TextInputType.phone,
                  validator: (value) =>
                      RegExp(r'^\+?[0-9]{10,15}$').hasMatch(value?.trim() ?? '')
                      ? null
                      : 'Nomor HP harus 10–15 digit.',
                ),
                const SizedBox(height: 14),
                _Field(
                  controller: _address,
                  label: 'Alamat Lengkap',
                  lines: 3,
                  validator: (value) => (value?.trim().length ?? 0) >= 5
                      ? null
                      : 'Lengkapi alamatmu.',
                ),
                const SizedBox(height: 18),
                FilledButton(
                  onPressed: _busy ? null : _save,
                  child: _busy
                      ? const CircularProgressIndicator()
                      : const Text('Simpan Profil'),
                ),
              ],
            ),
          ),
        ),
      ),
    ),
  );

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _busy = true);
    try {
      if (_name.text.trim().length < 2) {
        throw Exception('Nama minimal 2 karakter.');
      }
      final updated = UserProfile(
        id: widget.profile.id,
        name: _name.text,
        email: widget.profile.email,
        phone: _phone.text,
        address: _address.text,
        photoUrl: widget.profile.photoUrl,
        verified: widget.profile.verified,
        role: widget.profile.role,
        active: widget.profile.active,
      );
      await TulunginAuth.saveProfile(updated);
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }
}

class _HomeTab extends StatelessWidget {
  const _HomeTab({
    required this.profile,
    required this.jobs,
    required this.applications,
    required this.onCreate,
    required this.onOpen,
    required this.onBrowse,
  });
  final UserProfile profile;
  final List<UserJob> jobs;
  final List<JobApplication> applications;
  final VoidCallback onCreate;
  final ValueChanged<UserJob> onOpen;
  final VoidCallback onBrowse;

  @override
  Widget build(BuildContext context) {
    final active = jobs.where((job) {
      final mine =
          job.requesterId == profile.id ||
          job.helperId == profile.id ||
          applications.any(
            (app) =>
                app.jobId == job.id &&
                app.applicantId == profile.id &&
                app.status == 'accepted',
          );
      return mine &&
          job.status != JobStatus.completed &&
          job.status != JobStatus.cancelled;
    }).toList();
    final available = jobs
        .where(
          (job) =>
              job.status == JobStatus.available &&
              job.requesterId != profile.id,
        )
        .take(3)
        .toList();
    return ListView(
      padding: const EdgeInsets.fromLTRB(22, 14, 22, 90),
      children: [
        Row(
          children: [
            Image.asset('assets/images/logo.png', width: 52, height: 52),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                'Halo, ${profile.name.trim().isEmpty ? 'Pengguna' : profile.name.split(' ').first}!',
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 18),
        Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: _mint,
            borderRadius: BorderRadius.circular(18),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Butuh bantuan?',
                style: TextStyle(
                  color: _ink,
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 7),
              const Text(
                'Buat permintaan dan temukan Penulung untuk membantumu.',
              ),
              const SizedBox(height: 14),
              FilledButton.icon(
                onPressed: onCreate,
                icon: const Icon(Icons.add),
                label: const Text('Buat Permintaan'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 26),
        _SectionTitle(title: 'Aktivitas Saya', onSeeAll: null),
        if (active.isEmpty)
          const _EmptyState(
            title: 'Belum ada aktivitas',
            message: 'Job yang kamu buat atau ambil tampil di sini.',
          )
        else
          ...active
              .take(3)
              .map((job) => _JobCard(job: job, onTap: () => onOpen(job))),
        const SizedBox(height: 18),
        _SectionTitle(title: 'Job Tersedia', onSeeAll: onBrowse),
        if (available.isEmpty)
          const _EmptyState(
            title: 'Belum ada job',
            message: 'Coba lagi nanti atau buat permintaan bantuanmu.',
          )
        else
          ...available.map(
            (job) => _JobCard(job: job, onTap: () => onOpen(job)),
          ),
      ],
    );
  }
}

class _BrowseTab extends StatefulWidget {
  const _BrowseTab({
    required this.profile,
    required this.jobs,
    required this.applications,
    required this.onOpen,
  });
  final UserProfile profile;
  final List<UserJob> jobs;
  final List<JobApplication> applications;
  final ValueChanged<UserJob> onOpen;

  @override
  State<_BrowseTab> createState() => _BrowseTabState();
}

class _BrowseTabState extends State<_BrowseTab> {
  final _search = TextEditingController();
  String _category = 'Semua kategori';
  String _distance = 'Semua jarak';
  LatLng? _currentLocation;

  @override
  void dispose() {
    _search.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final maximum = switch (_distance) {
      'Maksimal 2 km' => 2.0,
      'Maksimal 5 km' => 5.0,
      'Maksimal 10 km' => 10.0,
      _ => double.infinity,
    };
    final jobs = widget.jobs.where((job) {
      final query = _search.text.toLowerCase();
      return job.status == JobStatus.available &&
          job.requesterId != widget.profile.id &&
          (query.isEmpty ||
              '${job.title} ${job.category} ${job.location}'
                  .toLowerCase()
                  .contains(query)) &&
          (_category == 'Semua kategori' || job.category == _category) &&
          (_distance == 'Semua jarak' ||
              (_currentLocation != null &&
                  (job.locationLat != null && job.locationLng != null
                          ? Geolocator.distanceBetween(
                                  _currentLocation!.latitude,
                                  _currentLocation!.longitude,
                                  job.locationLat!,
                                  job.locationLng!,
                                ) /
                                1000
                          : job.distanceKm) <=
                      maximum));
    }).toList();
    return ListView(
      padding: const EdgeInsets.fromLTRB(22, 16, 22, 92),
      children: [
        TextField(
          controller: _search,
          decoration: const InputDecoration(
            labelText: 'Cari Job',
            hintText: 'Cari pekerjaan...',
            prefixIcon: Icon(Icons.search),
          ),
          onChanged: (_) => setState(() {}),
        ),
        const SizedBox(height: 10),
        OutlinedButton.icon(
          onPressed: _showFilters,
          icon: const Icon(Icons.tune),
          label: Text('Filter · $_category · $_distance'),
        ),
        const SizedBox(height: 12),
        Text(
          'Job Tersedia (${jobs.length})',
          style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 10),
        if (jobs.isEmpty)
          const _EmptyState(
            title: 'Job tidak ditemukan',
            message: 'Coba kata kunci lain atau ubah filter.',
          )
        else
          ...jobs.map(
            (job) => _JobCard(job: job, onTap: () => widget.onOpen(job)),
          ),
      ],
    );
  }

  Future<void> _showFilters() async {
    var category = _category;
    var distance = _distance;
    await showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (context) => StatefulBuilder(
        builder: (context, setSheetState) => SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(22, 4, 22, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Filter Job',
                  style: TextStyle(fontSize: 19, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 14),
                DropdownButtonFormField<String>(
                  initialValue: category,
                  decoration: const InputDecoration(labelText: 'Kategori'),
                  items: ['Semua kategori', ..._categories]
                      .map(
                        (item) =>
                            DropdownMenuItem(value: item, child: Text(item)),
                      )
                      .toList(),
                  onChanged: (value) =>
                      setSheetState(() => category = value ?? category),
                ),
                const SizedBox(height: 12),
                DropdownButtonFormField<String>(
                  initialValue: distance,
                  decoration: const InputDecoration(labelText: 'Jarak'),
                  items:
                      [
                            'Semua jarak',
                            'Maksimal 2 km',
                            'Maksimal 5 km',
                            'Maksimal 10 km',
                          ]
                          .map(
                            (item) => DropdownMenuItem(
                              value: item,
                              child: Text(item),
                            ),
                          )
                          .toList(),
                  onChanged: (value) async {
                    final selected = value ?? distance;
                    if (selected == 'Semua jarak') {
                      setSheetState(() => distance = selected);
                      return;
                    }
                    try {
                      var permission = await Geolocator.checkPermission();
                      if (permission == LocationPermission.denied) {
                        permission = await Geolocator.requestPermission();
                      }
                      final granted =
                          permission == LocationPermission.always ||
                          permission == LocationPermission.whileInUse;
                      if (!granted) {
                        if (context.mounted) {
                          _message(
                            context,
                            'Izinkan akses lokasi untuk memfilter jarak.',
                          );
                        }
                        return;
                      }
                      final position = await Geolocator.getCurrentPosition();
                      if (!mounted) return;
                      setState(
                        () => _currentLocation = LatLng(
                          position.latitude,
                          position.longitude,
                        ),
                      );
                      setSheetState(() => distance = selected);
                    } catch (error) {
                      if (context.mounted) _message(context, _friendly(error));
                    }
                  },
                ),
                const SizedBox(height: 18),
                FilledButton(
                  onPressed: () {
                    setState(() {
                      _category = category;
                      _distance = distance;
                    });
                    Navigator.pop(context);
                  },
                  child: const Text('Terapkan Filter'),
                ),
                TextButton(
                  onPressed: () {
                    setState(() {
                      _category = 'Semua kategori';
                      _distance = 'Semua jarak';
                    });
                    Navigator.pop(context);
                  },
                  child: const Text('Reset Filter'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _HistoryTab extends StatelessWidget {
  const _HistoryTab({
    required this.profile,
    required this.jobs,
    required this.applications,
    required this.onOpen,
  });
  final UserProfile profile;
  final List<UserJob> jobs;
  final List<JobApplication> applications;
  final ValueChanged<UserJob> onOpen;

  @override
  Widget build(BuildContext context) {
    final history = jobs.where((job) {
      final isParticipant =
          job.requesterId == profile.id ||
          job.helperId == profile.id ||
          applications.any(
            (app) =>
                app.jobId == job.id &&
                app.applicantId == profile.id &&
                app.status == 'accepted',
          );
      return isParticipant && job.status != JobStatus.available;
    }).toList()..sort((a, b) => b.scheduledAt.compareTo(a.scheduledAt));
    return ListView(
      padding: const EdgeInsets.all(22),
      children: [
        Text(
          'Riwayat & Job Berlangsung (${history.length})',
          style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 12),
        if (history.isEmpty)
          const _EmptyState(
            title: 'Belum ada riwayat',
            message:
                'Job yang diambil, selesai, atau dibatalkan tampil di sini.',
          )
        else
          ...history.map((job) => _JobCard(job: job, onTap: () => onOpen(job))),
      ],
    );
  }
}

class _ProfileTab extends StatelessWidget {
  const _ProfileTab({
    required this.profile,
    required this.onLogout,
    required this.onEdit,
    required this.onSupport,
    required this.onChats,
  });
  final UserProfile profile;
  final VoidCallback onLogout;
  final VoidCallback onEdit;
  final VoidCallback onSupport;
  final VoidCallback onChats;

  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(22),
    children: [
      Card(
        color: _mint,
        child: ListTile(
          leading: _Avatar(url: profile.photoUrl, size: 52),
          title: Text(
            profile.name.isEmpty ? 'Pengguna' : profile.name,
            style: const TextStyle(fontWeight: FontWeight.w700),
          ),
          subtitle: Text(
            profile.verified ? 'Akun terverifikasi' : 'Belum terverifikasi',
          ),
        ),
      ),
      if (!profile.verified)
        _ActionTile(
          icon: Icons.verified_user_outlined,
          title: 'Verifikasi Email',
          onTap: () async {
            try {
              final verified = await TulunginAuth.refreshEmailVerification();
              if (context.mounted) {
                _message(
                  context,
                  verified
                      ? 'Email berhasil diverifikasi.'
                      : 'Email verifikasi dikirim. Periksa inbox, lalu kembali ke halaman profil.',
                );
              }
            } catch (error) {
              if (context.mounted) _message(context, _friendly(error));
            }
          },
        ),
      _InfoCard(label: 'Email', value: profile.email),
      _InfoCard(label: 'Nomor HP', value: profile.phone),
      _InfoCard(label: 'Alamat', value: profile.address),
      const SizedBox(height: 10),
      FilledButton.icon(
        onPressed: onEdit,
        icon: const Icon(Icons.edit_outlined),
        label: const Text('Edit Profil'),
      ),
      const SizedBox(height: 14),
      const Text(
        'Layanan lainnya',
        style: TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
      ),
      _ActionTile(
        icon: Icons.chat_bubble_outline,
        title: 'Chat',
        onTap: onChats,
      ),
      _ActionTile(
        icon: Icons.support_agent,
        title: 'Bantuan',
        onTap: onSupport,
      ),
      OutlinedButton.icon(
        onPressed: onLogout,
        icon: const Icon(Icons.logout),
        label: const Text('Keluar'),
      ),
    ],
  );
}

class CreateJobPage extends StatefulWidget {
  const CreateJobPage({super.key, required this.profile});
  final UserProfile profile;

  @override
  State<CreateJobPage> createState() => _CreateJobPageState();
}

class _CreateJobPageState extends State<CreateJobPage> {
  final _form = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _description = TextEditingController();
  final _location = TextEditingController();
  final _fee = TextEditingController();
  String? _category;
  DateTime? _dateTime;
  LatLng? _point;
  bool _busy = false;

  @override
  void dispose() {
    _title.dispose();
    _description.dispose();
    _location.dispose();
    _fee.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Buat Permintaan')),
    body: Form(
      key: _form,
      child: ListView(
        padding: const EdgeInsets.all(22),
        children: [
          _Field(controller: _title, label: 'Judul Permintaan', max: 100),
          const SizedBox(height: 14),
          DropdownButtonFormField<String>(
            initialValue: _category,
            decoration: const InputDecoration(labelText: 'Kategori'),
            items: _categories
                .map((item) => DropdownMenuItem(value: item, child: Text(item)))
                .toList(),
            validator: (value) =>
                value == null ? 'Pilih kategori bantuan.' : null,
            onChanged: (value) => setState(() => _category = value),
          ),
          const SizedBox(height: 14),
          _Field(
            controller: _description,
            label: 'Detail Kebutuhan',
            max: 2000,
            lines: 4,
          ),
          const SizedBox(height: 14),
          _Field(controller: _location, label: 'Lokasi (Alamat)', max: 250),
          const SizedBox(height: 10),
          OutlinedButton.icon(
            onPressed: _chooseLocation,
            icon: const Icon(Icons.map_outlined),
            label: Text(
              _point == null
                  ? 'Pilih Titik Lokasi pada Peta'
                  : 'Koordinat: ${_point!.latitude.toStringAsFixed(4)}, ${_point!.longitude.toStringAsFixed(4)}',
            ),
          ),
          const SizedBox(height: 14),
          OutlinedButton.icon(
            onPressed: _chooseDateTime,
            icon: const Icon(Icons.calendar_month_outlined),
            label: Text(
              _dateTime == null
                  ? 'Pilih tanggal dan waktu'
                  : DateFormat(
                      'd MMM yyyy · HH.mm',
                      'id_ID',
                    ).format(_dateTime!),
            ),
          ),
          const SizedBox(height: 14),
          _Field(
            controller: _fee,
            label: 'Upah Jasa (Rp)',
            keyboard: TextInputType.number,
            validator: (value) {
              final amount = int.tryParse(value ?? '');
              if (amount == null || amount < 1000 || amount > 100000000) {
                return 'Upah harus Rp1.000–Rp100.000.000.';
              }
              return null;
            },
          ),
          const SizedBox(height: 18),
          FilledButton(
            onPressed: _busy ? null : _publish,
            child: _busy
                ? const CircularProgressIndicator()
                : const Text('Terbitkan Permintaan'),
          ),
        ],
      ),
    ),
  );

  Future<void> _chooseDateTime() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      firstDate: now,
      lastDate: DateTime(now.year + 3),
      initialDate: now.add(const Duration(days: 1)),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(now.add(const Duration(hours: 1))),
    );
    if (time == null || !mounted) return;
    setState(
      () => _dateTime = DateTime(
        date.year,
        date.month,
        date.day,
        time.hour,
        time.minute,
      ),
    );
  }

  Future<void> _chooseLocation() async {
    final point = await Navigator.push<LatLng>(
      context,
      MaterialPageRoute(builder: (_) => const LocationPickerPage()),
    );
    if (point != null) setState(() => _point = point);
  }

  Future<void> _publish() async {
    if (!_form.currentState!.validate()) return;
    if (_dateTime == null || !_dateTime!.isAfter(DateTime.now())) {
      _message(context, 'Pilih tanggal dan waktu yang akan datang.');
      return;
    }
    if (_location.text.trim().isEmpty) {
      _message(context, 'Lengkapi lokasi atau pilih alamat.');
      return;
    }
    setState(() => _busy = true);
    try {
      final id = await TulunginData.createJob(
        requester: widget.profile,
        title: _title.text,
        category: _category!,
        description: _description.text,
        location: _location.text,
        scheduledAt: DateFormat(
          'd MMMM yyyy · HH.mm',
          'id_ID',
        ).format(_dateTime!),
        fee: int.parse(_fee.text),
        latitude: _point?.latitude,
        longitude: _point?.longitude,
      );
      if (!mounted) return;
      _message(context, 'Permintaan berhasil diterbitkan · $id');
      Navigator.pop(context);
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }
}

class JobDetailPage extends StatelessWidget {
  const JobDetailPage({
    super.key,
    required this.profile,
    required this.job,
    required this.jobs,
    required this.applications,
  });
  final UserProfile profile;
  final UserJob job;
  final List<UserJob> jobs;
  final List<JobApplication> applications;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Detail Job')),
    body: StreamBuilder<List<UserJob>>(
      stream: TulunginData.jobs(),
      builder: (context, jobsSnapshot) {
        final current =
            jobsSnapshot.data?.where((item) => item.id == job.id).firstOrNull ??
            job;
        return StreamBuilder<List<JobApplication>>(
          stream: TulunginData.applications(),
          builder: (context, applicationsSnapshot) {
            final currentApplications =
                applicationsSnapshot.data ?? applications;
            final myApplication = currentApplications
                .where(
                  (item) =>
                      item.jobId == job.id && item.applicantId == profile.id,
                )
                .firstOrNull;
            final jobApplications = currentApplications
                .where((item) => item.jobId == job.id)
                .toList();
            final isRequester = current.requesterId == profile.id;
            return ListView(
              padding: const EdgeInsets.all(22),
              children: [
                Text(
                  current.title,
                  style: const TextStyle(
                    fontSize: 21,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 10),
                _StatusPill(status: current.status),
                const SizedBox(height: 14),
                _InfoCard(label: 'Kategori', value: current.category),
                _InfoCard(
                  label: 'Detail Kebutuhan',
                  value: current.description,
                ),
                _InfoCard(label: 'Lokasi', value: current.location),
                if (current.locationLat != null && current.locationLng != null)
                  SizedBox(
                    height: 210,
                    child: JobMap(
                      point: LatLng(current.locationLat!, current.locationLng!),
                    ),
                  ),
                _InfoCard(label: 'Waktu', value: current.scheduledAt),
                _InfoCard(
                  label: 'Upah Jasa',
                  value: _rupiah.format(current.fee),
                ),
                if (isRequester) ...[
                  const SizedBox(height: 12),
                  Text(
                    'Daftar Pelamar (${jobApplications.length})',
                    style: const TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  if (jobApplications.isEmpty)
                    const _EmptyState(
                      title: 'Belum ada pelamar',
                      message: 'Penulung yang melamar akan muncul di sini.',
                    )
                  else
                    ...jobApplications.map(
                      (app) => Card(
                        child: ListTile(
                          title: Text(app.applicantName),
                          subtitle: Text('Status: ${app.status}'),
                          trailing:
                              current.status == JobStatus.available &&
                                  app.status == 'pending'
                              ? FilledButton(
                                  onPressed: () => _perform(
                                    context,
                                    () => TulunginData.selectApplicant(
                                      current,
                                      app,
                                      profile,
                                    ),
                                    success: 'Penulung berhasil dipilih.',
                                  ),
                                  child: const Text('Terima'),
                                )
                              : null,
                        ),
                      ),
                    ),
                ],
                const SizedBox(height: 12),
                if (isRequester && current.helperId != null) ...[
                  OutlinedButton.icon(
                    onPressed: () => _openChat(context, current, profile),
                    icon: const Icon(Icons.chat_outlined),
                    label: const Text('Buka Chat'),
                  ),
                  FilledButton(
                    onPressed: () => _openCompletion(context, current, profile),
                    child: const Text('Bukti & Penyelesaian'),
                  ),
                ] else if (!isRequester &&
                    current.status == JobStatus.available &&
                    myApplication == null)
                  FilledButton(
                    onPressed: () => _perform(
                      context,
                      () => TulunginData.applyForJob(current, profile),
                      success: 'Lamaran berhasil dikirim.',
                    ),
                    child: const Text('Lamar Job'),
                  )
                else if (!isRequester && myApplication != null)
                  _InfoCard(
                    label: 'Status Lamaran',
                    value: 'Lamaran ${myApplication.status}',
                  ),
                if (myApplication?.status == 'accepted' ||
                    current.helperId == profile.id) ...[
                  FilledButton(
                    onPressed: current.status == JobStatus.accepted
                        ? () => _perform(
                            context,
                            () => TulunginData.startJob(current, profile),
                            success: 'Status diperbarui: sedang dikerjakan.',
                          )
                        : null,
                    child: const Text('Mulai pekerjaan'),
                  ),
                  OutlinedButton.icon(
                    onPressed: () => _openChat(context, current, profile),
                    icon: const Icon(Icons.chat_outlined),
                    label: const Text('Buka Chat'),
                  ),
                  FilledButton(
                    onPressed: () => _openCompletion(context, current, profile),
                    child: const Text('Bukti & Penyelesaian'),
                  ),
                ],
                if (current.status == JobStatus.completed) ...[
                  const SizedBox(height: 10),
                  if (current.requesterId == profile.id && current.rating == 0)
                    FilledButton(
                      onPressed: () => _openReview(context, current, profile),
                      child: const Text('Beri Ulasan'),
                    ),
                  if (current.requesterId == profile.id && !current.paid)
                    OutlinedButton(
                      onPressed: () => _openPayment(context, current, profile),
                      child: const Text('Pembayaran'),
                    ),
                  if (current.rating > 0)
                    _InfoCard(
                      label: 'Ulasan',
                      value:
                          '${'★' * current.rating}${'☆' * (5 - current.rating)} ${current.review}',
                    ),
                ],
              ],
            );
          },
        );
      },
    ),
  );
}

class JobCompletionPage extends StatefulWidget {
  const JobCompletionPage({
    super.key,
    required this.job,
    required this.profile,
  });
  final UserJob job;
  final UserProfile profile;

  @override
  State<JobCompletionPage> createState() => _JobCompletionPageState();
}

class _JobCompletionPageState extends State<JobCompletionPage> {
  bool _busy = false;
  String? _workProof;
  String? _paymentProof;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Konfirmasi Penyelesaian')),
    body: StreamBuilder<List<UserJob>>(
      stream: TulunginData.jobs(),
      builder: (context, snapshot) {
        final job =
            snapshot.data
                ?.where((item) => item.id == widget.job.id)
                .firstOrNull ??
            widget.job;
        final isHelper = job.helperId == widget.profile.id;
        final isRequester = job.requesterId == widget.profile.id;
        return ListView(
          padding: const EdgeInsets.all(22),
          children: [
            _StatusPill(status: job.status),
            const SizedBox(height: 16),
            _ProofSection(
              title: 'Bukti Pekerjaan (dari Penulung)',
              remoteUrl: job.proofUri,
              fileName: job.proofName,
              selectedFile: _workProof,
              canUpload: isHelper,
              busy: _busy,
              onChoose: () => _chooseAndUpload(job, payment: false),
            ),
            const SizedBox(height: 18),
            _ProofSection(
              title: 'Bukti Pembayaran (dari Peminta)',
              remoteUrl: job.paymentProofUri,
              fileName: job.paymentProofName,
              selectedFile: _paymentProof,
              canUpload: isRequester,
              busy: _busy,
              onChoose: () => _chooseAndUpload(job, payment: true),
            ),
            const SizedBox(height: 18),
            FilledButton(
              onPressed:
                  _busy ||
                      (job.proofUri ?? '').isEmpty ||
                      (job.paymentProofUri ?? '').isEmpty
                  ? null
                  : () => _perform(
                      context,
                      () => TulunginData.confirmCompletion(job, widget.profile),
                      success: 'Konfirmasi selesai berhasil disimpan.',
                    ),
              child: const Text('Konfirmasi Selesai'),
            ),
            if (job.status == JobStatus.completed)
              const Padding(
                padding: EdgeInsets.all(14),
                child: Text('Pekerjaan sudah selesai dan dikonfirmasi.'),
              ),
            if ((job.proofUri ?? '').isEmpty ||
                (job.paymentProofUri ?? '').isEmpty)
              const Padding(
                padding: EdgeInsets.only(top: 10),
                child: Text(
                  'Kedua pihak perlu mengirim bukti sebelum konfirmasi.',
                  style: TextStyle(color: _muted),
                ),
              ),
          ],
        );
      },
    ),
  );

  Future<void> _chooseAndUpload(UserJob job, {required bool payment}) async {
    try {
      final files = await FilePicker.pickFiles(type: FileType.image);
      if (files.isEmpty) return;
      final file = files.first;
      final bytes = await file.readAsBytes();
      setState(() {
        _busy = true;
        if (payment) {
          _paymentProof = file.name;
        } else {
          _workProof = file.name;
        }
      });
      final url = await TulunginData.uploadImage(bytes, file.name);
      await TulunginData.submitProof(
        job,
        widget.profile,
        url,
        file.name,
        payment: payment,
      );
      if (mounted) {
        _message(
          context,
          payment
              ? 'Bukti pembayaran berhasil diunggah.'
              : 'Bukti pekerjaan berhasil diunggah.',
        );
      }
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) {
        setState(() {
          _busy = false;
          if (payment) {
            _paymentProof = null;
          } else {
            _workProof = null;
          }
        });
      }
    }
  }
}

class _ProofSection extends StatelessWidget {
  const _ProofSection({
    required this.title,
    required this.remoteUrl,
    required this.fileName,
    required this.selectedFile,
    required this.canUpload,
    required this.busy,
    required this.onChoose,
  });
  final String title;
  final String? remoteUrl;
  final String? fileName;
  final String? selectedFile;
  final bool canUpload;
  final bool busy;
  final VoidCallback onChoose;

  @override
  Widget build(BuildContext context) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Text(
        title,
        style: const TextStyle(fontSize: 17, fontWeight: FontWeight.w700),
      ),
      const SizedBox(height: 10),
      if ((remoteUrl ?? '').isNotEmpty)
        ClipRRect(
          borderRadius: BorderRadius.circular(14),
          child: Image.network(
            remoteUrl!,
            height: 160,
            width: double.infinity,
            fit: BoxFit.cover,
            errorBuilder: (context, error, stackTrace) => _InfoCard(
              label: 'Bukti tersimpan',
              value: fileName ?? remoteUrl!,
            ),
          ),
        )
      else
        _InfoCard(
          label: 'Status Bukti',
          value:
              selectedFile ??
              (canUpload ? 'Pilih foto bukti.' : 'Belum ada bukti.'),
        ),
      if (canUpload && (remoteUrl ?? '').isEmpty)
        OutlinedButton.icon(
          onPressed: busy ? null : onChoose,
          icon: const Icon(Icons.upload_file),
          label: Text(busy ? 'Mengunggah...' : 'Pilih dan Kirim Foto'),
        ),
    ],
  );
}

class EditProfilePage extends StatefulWidget {
  const EditProfilePage({super.key, required this.profile});
  final UserProfile profile;

  @override
  State<EditProfilePage> createState() => _EditProfilePageState();
}

class _EditProfilePageState extends State<EditProfilePage> {
  late final _name = TextEditingController(text: widget.profile.name);
  late final _email = TextEditingController(text: widget.profile.email);
  late final _phone = TextEditingController(text: widget.profile.phone);
  late final _address = TextEditingController(text: widget.profile.address);
  String? _photoUrl;
  bool _busy = false;

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _phone.dispose();
    _address.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Edit Profil')),
    body: ListView(
      padding: const EdgeInsets.all(22),
      children: [
        Center(
          child: Column(
            children: [
              _Avatar(url: _photoUrl ?? widget.profile.photoUrl, size: 96),
              TextButton.icon(
                onPressed: _busy ? null : _pickPhoto,
                icon: const Icon(Icons.photo_camera_outlined),
                label: const Text('Ganti Foto Profil'),
              ),
            ],
          ),
        ),
        _Field(controller: _name, label: 'Nama Lengkap'),
        const SizedBox(height: 14),
        _Field(
          controller: _email,
          label: 'Email',
          keyboard: TextInputType.emailAddress,
        ),
        const SizedBox(height: 14),
        _Field(
          controller: _phone,
          label: 'Nomor HP',
          keyboard: TextInputType.phone,
        ),
        const SizedBox(height: 14),
        _Field(controller: _address, label: 'Alamat Lengkap', lines: 3),
        const SizedBox(height: 18),
        FilledButton(
          onPressed: _busy ? null : _save,
          child: _busy
              ? const CircularProgressIndicator()
              : const Text('Simpan Perubahan'),
        ),
      ],
    ),
  );

  Future<void> _pickPhoto() async {
    try {
      final files = await FilePicker.pickFiles(type: FileType.image);
      if (files.isEmpty) return;
      final file = files.first;
      final bytes = await file.readAsBytes();
      setState(() => _busy = true);
      final url = await TulunginData.uploadImage(bytes, file.name);
      setState(() => _photoUrl = url);
      if (mounted) _message(context, 'Foto profil berhasil diunggah.');
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _save() async {
    setState(() => _busy = true);
    try {
      final profile = UserProfile(
        id: widget.profile.id,
        name: _name.text,
        email: _email.text,
        phone: _phone.text,
        address: _address.text,
        photoUrl: _photoUrl ?? widget.profile.photoUrl,
        role: widget.profile.role,
        verified: widget.profile.verified,
        active: widget.profile.active,
      );
      if (profile.name.trim().length < 2) {
        throw Exception('Nama minimal 2 karakter.');
      }
      if (!RegExp(
        r'^[^@\s]+@[^@\s]+\.[^@\s]+$',
      ).hasMatch(profile.email.trim())) {
        throw Exception('Format email belum benar.');
      }
      if (!RegExp(r'^\+?[0-9]{10,15}$').hasMatch(profile.phone.trim())) {
        throw Exception('Nomor HP harus 10–15 digit.');
      }
      if (profile.address.trim().length < 5) {
        throw Exception('Lengkapi alamatmu.');
      }
      await TulunginAuth.saveProfile(profile);
      if (mounted) Navigator.pop(context);
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }
}

class SupportPage extends StatefulWidget {
  const SupportPage({super.key, required this.profile});
  final UserProfile profile;

  @override
  State<SupportPage> createState() => _SupportPageState();
}

class _SupportPageState extends State<SupportPage> {
  final _messageController = TextEditingController();
  bool _busy = false;

  @override
  void dispose() {
    _messageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Bantuan')),
    body: ListView(
      padding: const EdgeInsets.all(22),
      children: [
        const Text(
          'Customer Service',
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 8),
        const Text('Ada kendala? Ceritakan kepada tim Tulungin.'),
        const SizedBox(height: 16),
        TextField(
          controller: _messageController,
          maxLines: 5,
          maxLength: 2000,
          decoration: const InputDecoration(labelText: 'Pesan Bantuan'),
        ),
        const SizedBox(height: 14),
        FilledButton(
          onPressed: _busy ? null : _send,
          child: const Text('Kirim Pesan'),
        ),
        const SizedBox(height: 18),
        StreamBuilder<QuerySnapshot<Map<String, dynamic>>>(
          stream: TulunginData.db
              .collection('support_tickets')
              .where('userId', isEqualTo: widget.profile.id)
              .snapshots(),
          builder: (context, snapshot) {
            if (snapshot.hasError) {
              return Text('Tidak dapat memuat tiket: ${snapshot.error}');
            }
            final tickets = snapshot.data?.docs ?? [];
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (tickets.isNotEmpty)
                  const Text(
                    'Riwayat bantuan',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                ...tickets.reversed.map(
                  (ticket) => _InfoCard(
                    label: ticket.id,
                    value: ticket.data()['message'] as String? ?? '',
                  ),
                ),
              ],
            );
          },
        ),
      ],
    ),
  );

  Future<void> _send() async {
    setState(() => _busy = true);
    try {
      final id = await TulunginData.sendSupport(
        widget.profile.id,
        _messageController.text,
      );
      _messageController.clear();
      if (mounted) _message(context, 'Tiket $id tersimpan.');
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }
}

class ChatListPage extends StatelessWidget {
  const ChatListPage({super.key, required this.profile});
  final UserProfile profile;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Chat')),
    body: ListView(
      padding: const EdgeInsets.all(18),
      children: [
        Card(
          color: _mint,
          child: ListTile(
            leading: const Icon(Icons.support_agent),
            title: const Text('Pusat Bantuan / Admin'),
            subtitle: const Text('Hubungi admin jika ada kendala'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => ChatPage(
                  profile: profile,
                  conversationId: TulunginData.adminConversationId,
                  collection: 'conversations',
                  title: 'Chat dengan Admin',
                ),
              ),
            ),
          ),
        ),
        const SizedBox(height: 12),
        const Padding(
          padding: EdgeInsets.all(8),
          child: Text(
            'Chat Pekerjaan',
            style: TextStyle(fontWeight: FontWeight.w700),
          ),
        ),
        StreamBuilder<List<UserJob>>(
          stream: TulunginData.jobs(),
          builder: (context, snapshot) {
            final jobs =
                snapshot.data
                    ?.where(
                      (job) =>
                          job.requesterId == profile.id ||
                          job.helperId == profile.id,
                    )
                    .toList() ??
                [];
            if (jobs.isEmpty) {
              return const _EmptyState(
                title: 'Belum ada percakapan',
                message: 'Buka chat dari pekerjaan yang sedang berjalan.',
              );
            }
            return Column(
              children: jobs
                  .map(
                    (job) => Card(
                      child: ListTile(
                        leading: const Icon(Icons.chat_bubble_outline),
                        title: Text(job.title),
                        subtitle: Text(job.helperName ?? job.requesterName),
                        onTap: () => _openChat(context, job, profile),
                      ),
                    ),
                  )
                  .toList(),
            );
          },
        ),
      ],
    ),
  );
}

class ChatPage extends StatefulWidget {
  const ChatPage({
    super.key,
    required this.profile,
    required this.conversationId,
    required this.collection,
    required this.title,
    this.job,
    this.admin = false,
    this.conversation,
  });
  final UserProfile profile;
  final String conversationId;
  final String collection;
  final String title;
  final UserJob? job;
  final bool admin;
  final Map<String, dynamic>? conversation;

  @override
  State<ChatPage> createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {
  final _controller = TextEditingController();
  bool _busy = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: Text(widget.title)),
    body: Column(
      children: [
        Expanded(
          child: StreamBuilder<List<ChatMessage>>(
            stream: TulunginData.messages(
              widget.collection,
              widget.conversationId,
            ),
            builder: (context, snapshot) {
              if (snapshot.hasError) {
                return Center(
                  child: Text('Tidak dapat memuat chat: ${snapshot.error}'),
                );
              }
              final messages = snapshot.data ?? [];
              if (messages.isEmpty) {
                return const _EmptyState(
                  title: 'Belum ada pesan',
                  message: 'Kirim pesan untuk memulai percakapan.',
                );
              }
              return ListView.builder(
                padding: const EdgeInsets.all(18),
                itemCount: messages.length,
                itemBuilder: (context, index) {
                  final message = messages[index];
                  final outgoing = message.senderId == widget.profile.id;
                  return Align(
                    alignment: outgoing
                        ? Alignment.centerRight
                        : Alignment.centerLeft,
                    child: Container(
                      constraints: const BoxConstraints(maxWidth: 310),
                      margin: const EdgeInsets.only(bottom: 10),
                      padding: const EdgeInsets.all(13),
                      decoration: BoxDecoration(
                        color: outgoing ? _mint : const Color(0xFFF0F2F2),
                        borderRadius: BorderRadius.circular(15),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (widget.admin)
                            Text(
                              message.senderName,
                              style: const TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: _ink,
                              ),
                            ),
                          Text(message.text),
                        ],
                      ),
                    ),
                  );
                },
              );
            },
          ),
        ),
        SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(14, 8, 14, 12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _controller,
                    maxLength: 2000,
                    decoration: const InputDecoration(
                      labelText: 'Pesan',
                      counterText: '',
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  onPressed: _busy ? null : _send,
                  icon: const Icon(Icons.send),
                ),
              ],
            ),
          ),
        ),
      ],
    ),
  );

  Future<void> _send() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    setState(() => _busy = true);
    try {
      if (widget.job != null) {
        await TulunginData.sendJobMessage(widget.job!, widget.profile, text);
      } else {
        if (widget.admin) {
          TulunginData.openAdminConversation(
            widget.conversation ?? {'id': widget.conversationId},
          );
        }
        await TulunginData.sendAdminMessage(
          widget.profile,
          text,
          admin: widget.admin,
        );
      }
      _controller.clear();
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }
}

class JobMap extends StatelessWidget {
  const JobMap({super.key, required this.point, this.onTap});
  final LatLng point;
  final ValueChanged<LatLng>? onTap;

  @override
  Widget build(BuildContext context) => ClipRRect(
    borderRadius: BorderRadius.circular(16),
    child: FlutterMap(
      options: MapOptions(
        initialCenter: point,
        initialZoom: 14,
        onTap: onTap == null ? null : (_, location) => onTap!(location),
      ),
      children: [
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'com.pemmob.tulungin',
        ),
        MarkerLayer(
          markers: [
            Marker(
              point: point,
              width: 42,
              height: 52,
              child: const Icon(Icons.location_on, size: 42, color: _ink),
            ),
          ],
        ),
        const RichAttributionWidget(
          attributions: [TextSourceAttribution('OpenStreetMap contributors')],
        ),
      ],
    ),
  );
}

class LocationPickerPage extends StatefulWidget {
  const LocationPickerPage({super.key});
  @override
  State<LocationPickerPage> createState() => _LocationPickerPageState();
}

class _LocationPickerPageState extends State<LocationPickerPage> {
  LatLng? _point = const LatLng(-7.4214, 109.2312);
  bool _locating = false;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Pilih Lokasi')),
    body: Stack(
      children: [
        Positioned.fill(
          child: JobMap(
            point: _point!,
            onTap: (point) => setState(() => _point = point),
          ),
        ),
        Positioned(
          top: 16,
          right: 16,
          child: FloatingActionButton.small(
            heroTag: 'current-location',
            onPressed: _locating ? null : _useCurrentLocation,
            child: _locating
                ? const SizedBox.square(
                    dimension: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.my_location),
          ),
        ),
        Positioned(
          left: 18,
          right: 18,
          bottom: 20,
          child: FilledButton(
            onPressed: () => Navigator.pop(context, _point),
            child: const Text('Gunakan Lokasi Ini'),
          ),
        ),
      ],
    ),
  );

  Future<void> _useCurrentLocation() async {
    setState(() => _locating = true);
    try {
      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        throw Exception('Izin lokasi belum diberikan.');
      }
      final position = await Geolocator.getCurrentPosition();
      if (mounted) {
        setState(() => _point = LatLng(position.latitude, position.longitude));
      }
    } catch (error) {
      if (mounted) _message(context, _friendly(error));
    } finally {
      if (mounted) setState(() => _locating = false);
    }
  }
}

Future<void> _openChat(
  BuildContext context,
  UserJob job,
  UserProfile profile,
) async {
  await Navigator.push<void>(
    context,
    MaterialPageRoute(
      builder: (_) => ChatPage(
        profile: profile,
        conversationId: job.id,
        collection: 'jobs',
        title: job.title,
        job: job,
      ),
    ),
  );
}

Future<void> _openCompletion(
  BuildContext context,
  UserJob job,
  UserProfile profile,
) async {
  await Navigator.push<void>(
    context,
    MaterialPageRoute(
      builder: (_) => JobCompletionPage(job: job, profile: profile),
    ),
  );
}

Future<void> _openReview(
  BuildContext context,
  UserJob job,
  UserProfile profile,
) async {
  var rating = 0;
  final text = TextEditingController();
  final result = await showDialog<bool>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        title: const Text('Beri Ulasan'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Wrap(
              children: List.generate(
                5,
                (index) => IconButton(
                  onPressed: () => setState(() => rating = index + 1),
                  icon: Icon(index < rating ? Icons.star : Icons.star_border),
                ),
              ),
            ),
            TextField(
              controller: text,
              maxLines: 3,
              decoration: const InputDecoration(labelText: 'Ulasan'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Batal'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Kirim'),
          ),
        ],
      ),
    ),
  );
  if (result == true && context.mounted) {
    await _perform(
      context,
      () => TulunginData.submitReview(job, profile, rating, text.text),
      success: 'Ulasan berhasil disimpan.',
    );
  }
  text.dispose();
}

Future<void> _openPayment(
  BuildContext context,
  UserJob job,
  UserProfile profile,
) async {
  var method = TulunginData.paymentMethods.first;
  final confirmed = await showDialog<bool>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        title: const Text('Pembayaran'),
        content: DropdownButtonFormField<String>(
          initialValue: method,
          decoration: const InputDecoration(labelText: 'Metode Pembayaran'),
          items: TulunginData.paymentMethods
              .map((item) => DropdownMenuItem(value: item, child: Text(item)))
              .toList(),
          onChanged: (value) => setState(() => method = value ?? method),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Batal'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Bayar'),
          ),
        ],
      ),
    ),
  );
  if (confirmed == true && context.mounted) {
    await _perform(
      context,
      () => TulunginData.pay(job, profile, method),
      success: 'Pembayaran simulasi berhasil.',
    );
  }
}

class _JobCard extends StatelessWidget {
  const _JobCard({required this.job, required this.onTap});
  final UserJob job;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.only(top: 10),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
    child: InkWell(
      borderRadius: BorderRadius.circular(16),
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              job.title,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 4),
            Text(
              job.category,
              style: const TextStyle(color: _muted, fontSize: 13),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                const Icon(Icons.location_on_outlined, size: 17, color: _muted),
                Expanded(
                  child: Text(
                    job.location,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: _muted),
                  ),
                ),
                Text(
                  _rupiah.format(job.fee),
                  style: const TextStyle(
                    color: _ink,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            _StatusPill(status: job.status),
          ],
        ),
      ),
    ),
  );
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.onSeeAll});
  final String title;
  final VoidCallback? onSeeAll;

  @override
  Widget build(BuildContext context) => Row(
    children: [
      Expanded(
        child: Text(
          title,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
        ),
      ),
      if (onSeeAll != null)
        TextButton(onPressed: onSeeAll, child: const Text('Lihat Semua')),
    ],
  );
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({required this.status});
  final JobStatus status;

  @override
  Widget build(BuildContext context) => Align(
    alignment: Alignment.centerLeft,
    child: DecoratedBox(
      decoration: BoxDecoration(
        color: status == JobStatus.completed
            ? const Color(0xFFD5F5E3)
            : status == JobStatus.cancelled
            ? const Color(0xFFFFDAD6)
            : _mint,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 11, vertical: 6),
        child: Text(
          status.label,
          style: const TextStyle(
            color: _ink,
            fontSize: 12,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    ),
  );
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.only(top: 9),
    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
    child: Padding(
      padding: const EdgeInsets.symmetric(horizontal: 15, vertical: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(color: _muted, fontSize: 12)),
          const SizedBox(height: 4),
          Text(value, style: const TextStyle(fontWeight: FontWeight.w600)),
        ],
      ),
    ),
  );
}

class _Field extends StatelessWidget {
  const _Field({
    required this.controller,
    required this.label,
    this.max,
    this.lines = 1,
    this.keyboard,
    this.validator,
  });
  final TextEditingController controller;
  final String label;
  final int? max;
  final int lines;
  final TextInputType? keyboard;
  final String? Function(String?)? validator;

  @override
  Widget build(BuildContext context) => TextFormField(
    controller: controller,
    maxLength: max,
    maxLines: lines,
    keyboardType: keyboard,
    decoration: InputDecoration(labelText: label, counterText: ''),
    validator:
        validator ??
        (value) => (value ?? '').trim().isEmpty ? '$label wajib diisi.' : null,
  );
}

class _Avatar extends StatelessWidget {
  const _Avatar({required this.url, this.size = 46});
  final String url;
  final double size;

  @override
  Widget build(BuildContext context) => CircleAvatar(
    radius: size / 2,
    backgroundColor: _mint,
    backgroundImage: url.startsWith('http') ? NetworkImage(url) : null,
    child: url.startsWith('http')
        ? null
        : const Icon(Icons.person_outline, color: _ink),
  );
}

class _ActionTile extends StatelessWidget {
  const _ActionTile({
    required this.icon,
    required this.title,
    required this.onTap,
  });
  final IconData icon;
  final String title;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: Icon(icon, color: _ink),
      title: Text(title),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    ),
  );
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.title, required this.message});
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.only(top: 12),
    padding: const EdgeInsets.all(18),
    decoration: BoxDecoration(
      color: _mint,
      borderRadius: BorderRadius.circular(15),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
        const SizedBox(height: 4),
        Text(message),
      ],
    ),
  );
}

class _LoadingScaffold extends StatelessWidget {
  const _LoadingScaffold();
  @override
  Widget build(BuildContext context) =>
      const Scaffold(body: Center(child: CircularProgressIndicator()));
}

class _ErrorScaffold extends StatelessWidget {
  const _ErrorScaffold({required this.error});
  final String error;
  @override
  Widget build(BuildContext context) => Scaffold(
    body: Center(
      child: Padding(
        padding: const EdgeInsets.all(22),
        child: Text('Gagal memuat data: $error', textAlign: TextAlign.center),
      ),
    ),
  );
}

Future<void> _perform(
  BuildContext context,
  Future<void> Function() action, {
  String? success,
}) async {
  try {
    await action();
    if (success != null && context.mounted) _message(context, success);
  } catch (error) {
    if (context.mounted) _message(context, _friendly(error));
  }
}

String _friendly(Object error) {
  final message = error.toString().replaceFirst('Exception: ', '');
  if (message.contains('permission-denied')) {
    return 'Akses ditolak oleh aturan Firebase. Hubungi admin.';
  }
  if (message.contains('network-request-failed')) {
    return 'Periksa koneksi internet, lalu coba lagi.';
  }
  return message;
}

void _message(BuildContext context, String message) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text(message)));
}

extension _IterableFirstOrNull<T> on Iterable<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
