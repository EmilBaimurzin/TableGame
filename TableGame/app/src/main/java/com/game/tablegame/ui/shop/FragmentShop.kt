package com.game.tablegame.ui.shop

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.game.tablegame.core.library.shortToast
import com.game.tablegame.core.library.soundClickListener
import com.game.tablegame.databinding.FragmentShopBinding
import com.game.tablegame.domain.shared_prefs.AppSharedPrefs
import com.game.tablegame.ui.other.ViewBindingFragment

class FragmentShop : ViewBindingFragment<FragmentShopBinding>(FragmentShopBinding::inflate) {
    private val sharedPrefs by lazy {
        AppSharedPrefs(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBg()
        setBalance()
        setStrings()
        binding.menuButton.soundClickListener {
            findNavController().popBackStack()
        }
        binding.buttonBg1.soundClickListener {
            buttonClick(1)
        }
        binding.buttonBg2.soundClickListener {
            buttonClick(2)
        }
        binding.buttonBg3.soundClickListener {
            buttonClick(3)
        }
        binding.buttonBg4.soundClickListener {
            buttonClick(4)
        }
        binding.buttonBg5.soundClickListener {
            buttonClick(5)
        }
        binding.buttonBg6.soundClickListener {
            buttonClick(6)
        }
    }

    private fun buttonClick(value: Int) {
        if (!sharedPrefs.isBgBought(value)) {
            if (sharedPrefs.getBalance() > 10000) {
                sharedPrefs.buyBg(value)
                sharedPrefs.selectBg(value)
                setBg()
                setBalance()
                setStrings()
            } else {
                shortToast(requireContext(), "not enough points")
            }
        } else {
            sharedPrefs.selectBg(value)
            setBg()
        }
    }


    private fun setBg() {
        binding.root.setBackgroundResource(sharedPrefs.getCurrentBg())
    }

    private fun setStrings() {
        binding.apply {
            buttonBg1.text = "SELECT"
            buttonBg2.text = if (sharedPrefs.isBgBought(2)) "SELECT" else "10000"
            buttonBg3.text = if (sharedPrefs.isBgBought(3)) "SELECT" else "10000"
            buttonBg4.text = if (sharedPrefs.isBgBought(4)) "SELECT" else "10000"
            buttonBg5.text = if (sharedPrefs.isBgBought(5)) "SELECT" else "10000"
            buttonBg6.text = if (sharedPrefs.isBgBought(6)) "SELECT" else "10000"
        }
    }

    private fun setBalance() {
        binding.balanceTextView.text = sharedPrefs.getBalance().toString()
    }
}