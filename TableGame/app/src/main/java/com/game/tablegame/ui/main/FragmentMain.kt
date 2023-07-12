package com.game.tablegame.ui.main

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.game.tablegame.R
import com.game.tablegame.core.library.soundClickListener
import com.game.tablegame.databinding.FragmentMainBinding
import com.game.tablegame.domain.shared_prefs.AppSharedPrefs
import com.game.tablegame.ui.other.ViewBindingFragment

class FragmentMain: ViewBindingFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            buttonStart.soundClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentGame)
            }
            buttonBuy.soundClickListener {
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentShop)
            }
            buttonExit.soundClickListener {
                requireActivity().finish()
            }
        }
    }
}