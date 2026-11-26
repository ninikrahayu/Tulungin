import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

import 'data.dart';
import 'models.dart';
import 'screens.dart' show ChatPage;

const _adminInk = Color(0xFF0F282F);
const _adminMint = Color(0xFFD7F3F3);

class AdminDashboardPage extends StatelessWidget {
  const AdminDashboardPage({super.key, required this.profile});
  final UserProfile profile;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: const Text(
        'Dashboard Admin',
        style: TextStyle(fontWeight: FontWeight.w700),
      ),
      actions: [
        IconButton(
          tooltip: 'Keluar',
          onPressed: () async {
            final confirmed = await showDialog<bool>(
              context: context,
              builder: (context) => AlertDialog(
                title: const Text('Keluar dari akun?'),
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
          },
          icon: const Icon(Icons.logout),
        ),
      ],
    ),
    body: StreamBuilder<List<UserProfile>>(
      stream: TulunginData.users(),
      builder: (context, usersSnapshot) => StreamBuilder<List<UserJob>>(
        stream: TulunginData.jobs(),
        builder: (context, jobsSnapshot) =>
            StreamBuilder<List<Map<String, dynamic>>>(
              stream: TulunginData.categories(),
              builder: (context, categoriesSnapshot) {
                final users = usersSnapshot.data ?? [];
                final jobs = jobsSnapshot.data ?? [];
                final categories = categoriesSnapshot.data ?? [];
                final activeCount = jobs
                    .where(
                      (job) =>
                          job.status == JobStatus.available ||
                          job.status == JobStatus.accepted ||
                          job.status == JobStatus.inProgress ||
                          job.status == JobStatus.awaitingConfirmation,
                    )
                    .length;
                return ListView(
                  padding: const EdgeInsets.all(20),
                  children: [
                    Text(
                      'Halo, ${profile.name}',
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 18),
                    Wrap(
                      spacing: 12,
                      runSpacing: 12,
                      children: [
                        _StatCard(label: 'Total User', value: users.length),
                        _StatCard(label: 'Total Job', value: jobs.length),
                        _StatCard(
                          label: 'Total Kategori',
                          value: categories.length,
                        ),
                        _StatCard(label: 'Job Aktif', value: activeCount),
                      ],
                    ),
                    const SizedBox(height: 24),
                    const Text(
                      'Menu Utama',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 10),
                    _AdminMenuTile(
                      icon: Icons.people_outline,
                      title: 'Kelola User/Akun',
                      subtitle: '${users.length} pengguna',
                      onTap: () =>
                          _push(context, AdminUsersPage(admin: profile)),
                    ),
                    _AdminMenuTile(
                      icon: Icons.category_outlined,
                      title: 'Kelola Kategori',
                      subtitle: '${categories.length} kategori',
                      onTap: () => _push(context, const AdminCategoriesPage()),
                    ),
                    _AdminMenuTile(
                      icon: Icons.work_outline,
                      title: 'Kelola Job',
                      subtitle: '${jobs.length} job',
                      onTap: () => _push(context, const AdminJobsPage()),
                    ),
                    _AdminMenuTile(
                      icon: Icons.chat_outlined,
                      title: 'Chat',
                      subtitle: 'Percakapan dengan pengguna',
                      onTap: () =>
                          _push(context, AdminChatInboxPage(admin: profile)),
                    ),
                  ],
                );
              },
            ),
      ),
    ),
  );
}

class AdminUsersPage extends StatefulWidget {
  const AdminUsersPage({super.key, required this.admin});
  final UserProfile admin;

  @override
  State<AdminUsersPage> createState() => _AdminUsersPageState();
}

class _AdminUsersPageState extends State<AdminUsersPage> {
  final _query = TextEditingController();

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Kelola User/Akun')),
    body: Column(
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(18, 12, 18, 8),
          child: TextField(
            controller: _query,
            decoration: const InputDecoration(
              hintText: 'Cari pengguna...',
              prefixIcon: Icon(Icons.search),
            ),
            onChanged: (_) => setState(() {}),
          ),
        ),
        Expanded(
          child: StreamBuilder<List<UserProfile>>(
            stream: TulunginData.users(),
            builder: (context, snapshot) {
              if (snapshot.hasError) {
                return _AdminError(error: snapshot.error.toString());
              }
              if (!snapshot.hasData) return const _AdminLoading();
              final query = _query.text.toLowerCase();
              final users = snapshot.data!
                  .where(
                    (user) =>
                        user.name.toLowerCase().contains(query) ||
                        user.email.toLowerCase().contains(query) ||
                        user.phone.contains(query),
                  )
                  .toList();
              if (users.isEmpty) return const _AdminEmpty();
              return ListView.builder(
                padding: const EdgeInsets.all(14),
                itemCount: users.length,
                itemBuilder: (context, index) {
                  final user = users[index];
                  return Card(
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: _adminMint,
                        child: Text(
                          user.name.isEmpty ? '?' : user.name[0].toUpperCase(),
                        ),
                      ),
                      title: Text(user.name.isEmpty ? 'Tanpa Nama' : user.name),
                      subtitle: Text(
                        '${user.email}\n${user.verified ? 'Terverifikasi' : 'Belum diverifikasi'} · ${user.active ? 'Aktif' : 'Nonaktif'}',
                      ),
                      isThreeLine: true,
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => _showUser(context, user),
                    ),
                  );
                },
              );
            },
          ),
        ),
      ],
    ),
  );

  Future<void> _showUser(BuildContext context, UserProfile user) async {
    await showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (context) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(22),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                user.name,
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                ),
              ),
              Text(user.email),
              Text(user.phone),
              Text(user.address),
              const SizedBox(height: 14),
              FilledButton.tonal(
                onPressed: user.id == widget.admin.id
                    ? null
                    : () async {
                        await _adminAction(
                          context,
                          () => TulunginData.setUserActive(user, !user.active),
                        );
                        if (context.mounted) Navigator.pop(context);
                      },
                child: Text(user.active ? 'Nonaktifkan Akun' : 'Aktifkan Akun'),
              ),
              TextButton(
                onPressed: user.id == widget.admin.id
                    ? null
                    : () async {
                        final confirm = await showDialog<bool>(
                          context: context,
                          builder: (context) => AlertDialog(
                            title: const Text('Hapus profil pengguna?'),
                            content: const Text(
                              'Aksi ini hanya menghapus dokumen profil Firestore.',
                            ),
                            actions: [
                              TextButton(
                                onPressed: () => Navigator.pop(context, false),
                                child: const Text('Batal'),
                              ),
                              FilledButton(
                                onPressed: () => Navigator.pop(context, true),
                                child: const Text('Hapus'),
                              ),
                            ],
                          ),
                        );
                        if (confirm == true && context.mounted) {
                          await _adminAction(
                            context,
                            () => TulunginData.deleteUserProfile(user),
                          );
                          if (context.mounted) Navigator.pop(context);
                        }
                      },
                child: const Text('Hapus Profil'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class AdminJobsPage extends StatefulWidget {
  const AdminJobsPage({super.key});
  @override
  State<AdminJobsPage> createState() => _AdminJobsPageState();
}

class _AdminJobsPageState extends State<AdminJobsPage> {
  final _query = TextEditingController();

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Kelola Job')),
    body: Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16),
          child: TextField(
            controller: _query,
            decoration: const InputDecoration(
              hintText: 'Cari job...',
              prefixIcon: Icon(Icons.search),
            ),
            onChanged: (_) => setState(() {}),
          ),
        ),
        Expanded(
          child: StreamBuilder<List<UserJob>>(
            stream: TulunginData.jobs(),
            builder: (context, snapshot) {
              if (snapshot.hasError) {
                return _AdminError(error: snapshot.error.toString());
              }
              if (!snapshot.hasData) return const _AdminLoading();
              final query = _query.text.toLowerCase();
              final jobs = snapshot.data!
                  .where(
                    (job) =>
                        '${job.title} ${job.category} ${job.requesterName} ${job.status.label}'
                            .toLowerCase()
                            .contains(query),
                  )
                  .toList();
              if (jobs.isEmpty) return const _AdminEmpty();
              return ListView(
                padding: const EdgeInsets.fromLTRB(14, 0, 14, 18),
                children: jobs
                    .map(
                      (job) => Card(
                        child: ListTile(
                          title: Text(job.title),
                          subtitle: Text(
                            '${job.category} · ${job.requesterName}\n${job.status.label} · ${_formatFee(job.fee)}',
                          ),
                          isThreeLine: true,
                          trailing: IconButton(
                            tooltip: 'Hapus Job',
                            onPressed: () => _deleteJob(context, job),
                            icon: const Icon(Icons.delete_outline),
                          ),
                        ),
                      ),
                    )
                    .toList(),
              );
            },
          ),
        ),
      ],
    ),
  );

  Future<void> _deleteJob(BuildContext context, UserJob job) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Hapus job?'),
        content: Text('Hapus "${job.title}" dari Firestore?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Batal'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Hapus'),
          ),
        ],
      ),
    );
    if (confirm == true && context.mounted) {
      await _adminAction(context, () => TulunginData.deleteJob(job.id));
    }
  }
}

class AdminCategoriesPage extends StatefulWidget {
  const AdminCategoriesPage({super.key});

  @override
  State<AdminCategoriesPage> createState() => _AdminCategoriesPageState();
}

class _AdminCategoriesPageState extends State<AdminCategoriesPage> {
  bool _seedRequested = false;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Kelola Kategori')),
    body: StreamBuilder<List<Map<String, dynamic>>>(
      stream: TulunginData.categories(),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return _AdminError(error: snapshot.error.toString());
        }
        if (!snapshot.hasData) return const _AdminLoading();
        final categories = snapshot.data!;
        if (categories.isEmpty) {
          if (!_seedRequested) {
            _seedRequested = true;
            WidgetsBinding.instance.addPostFrameCallback((_) async {
              try {
                await TulunginData.ensureDefaultCategories();
              } catch (error) {
                if (context.mounted) {
                  ScaffoldMessenger.of(
                    context,
                  ).showSnackBar(SnackBar(content: Text(error.toString())));
                }
              }
            });
          }
          return const _AdminLoading();
        }
        return ListView(
          padding: const EdgeInsets.all(16),
          children: [
            ...categories.map((category) {
              final id = category['id'] as String;
              final name = category['name'] as String? ?? 'Kategori';
              return Card(
                child: ListTile(
                  leading: const Icon(
                    Icons.category_outlined,
                    color: _adminInk,
                  ),
                  title: Text(name),
                  trailing: PopupMenuButton<String>(
                    onSelected: (action) {
                      if (action == 'edit') {
                        _editCategory(context, id, name);
                      } else if (action == 'delete') {
                        _deleteCategory(context, id, name);
                      }
                    },
                    itemBuilder: (_) => const [
                      PopupMenuItem(value: 'edit', child: Text('Edit')),
                      PopupMenuItem(value: 'delete', child: Text('Hapus')),
                    ],
                  ),
                ),
              );
            }),
            const SizedBox(height: 14),
            FilledButton.icon(
              onPressed: () => _editCategory(context, null, ''),
              icon: const Icon(Icons.add),
              label: const Text('Tambah Kategori'),
            ),
          ],
        );
      },
    ),
  );
}

class AdminChatInboxPage extends StatelessWidget {
  const AdminChatInboxPage({super.key, required this.admin});
  final UserProfile admin;

  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: const Text('Chat Pengguna')),
    body: StreamBuilder<QuerySnapshot<Map<String, dynamic>>>(
      stream: TulunginData.db
          .collection('conversations')
          .orderBy('updatedAt', descending: true)
          .snapshots(),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return _AdminError(error: snapshot.error.toString());
        }
        if (!snapshot.hasData) return const _AdminLoading();
        final conversations = snapshot.data!.docs;
        if (conversations.isEmpty) return const _AdminEmpty();
        return ListView(
          padding: const EdgeInsets.all(14),
          children: conversations.map((document) {
            final data = {'id': document.id, ...document.data()};
            return Card(
              child: ListTile(
                leading: const CircleAvatar(
                  backgroundColor: _adminMint,
                  child: Icon(Icons.person_outline, color: _adminInk),
                ),
                title: Text(data['userName'] as String? ?? 'Pengguna'),
                subtitle: Text(
                  data['lastMessage'] as String? ?? 'Belum ada pesan',
                ),
                trailing: const Icon(Icons.chevron_right),
                onTap: () {
                  TulunginData.openAdminConversation(data);
                  Navigator.push<void>(
                    context,
                    MaterialPageRoute(
                      builder: (_) => ChatPage(
                        profile: admin,
                        conversationId: document.id,
                        collection: 'conversations',
                        title: data['userName'] as String? ?? 'Chat',
                        admin: true,
                        conversation: data,
                      ),
                    ),
                  );
                },
              ),
            );
          }).toList(),
        );
      },
    ),
  );
}

class _StatCard extends StatelessWidget {
  const _StatCard({required this.label, required this.value});
  final String label;
  final int value;

  @override
  Widget build(BuildContext context) => SizedBox(
    width: (MediaQuery.sizeOf(context).width - 52) / 2,
    child: Card(
      color: _adminMint,
      child: Padding(
        padding: const EdgeInsets.all(15),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label, style: const TextStyle(color: Colors.black54)),
            const SizedBox(height: 8),
            Text(
              '$value',
              style: const TextStyle(
                fontSize: 23,
                color: _adminInk,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    ),
  );
}

class _AdminMenuTile extends StatelessWidget {
  const _AdminMenuTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
    child: ListTile(
      leading: CircleAvatar(
        backgroundColor: _adminInk,
        child: Icon(icon, color: Colors.white),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
      subtitle: Text(subtitle),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    ),
  );
}

class _AdminLoading extends StatelessWidget {
  const _AdminLoading();
  @override
  Widget build(BuildContext context) =>
      const Center(child: CircularProgressIndicator());
}

class _AdminEmpty extends StatelessWidget {
  const _AdminEmpty();
  @override
  Widget build(BuildContext context) =>
      const Center(child: Text('Belum ada data.'));
}

class _AdminError extends StatelessWidget {
  const _AdminError({required this.error});
  final String error;
  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(20),
      child: Text(
        'Tidak dapat memuat data: $error',
        textAlign: TextAlign.center,
      ),
    ),
  );
}

void _push(BuildContext context, Widget page) {
  Navigator.push<void>(context, MaterialPageRoute<void>(builder: (_) => page));
}

Future<void> _editCategory(
  BuildContext context,
  String? id,
  String initial,
) async {
  final controller = TextEditingController(text: initial);
  final save = await showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      title: Text(id == null ? 'Tambah Kategori' : 'Edit Kategori'),
      content: TextField(
        controller: controller,
        autofocus: true,
        decoration: const InputDecoration(labelText: 'Nama Kategori'),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: const Text('Batal'),
        ),
        FilledButton(
          onPressed: () => Navigator.pop(context, true),
          child: const Text('Simpan'),
        ),
      ],
    ),
  );
  if (save == true && context.mounted) {
    await _adminAction(
      context,
      () => TulunginData.saveCategory(id, controller.text),
    );
  }
  controller.dispose();
}

Future<void> _deleteCategory(
  BuildContext context,
  String id,
  String name,
) async {
  final confirm = await showDialog<bool>(
    context: context,
    builder: (context) => AlertDialog(
      title: const Text('Hapus kategori?'),
      content: Text('Hapus "$name"?'),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context, false),
          child: const Text('Batal'),
        ),
        FilledButton(
          onPressed: () => Navigator.pop(context, true),
          child: const Text('Hapus'),
        ),
      ],
    ),
  );
  if (confirm == true && context.mounted) {
    await _adminAction(context, () => TulunginData.deleteCategory(id));
  }
}

Future<void> _adminAction(
  BuildContext context,
  Future<void> Function() action,
) async {
  try {
    await action();
    if (context.mounted) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Perubahan disimpan.')));
    }
  } catch (error) {
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.toString().replaceFirst('Exception: ', '')),
        ),
      );
    }
  }
}

String _formatFee(int value) => 'Rp${value.toString()}';
