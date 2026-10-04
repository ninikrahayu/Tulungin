package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CategoryItem(
    val id: String,
    val name: String,
    val iconRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKategori(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var categoryList by remember { mutableStateOf<List<CategoryItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var editedName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var isAddingCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("categories").addSnapshotListener { snapshot, error ->
            loading = false
            if (error != null) {
                errorMessage = error.message
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed initial categories
                    val defaults = listOf("Kebersihan", "Pengantaran", "Perbaikan", "Jasa Rumah", "Lainnya")
                    defaults.forEach { catName ->
                        val docId = db.collection("categories").document().id
                        db.collection("categories").document(docId).set(
                            mapOf("id" to docId, "name" to catName, "createdAt" to FieldValue.serverTimestamp())
                        )
                    }
                } else {
                    categoryList = snapshot.documents.map { doc ->
                        CategoryItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "Kategori",
                            iconRes = R.drawable.ic_category
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Kembali",
                        tint = TulunginTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kelola Kategori",
                    color = TulunginTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Kategori Bantuan",
                color = TulunginTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Memuat data...", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak dapat memuat data: $errorMessage", color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
                categoryList.isEmpty() -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data.", color = TulunginTextSecondary, textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = categoryList,
                            key = { it.id }
                        ) { item ->
                            CategoryCardItem(
                                item = item,
                                onClick = {
                                    selectedCategory = item
                                    editedName = item.name
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isAddingCategory = true
                    newCategoryName = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TulunginPrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "+ Tambah Kategori",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (selectedCategory != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedCategory = null },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 14.dp, bottom = 8.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(TulunginInactiveStep, CircleShape)
                    )
                }
            ) {
                EditCategorySheetContent(
                    title = "Edit Kategori",
                    categoryName = editedName,
                    onNameChange = { editedName = it },
                    onSaveClick = {
                        val current = selectedCategory
                        if (current != null && editedName.isNotBlank()) {
                            coroutineScope.launch {
                                runCatching {
                                    FirebaseFirestore.getInstance().collection("categories").document(current.id)
                                        .update("name", editedName.trim()).await()
                                }
                                sheetState.hide()
                                selectedCategory = null
                            }
                        }
                    },
                    onDeleteClick = {
                        val current = selectedCategory
                        if (current != null) {
                            coroutineScope.launch {
                                runCatching {
                                    FirebaseFirestore.getInstance().collection("categories").document(current.id)
                                        .delete().await()
                                }
                                sheetState.hide()
                                selectedCategory = null
                            }
                        }
                    }
                )
            }
        }

        if (isAddingCategory) {
            ModalBottomSheet(
                onDismissRequest = { isAddingCategory = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 14.dp, bottom = 8.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(TulunginInactiveStep, CircleShape)
                    )
                }
            ) {
                EditCategorySheetContent(
                    title = "Tambah Kategori",
                    categoryName = newCategoryName,
                    onNameChange = { newCategoryName = it },
                    saveButtonText = "Simpan Kategori",
                    showDeleteButton = false,
                    onSaveClick = {
                        if (newCategoryName.isNotBlank()) {
                            coroutineScope.launch {
                                runCatching {
                                    val db = FirebaseFirestore.getInstance()
                                    val docId = db.collection("categories").document().id
                                    db.collection("categories").document(docId).set(
                                        mapOf("id" to docId, "name" to newCategoryName.trim(), "createdAt" to FieldValue.serverTimestamp())
                                    ).await()
                                }
                                sheetState.hide()
                                isAddingCategory = false
                            }
                        }
                    },
                    onDeleteClick = {}
                )
            }
        }
    }
}

@Composable
fun EditCategorySheetContent(
    title: String = "Edit Kategori",
    categoryName: String,
    onNameChange: (String) -> Unit,
    saveButtonText: String = "Simpan Perubahan",
    showDeleteButton: Boolean = true,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Nama Kategori",
                color = TulunginTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = categoryName,
                onValueChange = onNameChange,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TulunginInputBorderFocused,
                    unfocusedBorderColor = TulunginInputBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TulunginTextPrimary,
                    unfocusedTextColor = TulunginTextPrimary,
                    cursorColor = TulunginPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TulunginPrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = saveButtonText,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showDeleteButton) {
            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onDeleteClick,
                modifier = Modifier.heightIn(min = 44.dp)
            ) {
                Text(
                    text = "Hapus Kategori",
                    color = TulunginDeleteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CategoryCardItem(
    item: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.2.dp, TulunginMintBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(TulunginMintLight, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = TulunginPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = item.name,
            color = TulunginTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TulunginTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
