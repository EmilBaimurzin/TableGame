package com.game.tablegame.ui.game

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginStart
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.game.tablegame.R
import com.game.tablegame.core.library.soundClickListener
import com.game.tablegame.databinding.FragmentGameBinding
import com.game.tablegame.domain.game.ItemValue
import com.game.tablegame.domain.shared_prefs.AppSharedPrefs
import com.game.tablegame.ui.other.ViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FragmentGame : ViewBindingFragment<FragmentGameBinding>(FragmentGameBinding::inflate) {
    private val sharedPrefs by lazy {
        AppSharedPrefs(requireContext())
    }
    private val viewModel: GameViewModel by viewModels()
    private val xy by lazy {
        val display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Pair(size.x, size.y)
    }
    private var moveScope = CoroutineScope(Dispatchers.Default)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPlayer()
        initEnemy()
        setMovement()
        binding.root.setBackgroundResource(sharedPrefs.getCurrentBg())
        lifecycleScope.launch {
            delay(50)
            if (viewModel.items.value!!.isNotEmpty()) {
                if (viewModel.items.value!![0].x == 0f) {
                    val minX = binding.gameLayout.x + dpToPx(30)
                    val minY = binding.gameLayout.y + dpToPx(30)
                    val maxX =
                        binding.gameLayout.width + binding.gameLayout.marginStart - dpToPx(80)
                    val maxY = binding.gameLayout.height + binding.gameLayout.y - dpToPx(80)
                    viewModel.setItemsPosition(
                        minX = minX,
                        minY = minY,
                        maxX = maxX.toFloat(),
                        maxY = maxY
                    )
                }
                delay(50)
                viewModel.enemyGates = binding.enemyGates.x to binding.enemyGates.y
                viewModel.playerGates = binding.playerGates.x to binding.playerGates.y
                viewModel.moveEnemyToRandomItem()
            }
        }

        binding.menuButton.soundClickListener {
            findNavController().popBackStack()
        }

        viewModel.timer.observe(viewLifecycleOwner) {
            binding.timerTextView.text = it.toString()

            if (it == 0) {
                end()
            }
        }

        viewModel.playerPosition.observe(viewLifecycleOwner) { playerXY ->
            binding.playerView.x = playerXY.first
            binding.playerView.y = playerXY.second

            binding.playerInventoryView.x = playerXY.first
            binding.playerInventoryView.y = playerXY.second

            viewModel.items.value!!.forEach { item ->
                lifecycleScope.launch {
                    val x1 = playerXY.first
                    val y1 = playerXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = item.x
                    val y2 = item.y
                    val width2 = dpToPx(50)
                    val height2 = dpToPx(50)

                    val isIntersectingX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingX && isIntersectingY && viewModel.playerInventory.value == null) {
                        if (item.x != 0f) {
                            viewModel.addToPlayerInventory(item)
                            viewModel.removeItem(item.x, item.y)
                        }
                    }
                }
            }

            if (viewModel.playerInventory.value != null) {
                lifecycleScope.launch {
                    val x1 = playerXY.first
                    val y1 = playerXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = binding.playerGates.x
                    val y2 = binding.playerGates.y + dpToPx(50)
                    val width2 = dpToPx(100)
                    val height2 = dpToPx(50)

                    val isIntersectingWithGatesX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingWithGatesY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingWithGatesX && isIntersectingWithGatesY) {
                        viewModel.increasePlayerScore(getScore(viewModel.playerInventory.value!!.value))
                        viewModel.removeFromPlayerInventory()
                    }
                }

                lifecycleScope.launch {
                    val x1 = playerXY.first
                    val y1 = playerXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = binding.enemyGates.x
                    val y2 = binding.enemyGates.y
                    val width2 = dpToPx(100)
                    val height2 = dpToPx(50)

                    val isIntersectingWithEnemyGatesX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingWithEnemyGatesY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingWithEnemyGatesX && isIntersectingWithEnemyGatesY &&
                        viewModel.playerInventory.value!!.value == ItemValue.BOMB) {
                        viewModel.increaseEnemyScore(getScore(ItemValue.BOMB))
                        viewModel.removeFromPlayerInventory()
                    }
                }
            }
        }

        viewModel.enemyPosition.observe(viewLifecycleOwner) { enemyXY ->
            binding.enemyView.x = enemyXY.first
            binding.enemyView.y = enemyXY.second

            binding.enemyInventoryView.x = enemyXY.first
            binding.enemyInventoryView.y = enemyXY.second

            viewModel.items.value!!.forEach { item ->
                lifecycleScope.launch {
                    val x1 = enemyXY.first
                    val y1 = enemyXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = item.x
                    val y2 = item.y
                    val width2 = dpToPx(50)
                    val height2 = dpToPx(50)

                    val isIntersectingX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingX && isIntersectingY && viewModel.enemyInventory.value == null) {
                        if (item.x != 0f) {
                            viewModel.addToEnemyInventory(item)
                            viewModel.removeItem(item.x, item.y)
                        }
                    }
                }
            }

            if (viewModel.enemyInventory.value != null) {
                lifecycleScope.launch {
                    val x1 = enemyXY.first
                    val y1 = enemyXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = binding.enemyGates.x
                    val y2 = binding.enemyGates.y
                    val width2 = dpToPx(100)
                    val height2 = dpToPx(50)

                    val isIntersectingWithGatesX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingWithGatesY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingWithGatesX && isIntersectingWithGatesY) {
                        viewModel.increaseEnemyScore(getScore(viewModel.enemyInventory.value!!.value))
                        viewModel.removeFromEnemyInventory()
                    }
                }

                lifecycleScope.launch {
                    val x1 = enemyXY.first
                    val y1 = enemyXY.second
                    val width1 = dpToPx(80)
                    val height1 = dpToPx(80)

                    val x2 = binding.playerGates.x
                    val y2 = binding.playerGates.y
                    val width2 = dpToPx(100)
                    val height2 = dpToPx(50)

                    val isIntersectingWithPlayerGatesX = x1 <= x2 + width2 && x1 + width1 >= x2
                    val isIntersectingWithPlayerGatesY = y1 <= y2 + height2 && y1 + height1 >= y2

                    if (isIntersectingWithPlayerGatesX && isIntersectingWithPlayerGatesY &&
                        viewModel.enemyInventory.value!!.value == ItemValue.BOMB) {
                        viewModel.increasePlayerScore(getScore(ItemValue.BOMB))
                        viewModel.removeFromEnemyInventory()
                    }
                }
            }
        }

        viewModel.playerScore.observe(viewLifecycleOwner) {
            binding.playerScore.text = it.toString()
        }

        viewModel.enemyScore.observe(viewLifecycleOwner) {
            binding.enemyScore.text = it.toString()
        }

        viewModel.playerInventory.observe(viewLifecycleOwner) {
            if (it != null) {
                val image = when (it.value) {
                    ItemValue.COIN -> R.drawable.img_coin
                    ItemValue.BOMB -> R.drawable.img_bomb
                    ItemValue.ITEM1 -> R.drawable.img_item_1
                    ItemValue.ITEM2 -> R.drawable.img_item_2
                    ItemValue.ITEM3 -> R.drawable.img_item_3
                    ItemValue.ITEM4 -> R.drawable.img_item_4
                }
                binding.playerInventoryView.setImageResource(image)
            } else {
                binding.playerInventoryView.setImageDrawable(null)
            }
        }

        viewModel.enemyInventory.observe(viewLifecycleOwner) {
            if (it != null) {
                val image = when (it.value) {
                    ItemValue.COIN -> R.drawable.img_coin
                    ItemValue.BOMB -> R.drawable.img_bomb
                    ItemValue.ITEM1 -> R.drawable.img_item_1
                    ItemValue.ITEM2 -> R.drawable.img_item_2
                    ItemValue.ITEM3 -> R.drawable.img_item_3
                    ItemValue.ITEM4 -> R.drawable.img_item_4
                }
                binding.enemyInventoryView.setImageResource(image)
            } else {
                binding.enemyInventoryView.setImageDrawable(null)
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { itemList ->
            binding.allGameLayout.removeAllViews()
            itemList.forEach {
                val image = when (it.value) {
                    ItemValue.COIN -> R.drawable.img_coin
                    ItemValue.BOMB -> R.drawable.img_bomb
                    ItemValue.ITEM1 -> R.drawable.img_item_1
                    ItemValue.ITEM2 -> R.drawable.img_item_2
                    ItemValue.ITEM3 -> R.drawable.img_item_3
                    ItemValue.ITEM4 -> R.drawable.img_item_4
                }
                val itemView = ImageView(requireContext())
                itemView.layoutParams = ViewGroup.LayoutParams(dpToPx(50), dpToPx(50))
                itemView.setImageResource(image)
                itemView.x = it.x
                itemView.y = it.y
                binding.allGameLayout.addView(itemView)
            }
        }
    }

    private fun getScore(item: ItemValue): Int {
        return when (item) {
            ItemValue.COIN -> 50
            ItemValue.BOMB -> -100
            ItemValue.ITEM1 -> 10
            ItemValue.ITEM2 -> 10
            ItemValue.ITEM3 -> 10
            ItemValue.ITEM4 -> 10
        }
    }

    private fun initPlayer() {
        lifecycleScope.launch {
            val playerView = binding.playerView
            if (viewModel.getPlayerPosition().first == 0f) {
                delay(50)
                playerView.x = (xy.first / 2 - dpToPx(40)).toFloat()
                playerView.y = binding.playerGates.y
                viewModel.setPlayerPosition(playerView.x, playerView.y)
            } else {
                playerView.x = viewModel.getPlayerPosition().first
                playerView.y = viewModel.getPlayerPosition().second
            }
        }
    }

    private fun initEnemy() {
        lifecycleScope.launch {
            val enemyView = binding.enemyView
            if (viewModel.getEnemyPosition().first == 0f) {
                delay(50)
                enemyView.x = (xy.first / 2 - dpToPx(40)).toFloat()
                enemyView.y = binding.enemyGates.y
                viewModel.setEnemyPosition(enemyView.x, enemyView.y)
            } else {
                enemyView.x = viewModel.getEnemyPosition().first
                enemyView.y = viewModel.getEnemyPosition().second
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setMovement() {
        binding.buttonUp.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                moveScope.launch {
                    while (true) {
                        viewModel.playerMoveUp(binding.gameLayout.y)
                        delay(2)
                    }
                }
                true
            } else {
                moveScope.cancel()
                moveScope = CoroutineScope(Dispatchers.Default)
                false
            }
        }

        binding.buttonLeft.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                moveScope.launch {
                    while (true) {
                        viewModel.playerMoveLeft(binding.gameLayout.x + dpToPx(30))
                        delay(2)
                    }
                }
                true
            } else {
                moveScope.cancel()
                moveScope = CoroutineScope(Dispatchers.Default)
                false
            }
        }

        binding.buttonRight.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                moveScope.launch {
                    while (true) {
                        viewModel.playerMoveRight(
                            binding.gameLayout.x + binding.gameLayout.width - dpToPx(
                                30
                            )
                        )
                        delay(2)
                    }
                }
                true
            } else {
                moveScope.cancel()
                moveScope = CoroutineScope(Dispatchers.Default)
                false
            }
        }

        binding.buttonDown.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                moveScope.launch {
                    while (true) {
                        viewModel.playerMoveDown(
                            binding.gameLayout.y + binding.gameLayout.height - dpToPx(
                                80
                            )
                        )
                        delay(2)
                    }
                }
                true
            } else {
                moveScope.cancel()
                moveScope = CoroutineScope(Dispatchers.Default)
                false
            }
        }
    }

    private fun end() {
        viewModel.endGame()
        if (viewModel.playerScore.value!! > viewModel.enemyScore.value!!) {
            win()
        } else {
            lose()
        }
    }

    private fun win() {
        AppSharedPrefs(requireContext()).increaseBalance(viewModel.playerScore.value!!.toLong())
        findNavController().navigate(FragmentGameDirections.actionFragmentGameToDialogEnd("You Win!"))
    }

    private fun lose() {
        findNavController().navigate(FragmentGameDirections.actionFragmentGameToDialogEnd("You Lose!"))
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}