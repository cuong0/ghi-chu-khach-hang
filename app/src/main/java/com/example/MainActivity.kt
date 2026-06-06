package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.db.LeadDatabase
import com.example.data.repository.LeadRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.screens.MainScreenContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LeadViewModel
import com.example.ui.viewmodel.LeadViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge immersive design support
        enableEdgeToEdge()

        // Initialize SQLite Database and Data Repositories
        val database = LeadDatabase.getDatabase(this)
        val leadRepository = LeadRepository(database.leadDao())
        val settingsRepository = SettingsRepository(this)

        // Instantiate ViewModel with safe constructor injection factory
        val factory = LeadViewModelFactory(leadRepository, settingsRepository, this)
        val viewModel: LeadViewModel by viewModels { factory }

        setContent {
            // Live observe Dark Mode state flow from ViewModel
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainScreenContainer(viewModel = viewModel)
                }
            }
        }
    }
}

