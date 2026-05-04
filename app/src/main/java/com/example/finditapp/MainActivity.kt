package com.example.finditapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.finditapp.data.LostItemEntity
import com.example.finditapp.viewmodel.AuthViewModel
import com.example.finditapp.viewmodel.LostItemViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val viewModel: LostItemViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation(viewModel, authViewModel)
        }
    }
}

@Composable
fun AppNavigation(viewModel: LostItemViewModel, authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser
    var currentScreen by remember { mutableStateOf("splash") }
    var selectedItemForClaim by remember { mutableStateOf<LostItemEntity?>(null) }

    LaunchedEffect(currentUser) {
        if (currentScreen != "splash") {
            currentScreen = if (currentUser == null) "login" else "main"
        }
    }

    if (selectedItemForClaim != null) {
        ProofOfOwnershipScreen(
            item = selectedItemForClaim!!,
            viewModel = viewModel,
            onBack = { selectedItemForClaim = null }
        )
    } else {
        when (currentScreen) {
            "splash" -> SplashScreen {
                currentScreen = if (currentUser == null) "login" else "main"
            }
            "login" -> LoginScreen(
                authViewModel = authViewModel,
                onNavigateToSignUp = { currentScreen = "signup" }
            )
            "signup" -> SignUpScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { currentScreen = "login" }
            )
            "main" -> MainScaffold(
                viewModel = viewModel,
                authViewModel = authViewModel,
                onItemClick = { selectedItemForClaim = it }
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onTimeout()
    }

    // Full screen logo image without extra text or spacing
    Image(
        painter = painterResource(id = R.drawable.splash_logo),
        contentDescription = "App Logo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun LoginScreen(authViewModel: AuthViewModel, onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F4E9B)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email field
        Text(text = "Email", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password field
        Text(text = "Password", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            enabled = authState !is AuthViewModel.AuthState.Loading,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(180.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3C8B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { 
                authViewModel.resetState()
                onNavigateToSignUp() 
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Don't have an account? Sign Up", color = Color.Gray)
        }
    }
}

@Composable
fun SignUpScreen(authViewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3F4E9B) // Indigo/Navy color
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name field
        Text(text = "Name", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email field
        Text(text = "Email", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password field
        Text(text = "Password", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F2),
                unfocusedContainerColor = Color(0xFFF2F2F2),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        if (authState is AuthViewModel.AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { authViewModel.signUp(name, email, password) },
            enabled = authState !is AuthViewModel.AuthState.Loading,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(180.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3C8B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Register", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { 
                authViewModel.resetState()
                onNavigateToLogin() 
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Already have an account? Login", color = Color.Gray)
        }
    }
}

@Composable
fun MainScaffold(viewModel: LostItemViewModel, authViewModel: AuthViewModel, onItemClick: (LostItemEntity) -> Unit) {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF2D3C8B)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add") },
                    selected = selectedTab == "add",
                    onClick = { selectedTab = "add" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                "home" -> HomeScreen(viewModel, onItemClick)
                "add" -> PostItemScreen(viewModel, authViewModel)
                "profile" -> ProfileScreen(authViewModel)
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: LostItemViewModel, onItemClick: (LostItemEntity) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val items by viewModel.allItems.observeAsState(emptyList())

    // Filter items based on search query
    val filteredItems = if (searchQuery.isEmpty()) {
        items
    } else {
        items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp)
    ) {
        Text(
            text = "FindIt",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3C8B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text("Search for lost and found items...") },
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2D3C8B),
                unfocusedContainerColor = Color(0xFF2D3C8B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.LightGray,
                unfocusedPlaceholderColor = Color.LightGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No matching item", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    ItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: LostItemEntity, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFEAEAF0), RoundedCornerShape(8.dp))
            ) {
                if (!item.imageUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Delete button for items without images or for easy cleanup
                if (item.imageUri.isNullOrEmpty()) {
                    IconButton(
                        onClick = { /* This is handled by a special delete mode or long press if we wanted to be fancy, but for now let's add it to Proof screen */ },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        // Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }

                // Status dot (red for LOST, green for FOUND/Claimed)
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(10.dp)
                        .background(
                            if (item.type == "LOST") Color.Red else Color.Green,
                            CircleShape
                        )
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.type,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Location removed as requested
        }
    }
}

@Composable
fun PostItemScreen(viewModel: LostItemViewModel, authViewModel: AuthViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var itemType by remember { mutableStateOf("FOUND") }

    val context = LocalContext.current
    val currentUser by authViewModel.currentUser

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val savedPath = saveImageToInternalStorage(context, it)
            imageUri = savedPath
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (itemType == "FOUND") "Post Found Item" else "Post Lost Item",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3C8B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { itemType = "FOUND" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (itemType == "FOUND") Color(0xFF2D3C8B) else Color.LightGray
                )
            ) {
                Text("FOUND")
            }
            Button(
                onClick = { itemType = "LOST" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (itemType == "LOST") Color(0xFF2D3C8B) else Color.LightGray
                )
            ) {
                Text("LOST")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFEAEAF0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri == null) {
                        Text(text = "Tap to upload image", color = Color.Gray)
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Select Image")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (itemType == "FOUND") "Location Found" else "Location Lost") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Info") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val localUri = imageUri?.let { Uri.fromFile(File(it)) }
                        viewModel.insert(
                            LostItemEntity(
                                title = title,
                                description = description,
                                locationFound = location,
                                finderContact = contact,
                                imageUri = null, // Will be set by ViewModel after upload
                                type = itemType,
                                reporterId = currentUser?.uid ?: ""
                            ),
                            localUri
                        )
                        // Reset
                        title = ""
                        description = ""
                        location = ""
                        contact = ""
                        imageUri = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3C8B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Post Item", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(authViewModel: AuthViewModel) {
    val currentUser by authViewModel.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF2D3C8B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = currentUser?.email ?: "Not logged in",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { authViewModel.signOut() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofOfOwnershipScreen(item: LostItemEntity, viewModel: LostItemViewModel, onBack: () -> Unit) {
    var proofDetails by remember { mutableStateOf("") }
    var lostLocation by remember { mutableStateOf("") }
    var showContactInfo by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    // If there is no image, allow immediate deletion or simpler verification
    val isLegacyItem = item.imageUri.isNullOrEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proof of Ownership") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Immediate Delete Button at the top for easy cleanup of old posts
            OutlinedButton(
                onClick = {
                    viewModel.delete(item)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete this old post")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!item.imageUri.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.type,
                        color = if (item.type == "LOST") Color.Red else Color.Green,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!showContactInfo) {
                Text(
                    text = "To ensure this item belongs to you, please provide a few details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = lostLocation,
                    onValueChange = { 
                        lostLocation = it 
                        showErrorMessage = false
                    },
                    label = { Text("Where did you lose this item?") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter location details") },
                    isError = showErrorMessage
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = proofDetails,
                    onValueChange = { 
                        proofDetails = it 
                        showErrorMessage = false
                    },
                    label = { Text("Unique features/Proof of Ownership") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Describe something only the owner would know") },
                    isError = showErrorMessage
                )

                if (showErrorMessage) {
                    Text(
                        text = "Invalid proof. Please check the location or details.",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        // Simple validation: check if location matches the posted location
                        // In a real app, this might be more complex or handled by the poster
                        if (lostLocation.equals(item.locationFound, ignoreCase = true) && proofDetails.isNotBlank()) {
                            showContactInfo = true
                            showErrorMessage = false
                        } else {
                            showErrorMessage = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3C8B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Claim", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                // Success State: Show Contact Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Light Green background
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Proof Verified!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please contact the founder to retrieve your item:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = item.finderContact,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3C8B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "If you have already retrieved your item, the founder can delete this post to keep the list clean.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Button for Founder (in a real app, this would check if the current user is the poster)
                Button(
                    onClick = {
                        viewModel.delete(item)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Item Post", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF2D3C8B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Back to Home", color = Color(0xFF2D3C8B))
                }
            }
        }
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "item_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            inputStream?.copyTo(output)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
