package com.game.tablegame.domain.shared_prefs

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.game.tablegame.R
import com.game.tablegame.core.library.balance

class AppSharedPrefs(context: Context) {
    private val sharedPrefs by lazy { context.getSharedPreferences("SHARED_PREFS", MODE_PRIVATE) }

    fun getBalance() = sharedPrefs.getLong("BALANCE", 0)
    fun increaseBalance(value: Long) =
        sharedPrefs.edit().putLong("BALANCE", getBalance() + value).apply()

    fun buyBg(value: Int) {
        val text = when (value) {
            2 -> "BG2"
            3 -> "BG3"
            4 -> "BG4"
            5 -> "BG5"
            else -> "BG6"
        }
        sharedPrefs.edit().putLong("BALANCE", balance(sharedPrefs) - 10000).apply()
        sharedPrefs.edit().putBoolean(text, true).apply()
    }

    fun isBgBought(value: Int): Boolean {
        return when (value) {
            1 -> sharedPrefs.getBoolean("BG1", true)
            2 -> sharedPrefs.getBoolean("BG2", false)
            3 -> sharedPrefs.getBoolean("BG3", false)
            4 -> sharedPrefs.getBoolean("BG4", false)
            5 -> sharedPrefs.getBoolean("BG5", false)
            else -> sharedPrefs.getBoolean("BG6", false)
        }
    }

    fun selectBg(value: Int) {
        sharedPrefs.edit().putInt("CURRENT_BG", value).apply()
    }

    fun getCurrentBg(): Int {
        return when (sharedPrefs.getInt("CURRENT_BG", 1)) {
            1 -> R.drawable.bg_game_1
            2 -> R.drawable.bg_game_2
            3 -> R.drawable.bg_game_3
            4 -> R.drawable.bg_game_4
            5 -> R.drawable.bg_game_5
            else -> R.drawable.bg_game_6
        }
    }
}