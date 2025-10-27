package com.ljs.and.ui.pending

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ljs.and.ui.Screen
import com.ljs.and.ui.search.PendingItem
import com.ljs.and.ui.theme.AndTheme

// Dummy Data
val dummyPendingReceivingList = listOf(
    PendingItem("입고", "IN - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "https://i.namu.wiki/i/22z_qpCg1pxtx5-2a2p3rf_YnS9vyN03pt580e0Jc5n3rSg2g2TfNT9c2mfp3aLp4z-mAs2T9oPMwT3QYDYi6A.webp", "검수 중")
)

val dummyPendingReleasingList = listOf(
    PendingItem("출고", "OUT - ABCD", "현대 모비스", "엔진 오일", "A-03-2", 3, "이지수", "https://i.namu.wiki/i/22z_qpCg1pxtx5-2a2p3rf_YnS9vyN03pt580e0Jc5n3rSg2g2TfNT9c2mfp3aLp4z-mAs2T9oPMwT3QYDYi6A.webp", "피킹 중")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingItemsScreen(navController: NavController, initialTabIndex: Int = 0) {
    var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
    val tabs = listOf("입고", "출고")
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = { 
            PendingItemsTopAppBar(
                isSearchVisible = isSearchVisible, 
                searchQuery = searchQuery, 
                onSearchQueryChange = { searchQuery = it }, 
                onSearchVisibilityChange = { isSearchVisible = it },
                onPerformSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.SearchResult.createRoute("pending", searchQuery))
                    }
                    isSearchVisible = false
                    focusManager.clearFocus()
                }
            ) 
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            PendingItemsTabRow(
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it }
            )
            when (selectedTabIndex) {
                0 -> PendingList(items = dummyPendingReceivingList)
                1 -> PendingList(items = dummyPendingReleasingList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingItemsTopAppBar(
    isSearchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onPerformSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    TopAppBar(
        title = { 
            if (isSearchVisible) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("검색") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onPerformSearch()
                        focusManager.clearFocus()
                    }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            } else {
                Text("처리 대기", fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = {
            if (isSearchVisible) {
                IconButton(onClick = { onSearchVisibilityChange(false) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (isSearchVisible) {
                 IconButton(onClick = { 
                     onPerformSearch()
                     focusManager.clearFocus()
                 }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            } else {
                IconButton(onClick = { onSearchVisibilityChange(true) }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun PendingItemsTabRow(selectedTabIndex: Int, tabs: List<String>, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val containerColor = if (isSelected) Color(0xFF007BFF) else Color.White
            val contentColor = if (isSelected) Color.White else Color.Black
            val border = if (isSelected) BorderStroke(1.dp, Color(0xFF007BFF)) else BorderStroke(1.dp, Color.LightGray)

            Button(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(title)
            }
        }
    }
}

@Composable
fun PendingList(items: List<PendingItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(items) { item ->
            PendingItemCard(item = item)
        }
    }
}

@Composable
fun PendingItemCard(item: PendingItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("[${item.type} 번호] ${item.number}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("공급 업체: ${item.company}", fontSize = 14.sp, color = Color.Gray)
                    Text("부품: ${item.partName}", fontSize = 14.sp, color = Color.Gray)
                    Text("위치: ${item.location}, 수량: ${item.quantity}", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.partName,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Column {
                    Text("담당자", fontSize = 12.sp, color = Color.Gray)
                    Text(item.manager, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { /* No action */ },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.size(width = 80.dp, height = 40.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(item.status, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingItemsScreenPreview() {
    AndTheme {
        PendingItemsScreen(navController = rememberNavController())
    }
}


//메인 페이지의 처리 대기 화면 이동 페이지로 삭제해야 될 페이지.
