package com.androidshowtime.mymemoplaces

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {


    //static variables goes inside the companion object
    companion object {

        lateinit var places: MutableList<String>
        lateinit var locations: MutableList<LatLng>


        /*late initialization for adapter to enable use of this as context which is not available inside
            the companion object*/
        lateinit var adapter: ArrayAdapter<String>

        lateinit var sharedPrefs: SharedPreferences


        /* static saveLists() placed inside companion to make it static and available to Maps Activity off
         MainActivity(without instance of MainActivity)*/
        fun saveLists() {

            /*for this to work add Gson Library in the app gradle file
         implementation 'com.google.code.gson:gson:2.8.6'*/

            //convert placesList to Json Object
            val placesListJson = Gson().toJson(places)

            //convert locationsList to JsonObject

            val locationsListJson = Gson().toJson(locations)

            //write placesList to sharedPrefs
            sharedPrefs
                    .edit()
                    .putString("placesList", placesListJson)
                    .apply()
            sharedPrefs
                    .edit()
                    .putString("locationsList", locationsListJson)
                    .apply()
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initializing Timber Library for logs
        Timber.plant(Timber.DebugTree())

        //getting the Shared Prefs File(file name inside the String Resource Value)
        sharedPrefs = getSharedPreferences(getString(R.string.shared_prefs_file),
                                           Context.MODE_PRIVATE)


        places = retrievePlacesList()

        //determine whether or not to add the first row; if it already exists don't add the row
        if (places.size == 0) {
            places.add("Add a new place")
        }
        locations = retrieveLocationsList()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, places)


        //setting adapter to the list view
        listView.adapter = adapter

        //lambda setOnItemClickListener with the unused parameters market with '_'
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("placeNumber", position)
            startActivity(intent)
        }
    }


    //retrieve the placeList from Shared Preferences File
    private fun retrievePlacesList(): MutableList<String> {

        /*for this to work add Gson Library in the app gradle file
        implementation 'com.google.code.gson:gson:2.8.6'*/


        //initialize local mutable list
        val placesList: MutableList<String>

        //retrieve jsonPlacesList
        val jsonPlacesList = sharedPrefs.getString("placesList", "")

        //set jsonPlaceList to MutableList using when statement

        placesList = when {
            // if jsonPlacesList Object is null or empty return an empty list
            jsonPlacesList.isNullOrEmpty() -> mutableListOf<String>()
            //else return the inserted saved placesList
            else -> Gson().fromJson(jsonPlacesList,
                                    object : TypeToken<MutableList<String>>() {}.type)

        }

        return placesList
    }

    //retrieve the LocationList from Shared Preferences File
    private fun retrieveLocationsList(): MutableList<LatLng> {

        /*for this to work add Gson Library in the app gradle file
       implementation 'com.google.code.gson:gson:2.8.6'*/


        //initialize local mutable list
        val locationsList: MutableList<LatLng>

        //retrieve jsonLocationsList
        val jsonLocationsList = sharedPrefs.getString("locationsList", "")

        //set jsonPlaceList to MutableList using when statement

        locationsList = when {
            // if jsonLocationsList Object is null or empty return an empty list
            jsonLocationsList.isNullOrEmpty() -> mutableListOf()
            //else return the inserted saved locationsList
            else -> Gson().fromJson(jsonLocationsList,
                                    object : TypeToken<MutableList<LatLng>>() {}.type)

        }

        return locationsList
    }

    //function to clear the list as well as the Shared Preference File
    fun clearAll(view: View) {
        locations.clear()

        places.clear()
        //clear the list but maintain the "Add Place ..." row
        places.add("Add a new place")
        adapter.notifyDataSetChanged()

        //save the new Lists
        saveLists()

    }

}