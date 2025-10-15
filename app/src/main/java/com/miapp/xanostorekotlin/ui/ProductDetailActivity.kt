package com.miapp.xanostorekotlin.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ActivityProductDetailBinding
import com.miapp.xanostorekotlin.model.Product
import com.miapp.xanostorekotlin.ui.adapter.ImageSliderAdapter

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val product = intent.getSerializableExtra(EXTRA_PRODUCT) as? Product
        if (product == null) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.productName.text = product.name
        binding.productDescription.text = product.description.orEmpty()
        binding.productPrice.text = product.price?.let {
            getString(R.string.price_format, it)
        } ?: getString(R.string.price_unknown)
        binding.productCreatedAt.isVisible = !product.createdAt.isNullOrBlank()
        binding.productCreatedAt.text = product.createdAt.orEmpty()

        val adapter = ImageSliderAdapter(product.images)
        binding.imagePager.adapter = adapter
        binding.imageIndicator.isVisible = product.images.size > 1
        updateIndicator(0, product.images.size)

        binding.imagePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicator(position, product.images.size)
            }
        })
    }

    private fun updateIndicator(position: Int, total: Int) {
        binding.imageIndicator.text = getString(
            R.string.image_indicator_pattern,
            position + 1,
            if (total == 0) 1 else total
        )
    }

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
    }
}
