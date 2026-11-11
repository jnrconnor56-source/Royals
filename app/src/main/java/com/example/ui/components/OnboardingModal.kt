package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfile
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950

val GHANAIAN_SHS_LIST = listOf(
    "Akim Swedru SHS (AKIMSS)",
    "Presbyterian Boys' SHS (PRESEC Legon)",
    "Prempeh College",
    "Achimota School",
    "Wesley Girls' High School",
    "Mfantsipim School",
    "Opoku Ware SHS (OWASS)",
    "Holy Child School",
    "Aburi Girls' SHS",
    "St. Augustine's College",
    "Adisadel College",
    "Ghana National College",
    "Mawuli School",
    "Keta Senior High Technical",
    "Tamale SHS",
    "St. Rose's Senior High",
    "Pope John SHS & Minor Seminary",
    "Koforidua Secondary Technical (SECTECH)",
    "Ofori Panin SHS (OPASS)"
)

val ACADEMIC_PROGRAMS = listOf(
    "General Science",
    "General Arts",
    "Business",
    "Visual Arts",
    "Home Economics",
    "Agricultural Science",
    "Technical"
)

val MAJOR_CITIES = listOf(
    "Accra, Ghana" to Pair(5.6037, -0.1870),
    "Kumasi, Ghana" to Pair(6.6885, -1.6244),
    "Cape Coast, Ghana" to Pair(5.1054, -1.2466),
    "Takoradi, Ghana" to Pair(4.9016, -1.7831),
    "Tamale, Ghana" to Pair(9.4008, -0.8393),
    "Koforidua, Ghana" to Pair(6.0941, -0.2591),
    "London, UK" to Pair(51.5074, -0.1278),
    "Atlanta, US" to Pair(33.7490, -84.3880),
    "Toronto, Canada" to Pair(43.6532, -79.3832),
    "Manchester, UK" to Pair(53.4808, -2.2426)
)

@Composable
fun OnboardingModal(
    existingProfile: UserProfile,
    onComplete: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedSchool by remember { mutableStateOf(existingProfile.shsSchool.ifEmpty { GHANAIAN_SHS_LIST.first() }) }
    var selectedCourse by remember { mutableStateOf(existingProfile.course.ifEmpty { ACADEMIC_PROGRAMS.first() }) }
    var stream by remember { mutableStateOf(existingProfile.stream.ifEmpty { "Science 1" }) }
    var gradYearStr by remember { mutableStateOf(existingProfile.gradYear.toString()) }
    var personalPhone by remember { mutableStateOf(existingProfile.personalPhone) }
    var parentPhone by remember { mutableStateOf(existingProfile.parentPhone) }
    var selectedCityPair by remember { mutableStateOf(MAJOR_CITIES.first()) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateDark950)
                .padding(16.dp),
            color = SlateDark950
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyaltyGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "School Logo",
                                tint = SlateDark950
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Royals Connect",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Setup Alumni Profile • Step $step of 3",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3).forEach { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (step >= i) MaterialTheme.colorScheme.primary else SlateDark800
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when (step) {
                    1 -> {
                        // Step 1: Select Ghanaian SHS
                        Text(
                            text = "Step 1: Select your Ghanaian SHS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Choose your alma mater to connect with fellow Royals & alumni.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(GHANAIAN_SHS_LIST) { school ->
                                val isSelected = school == selectedSchool
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSchool = school }
                                        .testTag("school_option_${school.take(5)}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else SlateDark900
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = school,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_step1_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Continue to Academic Program")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                        }
                    }

                    2 -> {
                        // Step 2: Select Academic Program
                        Text(
                            text = "Step 2: Select Academic Program",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Select your field of study during your SHS years.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ACADEMIC_PROGRAMS) { course ->
                                val isSelected = course == selectedCourse
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCourse = course }
                                        .testTag("course_option_$course"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else SlateDark900
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = course,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { step = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDark800)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = { step = 3 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("onboarding_step2_next"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Next Step")
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Stream, Grad Year, Phone, Location
                        Text(
                            text = "Step 3: Class Stream & Location Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Specify your stream hub, graduation year, and contact info.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = stream,
                                    onValueChange = { stream = it },
                                    label = { Text("Class Stream (e.g., Science 1)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_stream_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SlateDark900,
                                        unfocusedContainerColor = SlateDark900
                                    )
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = gradYearStr,
                                    onValueChange = { gradYearStr = it },
                                    label = { Text("Graduation Year") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_year_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SlateDark900,
                                        unfocusedContainerColor = SlateDark900
                                    )
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = personalPhone,
                                    onValueChange = { personalPhone = it },
                                    label = { Text("Personal Phone (Optional)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_phone_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SlateDark900,
                                        unfocusedContainerColor = SlateDark900
                                    )
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = parentPhone,
                                    onValueChange = { parentPhone = it },
                                    label = { Text("Parent / Emergency Phone") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_parent_phone_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SlateDark900,
                                        unfocusedContainerColor = SlateDark900
                                    )
                                )
                            }
                            item {
                                Text(
                                    text = "Current City / Region",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MAJOR_CITIES.forEach { cityPair ->
                                        val isSel = cityPair.first == selectedCityPair.first
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedCityPair = cityPair }
                                                .testTag("city_${cityPair.first.take(5)}"),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else SlateDark900
                                            )
                                        ) {
                                            Text(
                                                text = cityPair.first,
                                                modifier = Modifier.padding(12.dp),
                                                fontSize = 13.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDark800)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    val year = gradYearStr.toIntOrNull() ?: 2026
                                    val cityCountry = selectedCityPair.first.split(",")
                                    val cityName = cityCountry.getOrNull(0)?.trim() ?: "Accra"
                                    val countryName = cityCountry.getOrNull(1)?.trim() ?: "Ghana"
                                    val updated = existingProfile.copy(
                                        shsSchool = selectedSchool,
                                        course = selectedCourse,
                                        stream = stream.ifEmpty { "Science 1" },
                                        gradYear = year,
                                        personalPhone = personalPhone,
                                        parentPhone = parentPhone,
                                        city = cityName,
                                        country = countryName,
                                        lat = selectedCityPair.second.first,
                                        lng = selectedCityPair.second.second,
                                        isOnboarded = true
                                    )
                                    onComplete(updated)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("onboarding_complete_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyaltyGold)
                            ) {
                                Text("Join Network 🎉", color = SlateDark950, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
