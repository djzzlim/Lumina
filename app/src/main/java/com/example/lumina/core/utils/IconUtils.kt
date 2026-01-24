package com.example.lumina.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CrueltyFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconName(icon: ImageVector): String {
        return icon.name.substringAfterLast('.')
    }

    fun getIconVector(iconName: String): ImageVector {
        return when (iconName) {
            "Language" -> Icons.Default.Language
            "Star" -> Icons.Default.Star
            "Favorite" -> Icons.Default.Favorite
            "Home" -> Icons.Default.Home
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Flight" -> Icons.Default.Flight
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Notifications" -> Icons.Default.Notifications
            "Delete" -> Icons.Default.Delete
            "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
            "FlashOn" -> Icons.Default.FlashOn
            "Cloud" -> Icons.Default.Cloud
            "WbSunny" -> Icons.Default.WbSunny
            "Nightlight" -> Icons.Default.Nightlight
            "AccessTime" -> Icons.Default.AccessTime
            "Settings" -> Icons.Default.Settings
            "VideogameAsset" -> Icons.Default.VideogameAsset
            "Face" -> Icons.Default.Face
            "Visibility" -> Icons.Default.Visibility
            "Sailing" -> Icons.Default.Sailing
            "Tv" -> Icons.Default.Tv
            "Flag" -> Icons.Default.Flag
            "SportsSoccer" -> Icons.Default.SportsSoccer
            "SportsBaseball" -> Icons.Default.SportsBaseball
            "SportsBasketball" -> Icons.Default.SportsBasketball
            "SportsFootball" -> Icons.Default.SportsFootball
            "SportsTennis" -> Icons.Default.SportsTennis
            "DownhillSkiing" -> Icons.Default.DownhillSkiing
            "Circle" -> Icons.Default.Circle
            "Sports" -> Icons.Default.Sports
            "EmojiEvents" -> Icons.Default.EmojiEvents
            "Pets" -> Icons.Default.Pets
            "Adb" -> Icons.Default.Adb
            "FlutterDash" -> Icons.Default.FlutterDash
            "CrueltyFree" -> Icons.Default.CrueltyFree
            "BugReport" -> Icons.Default.BugReport
            "WaterDrop" -> Icons.Default.WaterDrop
            "Eco" -> Icons.Default.Eco
            "LocalFlorist" -> Icons.Default.LocalFlorist
            "Park" -> Icons.Default.Park
            "FilterVintage" -> Icons.Default.FilterVintage
            "Science" -> Icons.Default.Science
            "Email" -> Icons.Default.Email
            "Chat" -> Icons.AutoMirrored.Filled.Chat
            "Forum" -> Icons.Default.Forum
            "Groups" -> Icons.Default.Groups
            "Person" -> Icons.Default.Person
            "Public" -> Icons.Default.Public
            "Share" -> Icons.Default.Share
            "CameraAlt" -> Icons.Default.CameraAlt
            "PhotoLibrary" -> Icons.Default.PhotoLibrary
            "Brush" -> Icons.Default.Brush
            "Palette" -> Icons.Default.Palette
            "Search" -> Icons.Default.Search
            "Lock" -> Icons.Default.Lock
            "Shield" -> Icons.Default.Shield
            "Key" -> Icons.Default.Key
            "Storefront" -> Icons.Default.Storefront
            "Wallet" -> Icons.Default.Wallet
            "CreditCard" -> Icons.Default.CreditCard
            "Explore" -> Icons.Default.Explore
            "Map" -> Icons.Default.Map
            "Restaurant" -> Icons.Default.Restaurant
            "LocalCafe" -> Icons.Default.LocalCafe
            "Fastfood" -> Icons.Default.Fastfood
            "MedicalServices" -> Icons.Default.MedicalServices
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "Laptop" -> Icons.Default.Laptop
            "Smartphone" -> Icons.Default.Smartphone
            "MenuBook" -> Icons.AutoMirrored.Filled.MenuBook
            "Edit" -> Icons.Default.Edit
            "PlayCircle" -> Icons.Default.PlayCircle
            "Movie" -> Icons.Default.Movie
            "MusicNote" -> Icons.Default.MusicNote
            "Radio" -> Icons.Default.Radio
            "Build" -> Icons.Default.Build
            else -> Icons.Default.Language
        }
    }
}
