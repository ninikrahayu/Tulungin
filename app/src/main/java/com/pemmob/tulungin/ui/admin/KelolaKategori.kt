package com.pemmob.tulungin.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pemmob.tulungin.R
import com.pemmob.tulungin.ui.theme.TulunginDeleteText
import com.pemmob.tulungin.ui.theme.TulunginInactiveStep
import com.pemmob.tulungin.ui.theme.TulunginInputBorder
import com.pemmob.tulungin.ui.theme.TulunginInputBorderFocused
import com.pemmob.tulungin.ui.theme.TulunginMintBorder
import com.pemmob.tulungin.ui.theme.TulunginMintLight
import com.pemmob.tulungin.ui.theme.TulunginPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextMuted
import com.pemmob.tulungin.ui.theme.TulunginTextPrimary
import com.pemmob.tulungin.ui.theme.TulunginTextSecondary
import com.pemmob.tulungin.ui.theme.TulunginTheme
import kotlinx.coroutines.launch

data class CategoryItem(
    val id: Int,
    val name: String,
    val iconRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKategori(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var categoryList by remember {
        mutableStateOf(
            listOf(
                CategoryItem(id = 1, name = "Kebersihan", iconRes = R.drawable.ic_sparkles),
                CategoryItem(id = 2, name = "Pengantaran", iconRes = R.drawable.ic_truck),
                CategoryItem(id = 3, name = "Perbaikan", iconRes = R.drawable.ic_wrench),
                CategoryItem(id = 4, name = "Jasa Rumah", iconRes = R.drawable.ic_home)
            )
        )
    }

    // State for Edit Bottom Sheet
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var editedName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // State for Add Category
    var isAddingCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header
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
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle: Kategori Bantuan
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

            // Category List (LazyColumn)
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

            // Bottom Button: + Tambah Kategori
            Button(
                onClick = {
                    isAddingCategory = true
                    newCategoryName = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .height(52.dp),
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

        // Edit Kategori BottomSheet (Sesuai Desain Screenshot)
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
                            categoryList = categoryList.map {
                                if (it.id == current.id) it.copy(name = editedName) else it
                            }
                        }
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedCategory = null
                        }
                    },
                    onDeleteClick = {
                        val current = selectedCategory
                        if (current != null) {
                            categoryList = categoryList.filter { it.id != current.id }
                        }
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedCategory = null
                        }
                    }
                )
            }
        }

        // Tambah Kategori BottomSheet
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
                            val newId = (categoryList.maxOfOrNull { it.id } ?: 0) + 1
                            categoryList = categoryList + CategoryItem(
                                id = newId,
                                name = newCategoryName,
                                iconRes = R.drawable.ic_category
                            )
                        }
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            isAddingCategory = false
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TulunginTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Input Field: Nama Kategori
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

        Spacer(modifier = Modifier.height(24.dp))

        // "Simpan Perubahan" Button
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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
            Spacer(modifier = Modifier.height(14.dp))

            // "Hapus Kategori" Text Button
            TextButton(
                onClick = onDeleteClick
            ) {
                Text(
                    text = "Hapus Kategori",
                    color = TulunginDeleteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
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
        // Icon Container Box
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

        Spacer(modifier = Modifier.width(16.dp))

        // Category Name
        Text(
            text = item.name,
            color = TulunginTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Chevron Right
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TulunginTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KelolaKategoriPreview() {
    TulunginTheme {
        KelolaKategori()
    }
}
