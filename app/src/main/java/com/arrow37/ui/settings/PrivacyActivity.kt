package com.arrow37.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrow37.ui.theme.ArrowTheme
import com.arrow37.ui.theme.LocalAppStrings

class PrivacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArrowTheme {
                PrivacyScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.privacy, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last updated: August 08, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = """
                    This Privacy Policy describes Our policies and procedures on the collection, use and disclosure of Your information when You use the Service and tells You about Your privacy rights and how the law protects You.
                    
                    We use Your Personal data to provide and improve the Service. By using the Service, You agree to the collection and use of information in accordance with this Privacy Policy.
                    
                    Interpretation and Definitions
                    ------------------------------
                    
                    The words of which the initial letter is capitalized have meanings defined under the following conditions. The following definitions shall have the same meaning regardless of whether they appear in singular or in plural.
                    
                    Definitions
                    -----------
                    
                    For the purposes of this Privacy Policy:
                    
                    * Account means a unique account created for You to access our Service or parts of our Service.
                    * Application means the software program provided by the Company downloaded by You on any electronic device, named ARROW.
                    * Company (referred to as either "the Company", "We", "Us" or "Our" in this Agreement) refers to Hamraj37.
                    * Country refers to India.
                    * Device means any device that can access the Service such as a computer, a cellphone or a digital tablet.
                    * Personal Data is any information that relates to an identified or identifiable individual.
                    * Service refers to the Application.
                    
                    Collecting and Using Your Personal Data
                    ---------------------------------------
                    
                    Types of Data Collected
                    ~~~~~~~~~~~~~~~~~~~~~~~
                    
                    Personal Data
                    *************
                    
                    While using Our Service, We may ask You to provide Us with certain personally identifiable information that can be used to contact or identify You. Personally identifiable information may include, but is not limited to:
                    
                    * Usage Data
                    
                    Usage Data
                    **********
                    
                    Usage Data is collected automatically when using the Service.
                    
                    Usage Data may include information such as Your Device's Internet Protocol address (e.g. IP address), browser type, browser version, the pages of our Service that You visit, the time and date of Your visit, the time spent on those pages, unique device identifiers and other diagnostic data.
                    
                    When You access the Service by or through a mobile device, We may collect certain information automatically, including, but not limited to, the type of mobile device You use, Your mobile device unique ID, the IP address of Your mobile device, Your mobile operating system, the type of mobile Internet browser You use, unique device identifiers and other diagnostic data.
                    
                    Information Collected while Using the Application
                    *************************************************
                    
                    While using Our Application, in order to provide features of Our Application, We may collect, with Your prior permission:
                    
                    * Information regarding your location
                    
                    We use this information to provide features of Our Service, to improve and customize Our Service. The information may be uploaded to the Company's servers and/or a Service Provider's server or it may be simply stored on Your device.
                    
                    You can enable or disable access to this information at any time, through Your Device settings.
                    
                    Use of Your Personal Data
                    -------------------------
                    
                    The Company may use Personal Data for the following purposes:
                    
                    * To provide and maintain our Service, including to monitor the usage of our Service.
                    * To manage Your Account: to manage Your registration as a user of the Service.
                    * For the performance of a contract: the development, compliance and undertaking of the purchase contract for the products, items or services You have purchased or of any other contract with Us through the Service.
                    * To contact You: To contact You by email, telephone calls, SMS, or other equivalent forms of electronic communication.
                    
                    Retention of Your Personal Data
                    -------------------------------
                    
                    The Company will retain Your Personal Data only for as long as is necessary for the purposes set out in this Privacy Policy. We will retain and use Your Personal Data to the extent necessary to comply with our legal obligations.
                    
                    Security of Your Personal Data
                    ------------------------------
                    
                    The security of Your Personal Data is important to Us, but remember that no method of transmission over the Internet, or method of electronic storage is 100% secure. While We strive to use commercially acceptable means to protect Your Personal Data, We cannot guarantee its absolute security.
                    
                    Changes to this Privacy Policy
                    ------------------------------
                    
                    We may update Our Privacy Policy from time to time. We will notify You of any changes by posting the new Privacy Policy on this page.
                    
                    Contact Us
                    ----------
                    
                    If you have any questions about this Privacy Policy, You can contact us:
                    
                    * By visiting this page on our website: https://github.com/Hamraj37/arrow
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyScreenPreview() {
    ArrowTheme {
        PrivacyScreen(onBack = {})
    }
}
