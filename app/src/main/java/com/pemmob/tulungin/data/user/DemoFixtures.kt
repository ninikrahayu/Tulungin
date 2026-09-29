package com.pemmob.tulungin.data.user

object DemoFixtures {
    fun initial(): UserSnapshot {
        val first = UserJob("available-1", "Bantu Pindahan Kos", "Jasa Rumah", "Membantu memindahkan barang dari kamar kos ke tempat baru.", "Jl. Kampus No. 12, Purwokerto", "28 September 2026 · 09.00", 50000, 1.2, "requester-andi", "Andi Pratama")
        val second = UserJob("available-2", "Antar Dokumen", "Pengantaran", "Mengantarkan dokumen ke alamat tujuan dengan aman.", "Jl. Soeparno No. 8, Purwokerto", "30 September 2026 · 10.00", 30000, 2.4, "requester-siti", "Siti Rahma")
        val third = UserJob("available-3", "Bersihkan Halaman", "Kebersihan", "Membersihkan halaman dan mengumpulkan daun kering.", "Jl. Gunung Slamet No. 5, Purwokerto", "1 Oktober 2026 · 08.00", 45000, 3.1, "requester-budi", "Budi Santoso")
        return UserSnapshot(
            jobs = listOf(
                first, second, third,
                first.copy(id = "history-1", requesterId = "demo-user", helperId = "helper-rina", helperName = "Rina", status = JobStatus.COMPLETED, proofUri = "demo://bukti", proofName = "Bukti foto pekerjaan", rating = 4, review = "Pelayanan baik dan tepat waktu.", paid = true, paymentMethod = "Tunai (simulasi)"),
                second.copy(id = "history-2", scheduledAt = "21 September 2026 · 10.00", helperId = "demo-user", helperName = "Andi Pratama", status = JobStatus.COMPLETED, paid = true),
                third.copy(id = "history-3", scheduledAt = "15 September 2026 · 08.00", requesterId = "demo-user", status = JobStatus.CANCELLED),
                first.copy(id = "request-1", requesterId = "demo-user", helperId = "helper-rina", helperName = "Rina", status = JobStatus.AWAITING_CONFIRMATION, proofUri = "demo://bukti", proofName = "Bukti foto pekerjaan")
            ),
            conversations = listOf(
                Conversation(first.id, "Andi Pratama", listOf(ChatMessage("m1", "Halo, kapan kamu bisa datang?", false), ChatMessage("m2", "Saya bisa datang pukul 09.00.", true), ChatMessage("m3", "Baik, saya tunggu ya.", false))),
                Conversation(second.id, "Siti Rahma", listOf(ChatMessage("m4", "Terima kasih sudah membantu.", false)))
            )
        )
    }
}
