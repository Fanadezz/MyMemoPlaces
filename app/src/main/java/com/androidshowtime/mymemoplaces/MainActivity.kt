package com.androidshowtime.mymemoplaces

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    //static variables goes inside the companion object
    companion object {

        val places = mutableListOf<String>()
        val locations = mutableListOf<LatLng>()

        /*late initialization for adapter to enable use of this as context which is not available inside
        the companion object*/
        lateinit var adapter: ArrayAdapter<String>


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)
        places.add("Add a new place")


        //setting adapter to the list view
        listView.adapter = adapter

        //lambda setOnItemClickListener with the unused parameters market with '_'
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("placeNumber", position)
            startActivity(intent)
        }
    }
}