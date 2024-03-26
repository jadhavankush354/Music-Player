package com.example.musicplayer

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.musicplayer.entity.Song
import com.example.musicplayer.entity.User
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(navController: NavHostController, viewModel: MusicViewModel) {
    viewModel.login()
    var song by remember { mutableStateOf(viewModel.currentPlayingSong) }
    var lyrics by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var formattedTotalTime by remember { mutableStateOf("00:00") }

    LaunchedEffect(Unit) {
        while (true) {
            song = viewModel.currentPlayingSong
            isPlaying = SongHelper.mediaPlayer?.isPlaying ?: isPlaying
            currentPosition = SongHelper.mediaPlayer?.currentPosition ?: currentPosition
            duration = SongHelper.mediaPlayer?.duration ?: duration
            sliderPosition = currentPosition
            formattedTotalTime = formatTime(duration)
            if (formattedTotalTime != "00:00" && formatTime(currentPosition) >= formattedTotalTime) {
                song = if (viewModel.shuffle != "repeat one")
                    viewModel.nextSong()
                else
                    viewModel.updateCurrentPlayingSong(song)
            }
            delay(1000) // Update every second
        }
    }
    Image(painter = rememberAsyncImagePainter(song.image), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier
        .fillMaxSize()
        .alpha(0.1f))
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.weight(1f)) { MyIcon(onClick = { navController.navigate("HomeScreen") }, imageVector = Icons.Default.KeyboardArrowDown) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier
                .weight(8f)
                .fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = song.title, textAlign = TextAlign.Center, fontSize = 15.sp, modifier = Modifier.horizontalScroll(rememberScrollState()))
                Text(text = "Artist: ${song.artist}", textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.horizontalScroll(rememberScrollState()))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) { MyIcon(onClick = { viewModel.logout() }, imageVector = Icons.Default.Logout) }
        }
        if (lyrics) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(6f)
                .padding(50.dp)
                .verticalScroll(rememberScrollState())
                .clickable { lyrics = !lyrics }
                .background(Color.Transparent), Alignment.Center) { Text(text = song.lyrics, textAlign = TextAlign.Center) }
        } else {
            Image(painter = rememberAsyncImagePainter(song.image), contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .weight(6f)
                .clickable { lyrics = !lyrics }
                .clip(shape = RoundedCornerShape(8.dp)))
        }
        Column(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Slider(value = sliderPosition.toFloat(), onValueChange = { sliderPosition = it.toInt() }, onValueChangeFinished = { SongHelper.currentPosition = sliderPosition
                    SongHelper.mediaPlayer?.seekTo(sliderPosition) }, valueRange = 0f..SongHelper.duration.toFloat(), colors = SliderDefaults.colors(thumbColor = Color.Black, activeTrackColor = Color.DarkGray, inactiveTrackColor = Color.Gray))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = formatTime(sliderPosition))
                if (formattedTotalTime == "97:12")
                    Text(text = "00:00")
                else
                    Text(text = formattedTotalTime)
            }
        }
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly) {
            when (viewModel.shuffle) {
                "shuffle" -> MyIcon(onClick = { viewModel.changeShuffle() }, imageVector = Icons.Default.Shuffle)
                "repeat" -> MyIcon(onClick = { viewModel.changeShuffle() }, imageVector = Icons.Default.Repeat)
                else -> MyIcon(onClick = { viewModel.changeShuffle() },imageVector = Icons.Default.RepeatOne)
            }
            MyIcon(onClick = { song = viewModel.previousSong() }, imageVector = Icons.Default.SkipPrevious, iconSize = 40.dp)
            if (isPlaying) { MyIcon(onClick = { SongHelper.pauseStream()  }, imageVector = Icons.Default.PauseCircle, iconSize = 70.dp) } else { MyIcon(onClick = { SongHelper.playStream(song.url, viewModel.context) }, imageVector = Icons.Default.PlayCircle, iconSize = 70.dp) }
            MyIcon(onClick = { song = viewModel.nextSong() }, imageVector = Icons.Default.SkipNext, iconSize = 40.dp)
            MyIcon(onClick = { expanded = true }, imageVector = Icons.Default.Menu)
        }
        Spacer(modifier = Modifier.weight(1f))
    }
    if (expanded) {
        Column(modifier = Modifier.clickable { expanded = false }) {
            Spacer(modifier = Modifier.weight(1f))
            Card(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(30.dp)
                    .clickable { expanded = true },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), Alignment.Center
                ) {
                    Text(
                        fontSize = 20.sp,
                        text = "Current playlist",
                        style = TextStyle(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
                LazyColumn(modifier = Modifier.weight(4f)) {
                    items(viewModel.currentPlayingPlaylist) { eachSong ->
                        SongsList(song = eachSong, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LogInScreen(navController: NavController, viewModel: MusicViewModel) {
    var Email by remember { mutableStateOf("") }
    var Password by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApplicationIcon()
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Sign In", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
        Spacer(modifier = Modifier.height(30.dp))
        Email = MyOutlinedTextField(placeholder = "Your Email", leadingIcon = Icons.Default.MailOutline)
        Password = MyOutlinedTextField(placeholder = "Your Password", leadingIcon = Icons.Default.Password)
        Text(text = "Forgot Password?", modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("ForgotPasswordScreen") }, textAlign = TextAlign.End, color = Color.Blue)
        ElevatedButton(onClick = {
            val user = AuthenticateUser(viewModel.users, Email, Password)
            if (user != null) {
                viewModel.user = user
                navController.navigate("PlayerScreen")
                Toast.makeText(viewModel.context, "Authentication successful", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(viewModel.context, "Authentication failed", Toast.LENGTH_SHORT)
                    .show()
            }
        },
            modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
            enabled = Email.isNotEmpty() && Password.isNotEmpty()
        ) {
            Text(text = "LogIn")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(text = "Don't have an account?")
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Sign up!", modifier = Modifier.clickable { navController.navigate("RegistrationScreen") }, color = Color.Blue)
        }
    }
}

@Composable
fun RegistrationScreen(navController: NavController, viewModel: MusicViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var createUser by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var OTPSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    if (createUser) {
        LaunchedEffect(Unit) {
            var userExist = false
            viewModel.users.forEach { user ->
                if (user.email == email) {
                    Toast.makeText(viewModel.context, "User already exists", Toast.LENGTH_SHORT).show()
                    userExist = true
                    createUser = false
                }
            }
            if (email != viewModel.user.email) {
                viewModel.user = User()
            }
            if (viewModel.user == User() && !userExist) {
                if (password.isNotEmpty()) {
                    if (mobileNumber.isNotEmpty() && !isValidMobileNumber(mobileNumber, viewModel)) {
                        createUser = false
                    } else {
                        viewModel.musicDatabase.createUser(User(name = name, email = email, password = password,mobileNumber = mobileNumber))
                        viewModel.users.forEach { user ->
                            if (user.email == email) {
                                viewModel.user = user
                            }
                        }
                        navController.navigate("PlayerScreen")
                        Toast.makeText(viewModel.context, "Account created", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    createUser = false
                }
                userExist = false
            } else {
                viewModel.user = User()
            }
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApplicationIcon()
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Create Account", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
        Spacer(modifier = Modifier.height(10.dp))
        name = MyOutlinedTextField(placeholder = "Your Name*", leadingIcon = Icons.Default.PersonOutline)
        email = MyOutlinedTextField(placeholder = "Your Email*", leadingIcon = Icons.Default.MailOutline)
        password = MyOutlinedTextField(placeholder = "Choose a Password*", leadingIcon = Icons.Default.Password)
        confirmPassword = MyOutlinedTextField(placeholder = "Confirm Password*", leadingIcon = Icons.Default.Password)
        mobileNumber = MyOutlinedTextField(placeholder = "Your Mobile Number (Optional)", leadingIcon = Icons.Default.Phone)
        enteredOtp = MyOutlinedTextField(placeholder = "Enter OTP*", leadingIcon = Icons.Default.Security)

        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            ElevatedButton(onClick = {
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(
                        viewModel.context,
                        "Fill in all required fields",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if(password.length < 6) {
                        Toast.makeText(viewModel.context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                    } else {
                        if (password != confirmPassword) {
                            Toast.makeText(
                                viewModel.context,
                                "Passwords do not match. Please re-enter.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (enteredOtp != otp) {
                                Toast.makeText(
                                    viewModel.context,
                                    "OTP doesn't match. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                createUser = true
                            }
                        }
                    }
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && OTPSent && enteredOtp.isNotEmpty()) {
                Text(text = "Register")
            }
            ElevatedButton(onClick = {
                if (email.isEmpty()) {
                    Toast.makeText(
                        viewModel.context,
                        "Please provide your Email ID",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    otp = (1000..9999).random().toString()
                    EmailSender.sendOtpEmail(email, otp)
                    Toast.makeText(
                        viewModel.context,
                        "An OTP has been sent to your email",
                        Toast.LENGTH_SHORT
                    ).show()
                    OTPSent = true
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                enabled = email.isNotEmpty()) {
                Text(text = "Verify Email")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(text = "One of us?")
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Sign In!", modifier = Modifier.clickable { navController.navigate("LogInScreen") }, color = Color.Blue)
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavHostController, viewModel: MusicViewModel) {
    var otp by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var otpStatus by remember { mutableStateOf("Generating") }
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var updatePassword by remember { mutableStateOf(false) }
    if (updatePassword) {
        LaunchedEffect(Unit) {
            viewModel.users.forEach { user ->
                if (user.email == email) {
                    viewModel.user = user
                    return@forEach
                }
            }
            if (email != viewModel.user.email) {
                viewModel.user = User()
            }
            if (viewModel.user != User()) {

                if (newPassword.isNotEmpty()) {
                    viewModel.musicDatabase.updateUserDetails(viewModel.user, newPassword)
                    Toast.makeText(viewModel.context, "Password changed", Toast.LENGTH_SHORT).show()
                } else {
                    updatePassword = false
                }
            }
            else {
                updatePassword = false
                Toast.makeText(viewModel.context, "User does not exist", Toast.LENGTH_SHORT).show()
            }
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ApplicationIcon()
        Spacer(modifier = Modifier.height(30.dp))
        Text(text = "Forgot Password", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
        Spacer(modifier = Modifier.height(30.dp))
        if(otpStatus == "Generating"){
            email = MyOutlinedTextField(placeholder = "Email", leadingIcon = Icons.Default.MailOutline)
            enteredOtp = MyOutlinedTextField(placeholder = "OTP", leadingIcon = Icons.Default.Security)
            Row(modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ElevatedButton(onClick = {
                    if (enteredOtp.isEmpty()) {
                        Toast.makeText(viewModel.context, "Please enter the OTP", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.users
                        if (otp == enteredOtp)
                            otpStatus = "Matched"
                        else
                            Toast.makeText(viewModel.context, "Incorrect OTP, please try again", Toast.LENGTH_SHORT).show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                    enabled = enteredOtp.isNotEmpty() ) {
                    Text(text = "Forgot")
                }
                ElevatedButton(onClick = {
                    if (email.isEmpty()) {
                        Toast.makeText(viewModel.context, "Please enter your email", Toast.LENGTH_SHORT).show()
                        viewModel.user = User()
                    }
                    updatePassword = true
                    if (viewModel.user != User()) {
                        otp = (1000..9999).random().toString()
                        EmailSender.sendOtpEmail(viewModel.user.email, otp)
                        Toast.makeText(viewModel.context, "OTP sent successfully!", Toast.LENGTH_SHORT).show()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                    enabled = email.isNotEmpty()) {
                    Text(text = "Get OTP")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text(text = "Go back?")
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Sign In!", modifier = Modifier.clickable { navController.navigate("LogInScreen") }, color = Color.Blue)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        else if (otpStatus == "Matched"){
            newPassword = MyOutlinedTextField(placeholder = "New password", leadingIcon = Icons.Default.Password)
            confirmPassword = MyOutlinedTextField(placeholder = "Confirm password", leadingIcon = Icons.Default.Password)
            ElevatedButton(onClick = {
                if(newPassword.length < 6) {
                    Toast.makeText(viewModel.context, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                } else {
                    if (confirmPassword == newPassword){
                        updatePassword = true
                        navController.navigate("LogInScreen")
                    } else {
                        Toast.makeText(viewModel.context, "Password doesn't match", Toast.LENGTH_SHORT).show()
                    }
                }

            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.DarkRed)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                enabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                Text(text = "Confirm")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MusicViewModel) {
    var search by remember { mutableStateOf("") }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    var song by remember { mutableStateOf(viewModel.currentPlayingSong) }
    var currentView by remember { mutableStateOf(viewModel.currentView) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            song = viewModel.currentPlayingSong
            currentPosition = SongHelper.mediaPlayer?.currentPosition ?: currentPosition
            duration = SongHelper.mediaPlayer?.duration ?: duration
            isPlaying = SongHelper.mediaPlayer?.isPlaying ?: isPlaying
            currentView = viewModel.currentView
            if (formatTime(duration) != "00:00" && formatTime(currentPosition) >= formatTime(duration)) {
                song = if (viewModel.shuffle != "repeat one")
                    viewModel.nextSong()
                else
                    viewModel.updateCurrentPlayingSong(song)
            }
            delay(1000) // Update every second
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            search = MyOutlinedTextField(placeholder = "Music", leadingIcon = Icons.Default.Search, modifier = Modifier.weight(9f))
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.weight(1f)) { MyIcon(onClick = { navController.navigate("ProfileScreen") }, imageVector = Icons.Default.AccountCircle) }
        }
        if (search.isNotEmpty()) {
            val filteredSongs = remember {
                derivedStateOf {
                    (viewModel.songs + viewModel.localSongs).filter { song ->
                        song.title.contains(search, ignoreCase = true)
                    }
                }
            }
            LazyColumn(modifier = Modifier.weight(9f)) {
                items(filteredSongs.value) { matchedSong ->
                    SongsList(song = matchedSong, viewModel = viewModel, currentPlaylistForUpdate = viewModel.songs)
                }
            }
        } else {
            Row(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("Lists", "Songs", "Artists", "Albums").forEach { view ->
                    Column { Text(text = view, modifier = Modifier.clickable { viewModel.currentView = view })
                        Spacer(modifier = Modifier.height(8.dp))
                        if (view == currentView) {
                            Box(modifier = Modifier
                                .height(2.dp)
                                .wrapContentSize()
                                .border(width = 1.dp, color = Color.Black)) { Text(text = view) }
                        }
                    }
                }
            }
            Column(modifier = Modifier
                .fillMaxWidth()
                .weight(7f)) {
                when (currentView) {
                    "Lists" -> { ListsView(navController, viewModel) }
                    "Songs" -> { viewModel.playListSongsToShow = viewModel.songs
                        SongsView(navController, viewModel)
                    }
                    "Artists" -> { ArtistView(navController, viewModel) }
                    "Albums" -> { AlbumView(navController, viewModel) }
                }
            }

            Card(onClick = { navController.navigate("PlayerScreen") }, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .wrapContentHeight(Alignment.CenterVertically)) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween)
                {
                    MyImage(song.image)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)) {
                        Text(text = song.title, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "Artist: ${song.artist}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    MyIcon(onClick = { song = viewModel.previousSong() }, imageVector = Icons.Default.SkipPrevious)
                    Spacer(modifier = Modifier.width(16.dp))
                    if (isPlaying) {
                        MyIcon(onClick = { SongHelper.pauseStream() }, imageVector = Icons.Default.Pause)
                    } else {
                        MyIcon(onClick = { SongHelper.playStream(song.url, viewModel.context) }, imageVector = Icons.Default.PlayArrow)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    MyIcon(onClick = { song = viewModel.nextSong() }, imageVector = Icons.Default.SkipNext)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, viewModel: MusicViewModel, activity: Activity) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var OtpSent by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(false) }
    var uploadSongs by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForFilePicker(activity) { pickedUris ->
        uploadSongs = pickedUris
        uploadSongs.forEach { uri ->
            viewModel.musicDatabase.uploadLocalSong(
                viewModel.context,
                uri,
                onSuccess = {
                    Toast.makeText(viewModel.context, "Song uploaded successfully", Toast.LENGTH_SHORT).show()
                },
                onFailure = { errorMessage ->
                    Toast.makeText(viewModel.context, "Failed to upload song: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (confirmPassword) {
                viewModel.users.forEach { user ->
                    if (user.email == email) {
                        confirmPassword = false
                        Toast.makeText(viewModel.context, "This email is already associated with another account", Toast.LENGTH_SHORT).show()
                        return@forEach
                    }
                }
                if (email.isNotEmpty() && OtpSent && confirmPassword) {
                    otp = (1000..9999).random().toString()
                    EmailSender.sendOtpEmail(email, otp)
                    OtpSent = false
                }
            }
            if (update) {
                val usersUpdatedDetails = viewModel.user
                if (name.isNotEmpty()) {
                    usersUpdatedDetails.name = name
                }
                if (email.isNotEmpty()) {
                    usersUpdatedDetails.email = email
                }
                if (mobileNumber.isNotEmpty()) {
                    usersUpdatedDetails.mobileNumber = mobileNumber
                }
                viewModel.musicDatabase.updateUserDetails(usersUpdatedDetails, usersUpdatedDetails.password)
                navController.navigate("ProfileScreen")
                Toast.makeText(viewModel.context, "Your details have been updated", Toast.LENGTH_SHORT).show()
                update = false
            }
            delay(1000)
        }
    }

    Box(modifier = Modifier.padding(30.dp), Alignment.Center) {
        Row(modifier = Modifier
            .fillMaxSize()
            .clickable { isEditing = false }, verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier) { MyIcon(onClick = { navController.popBackStack() }, imageVector = Icons.Default.ArrowBackIosNew) }
            Box(modifier = Modifier) { MyIcon(onClick = { viewModel.logout() }, imageVector = Icons.Default.Logout) }
        }
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .clickable { isEditing = false }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = rememberAsyncImagePainter(R.drawable.profilepicture), contentDescription = null, modifier = Modifier
                .size(200.dp)
                .clip(shape = RoundedCornerShape(20.dp)))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = viewModel.user.name, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.Start) {
                MyIcon(imageVector = Icons.Default.MailOutline)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = viewModel.user.email, fontSize = 20.sp)
            }
            if (viewModel.user.mobileNumber.isNotEmpty()) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.Start) {
                    MyIcon(imageVector = Icons.Default.Phone)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = viewModel.user.mobileNumber, fontSize = 20.sp)
                }
            }
            if (!isEditing) {
                ElevatedButton(onClick = {
                    isEditing = !isEditing
                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)) {
                    Text(text = "Edit Profile")
                }
                ExtendedFloatingActionButton(
                    onClick = { filePickerLauncher.launch("audio/*") },
                    icon = {  },
                    text = { Text("Upload Songs") },
                    modifier = Modifier
                        .padding(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                )
            }
        }
        if (isEditing) {
            if (confirmPassword) {
                ElevatedCard(onClick = { isEditing = true }) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable { isEditing = false }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Confirm Your Password", fontSize = 15.sp)
                        password = MyOutlinedTextField(placeholder = "Your Password", leadingIcon = Icons.Default.Password)
                        Text(text = "Please enter the OTP sent to your email address", fontSize = 15.sp)
                        enteredOtp = MyOutlinedTextField(placeholder = "Received OTP", leadingIcon = Icons.Default.Security)
                        ElevatedButton(onClick = {
                            if (viewModel.user.password != password)
                                Toast.makeText(viewModel.context, "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show()
                            else {
                                if (enteredOtp == otp)
                                    update = true
                                else
                                    Toast.makeText(viewModel.context, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp), enabled = enteredOtp.isNotEmpty() && password.isNotEmpty() ) {
                            Text(text = "Confirm Update")
                        }
                        Row {
                            Text(text = "Didn't receive the OTP?")
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Resend OTP", modifier = Modifier.clickable { OtpSent = true  }, color = Color.Blue)
                        }

                    }
                }
            } else {
                ElevatedCard(onClick = { isEditing = true }) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable { isEditing = false }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Tip: Only provide the information you want to update. Be clear.", fontSize = 15.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        name = MyOutlinedTextField(placeholder = "Your Name", leadingIcon = Icons.Default.PersonOutline)
                        email = MyOutlinedTextField(placeholder = "New New Email Address", leadingIcon = Icons.Default.MailOutline)
                        mobileNumber = MyOutlinedTextField(placeholder = "Your Mobile Number", leadingIcon = Icons.Default.Phone)
                        Row(modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ElevatedButton(onClick = {
                                confirmPassword = true
                                OtpSent = true
                            }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Purple)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp), enabled = !(name.isEmpty() && email.isEmpty() && mobileNumber.isEmpty()) && !(email.isNotEmpty() && viewModel.user.email == email) && !(name.isNotEmpty() && viewModel.user.name == name) && !(mobileNumber.isNotEmpty() && viewModel.user.mobileNumber == mobileNumber) ) {
                                Text(text = "Update Details")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListsView(navController: NavController, viewModel: MusicViewModel) {
    var addPlaylist by remember { mutableStateOf(false) }
    var openNewPlayListSelector by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf(viewModel.currentView) }
    var openPlaylist by remember { mutableStateOf(viewModel.openPlaylist) }
    var newPlaylistName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            openPlaylist = viewModel.openPlaylist
            currentView = viewModel.currentView
            delay(1000) // Update every second
        }
    }

    if (openNewPlayListSelector) {
        if (newPlaylistName.isNotEmpty()) {
            AddPlayList(newPlaylistName, "Lists", navController, viewModel)
        }
    } else {
        if (currentView == "Lists" && openPlaylist.isNotEmpty()) {
            SongsView(navController, viewModel)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable { addPlaylist = !addPlaylist },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(modifier = Modifier.size(48.dp), Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Add playlist")
                    }

                    if (viewModel.allUsersPlaylists.isNotEmpty()) {
                        LazyColumn {
                            item { PlayListNames("Local songs", "Lists", navController, viewModel) }
                            items(viewModel.allUsersPlaylists) { playlist ->
                                PlayListNames(playlist, "Lists", navController, viewModel)
                            }
                        }
                    } else {
                        //TODO: No Songs text
                    }
                }
                if (addPlaylist) {
                    Card(
                        modifier = Modifier.padding(horizontal = 50.dp, vertical = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            newPlaylistName = MyOutlinedTextField(placeholder = "Playlist name", leadingIcon = Icons.Default.PlaylistAddCircle)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ElevatedButton(onClick = { addPlaylist = !addPlaylist }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Gray)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)) {
                                    Text(text = "Cancel")
                                }
                                ElevatedButton(onClick = {
                                    if (newPlaylistName.isNotEmpty()) {
                                        addPlaylist = !addPlaylist
                                        openNewPlayListSelector = !openNewPlayListSelector
                                    } else {
                                        Toast.makeText(
                                            viewModel.context,
                                            "Please enter playlist name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                                    enabled = newPlaylistName.isNotEmpty()) {
                                    Text(text = " Save ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun PlayListNames(playlistName: String, playlistType: String, navController: NavController, viewModel: MusicViewModel) {
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    LaunchedEffect(Unit) {
        playlistSongs = when (playlistType) {
            "Lists" -> {
                if (playlistName == "Local songs")
                    viewModel.localSongs
                else
                    viewModel.musicDatabase.getPlaylist(viewModel.user.userId, playlistName)
            }
            "Artists" -> viewModel.musicDatabase.getArtist(playlistName)
            "Albums" -> viewModel.musicDatabase.getAlbums(playlistName)
            else -> emptyList()
        }
    }
    if (playlistSongs.isNotEmpty()) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable {
                    viewModel.playListSongsToShow = playlistSongs
                    viewModel.openPlaylist = playlistName
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            MyImage(playlistSongs[0].image)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = playlistName, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AddPlayList(playlistName: String, playlistType: String, navController: NavController, viewModel: MusicViewModel) {
    var selectedSongs by remember { mutableStateOf(listOf<Song>()) }
    var createPlaylist by remember { mutableStateOf(false) }
    if (createPlaylist) {
        LaunchedEffect(Unit) {
            if (createPlaylist) {
                when (playlistType) {
                    "Lists" -> viewModel.musicDatabase.createPlaylist(
                        viewModel.user.userId,
                        playlistName,
                        selectedSongs
                    )

                    "Artists" -> viewModel.musicDatabase.createArtist(playlistName, selectedSongs)
                    "Albums" -> viewModel.musicDatabase.createAlbum(playlistName, selectedSongs)
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(text = "Select All")
                Checkbox(
                    checked = selectedSongs.size == viewModel.songs.size,
                    onCheckedChange = { isChecked ->
                        selectedSongs = if (isChecked) viewModel.songs else emptyList()
                    },
                    modifier = Modifier.size(48.dp)
                )
            }
            LazyColumn( modifier = Modifier
                .fillMaxWidth()
                .weight(6f)) {
                items(viewModel.songs) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            MyImage(song.image)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = song.title)
                        }
                        Checkbox(
                            checked = selectedSongs.contains(song),
                            onCheckedChange = { isChecked ->
                                selectedSongs = if (isChecked) {
                                    selectedSongs + song
                                } else {
                                    selectedSongs - song
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            ElevatedButton(
                onClick = {
                    createPlaylist = !createPlaylist
                    navController.navigate("HomeScreen")
                    viewModel.currentView = playlistType
                    Toast.makeText(viewModel.context, "Added", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(20.dp)
                    .height(IntrinsicSize.Min)
                    .align(Alignment.CenterHorizontally), colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                enabled = selectedSongs.isNotEmpty()
            ) {
                Text(text = "Add")
            }
        }
    }
}

@Composable
fun SongsView(navController: NavController, viewModel: MusicViewModel) {
    var addSong by remember { mutableStateOf(false) }
    var imageState by remember { mutableStateOf("") }
    var artistState by remember { mutableStateOf("") }
    var urlState by remember { mutableStateOf("") }
    var titleState by remember { mutableStateOf("") }
    var lyricsState by remember { mutableStateOf("") }

    Box {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (viewModel.playListSongsToShow.isNotEmpty()) {
                LazyColumn {
                    items(viewModel.playListSongsToShow) { song ->
                        if (song.url.isNotEmpty()) {
                            SongsList(song, viewModel, viewModel.playListSongsToShow)
                        }
                    }
                }
            }
        }
        if (addSong) {
            Card(modifier = Modifier.padding(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    imageState = MyOutlinedTextField(placeholder = "Image url", leadingIcon = Icons.Default.Image)
                    artistState = MyOutlinedTextField(placeholder = "Artist name", leadingIcon = Icons.Default.PersonOutline)
                    urlState = MyOutlinedTextField(placeholder = "Song url", leadingIcon = Icons.Default.MusicNote)
                    titleState = MyOutlinedTextField(placeholder = "Title", leadingIcon = Icons.Default.Title)
                    lyricsState = MyOutlinedTextField(placeholder = "Lyrics", leadingIcon = Icons.Default.Lyrics)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElevatedButton(onClick = { addSong = !addSong }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Gray)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)) {
                            Text(text = "Cancel")
                        }
                        ElevatedButton(onClick = {
                            if (
                                imageState.isNotEmpty() &&
                                artistState.isNotEmpty() &&
                                urlState.isNotEmpty() &&
                                titleState.isNotEmpty() &&
                                lyricsState.isNotEmpty()
                            ) {
                                val message = "Song added:\n" +
                                        "Image: ${imageState}\n" +
                                        "Artist: ${artistState}\n" +
                                        "URL: ${urlState}\n" +
                                        "Title: ${titleState}\n" +
                                        "Lyrics: ${lyricsState}"
                                Toast.makeText(viewModel.context, message, Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                Toast.makeText(
                                    viewModel.context,
                                    "Please enter playlist name",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                            enabled = urlState.isNotEmpty()) {
                            Text(text = " Save ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongsList(song: Song, viewModel: MusicViewModel, currentPlaylistForUpdate: List<Song> = emptyList()) {
    var currentSong by remember { mutableStateOf(viewModel.currentPlayingSong) }
    var isPlaying by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            currentSong = viewModel.currentPlayingSong
            isPlaying = SongHelper.mediaPlayer?.isPlaying ?: isPlaying
            delay(1000) // Update every second
        }
    }
    Row(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { viewModel.updateCurrentPlayingSong(song, currentPlaylistForUpdate) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MyImage(song.image)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = song.title,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist: ${song.artist}",
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (currentSong == song && isPlaying) {
            Icon(
                imageVector = Icons.Default.Equalizer,
                contentDescription = "Equalizer Icon",
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp)) // Adjust the spacer size according to your UI design
        }
    }
}

@Composable
fun ArtistView(navController: NavController, viewModel: MusicViewModel) {
    var addArtist by remember { mutableStateOf(false) }
    var openNewPlayListSelector by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf(viewModel.currentView) }
    var openPlaylist by remember { mutableStateOf(viewModel.openPlaylist) }
    var newArtistName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            openPlaylist = viewModel.openPlaylist
            currentView = viewModel.currentView
            delay(1000) // Update every second
        }
    }

    if (openNewPlayListSelector) {
        if (newArtistName.isNotEmpty()) {
            AddPlayList(newArtistName, "Artists", navController, viewModel)
        }
    } else {
        if (currentView == "Artists" && openPlaylist.isNotEmpty()) {
            SongsView(navController, viewModel)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable { addArtist = !addArtist },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(modifier = Modifier.size(48.dp), Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Add Artist")
                    }
                    if (viewModel.allArtists.isNotEmpty()) {
                        LazyColumn {
                            items(viewModel.allArtists) { artist ->
                                PlayListNames(artist, "Artists", navController, viewModel)
                            }
                        }
                    } else {
                        //TODO: No Songs text
                    }
                }
                if (addArtist) {
                    Card(
                        modifier = Modifier.padding(horizontal = 50.dp, vertical = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            newArtistName = MyOutlinedTextField(placeholder = "Artist name", leadingIcon = Icons.Default.PersonOutline)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ElevatedButton(onClick = { addArtist = !addArtist }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Gray)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)) {
                                    Text(text = "Cancel")
                                }
                                ElevatedButton(onClick = {
                                    if (newArtistName.isNotEmpty()) {
                                        addArtist = !addArtist
                                        openNewPlayListSelector = !openNewPlayListSelector
                                    } else {
                                        Toast.makeText(
                                            viewModel.context,
                                            "Please enter artist name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                                    enabled = newArtistName.isNotEmpty()) {
                                    Text(text = " Save ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumView(navController: NavController, viewModel: MusicViewModel) {
    var addAlbum by remember { mutableStateOf(false) }
    var openNewPlayListSelector by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf(viewModel.currentView) }
    var openPlaylist by remember { mutableStateOf(viewModel.openPlaylist) }
    var newAlbumName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            openPlaylist = viewModel.openPlaylist
            currentView = viewModel.currentView
            delay(1000) // Update every second
        }
    }

    if (openNewPlayListSelector) {
        if (newAlbumName.isNotEmpty()) {
            AddPlayList(newAlbumName, "Albums", navController, viewModel)
        }
    } else {
        if (currentView == "Albums" && openPlaylist.isNotEmpty()) {
            SongsView(navController, viewModel)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .clickable { addAlbum = !addAlbum },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(modifier = Modifier.size(48.dp), Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Add Album")
                    }
                    if (viewModel.allAlbums.isNotEmpty()) {
                        LazyColumn {
                            items(viewModel.allAlbums) { album ->
                                PlayListNames(album, "Albums", navController, viewModel)
                            }
                        }
                    } else {
                        //TODO: No Songs text
                    }
                }
                if (addAlbum) {
                    Card(
                        modifier = Modifier.padding(horizontal = 50.dp, vertical = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            newAlbumName = MyOutlinedTextField(placeholder = "Album name", leadingIcon = Icons.Default.Album)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ElevatedButton(onClick = { addAlbum = !addAlbum }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Gray)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)) {
                                    Text(text = "Cancel")
                                }
                                ElevatedButton(onClick = {
                                    if (newAlbumName.isNotEmpty()) {
                                        addAlbum = !addAlbum
                                        openNewPlayListSelector = !openNewPlayListSelector
                                    } else {
                                        Toast.makeText(
                                            viewModel.context,
                                            "Please enter album name",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.Green)),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp),
                                    enabled = newAlbumName.isNotEmpty()) {
                                    Text(text = " Save ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
