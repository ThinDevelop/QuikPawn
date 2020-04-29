package com.tss.quikpawn

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search_item.*

class SearchIItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_item)

        card.setOnClickListener(onClick)
        card2.setOnClickListener(onClick)
        card3.setOnClickListener(onClick)
        card4.setOnClickListener(onClick)
        card5.setOnClickListener(onClick)
        card6.setOnClickListener(onClick)
        card6.setOnClickListener(onClick)
        card7.setOnClickListener(onClick)

    }

    val onClick = View.OnClickListener{
        startActivity(Intent(this@SearchIItemActivity, DetailActivity::class.java))

    }

}
