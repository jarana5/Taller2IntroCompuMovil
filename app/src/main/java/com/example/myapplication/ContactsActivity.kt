package com.example.myapplication

import ContactoAdapter
import android.provider.ContactsContract
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.Contact

class ContactsActivity: AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_contacts)
		
		fetchContacts()
		
	}
	
	private fun fetchContacts() {
		val cursor = contentResolver.query(
			ContactsContract.Contacts.CONTENT_URI,
			null,
			null,
			null,
			null
		)
		
		val contactList = mutableListOf<Pair<Int, String>>()
		
		cursor?.use { it  ->
			var indice = 1
			while (it.moveToNext()) {
				val nameColumnIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
				val name = it.getString(nameColumnIndex)
				contactList.add(Pair(indice, name))
				indice++
			}
		}
		cursor?.close()
		
		val adapter = ContactoAdapter(contactList)
		val recyclerViewContactos = findViewById<RecyclerView>(R.id.recyclerViewContactos)
		recyclerViewContactos.layoutManager = LinearLayoutManager(this)
		recyclerViewContactos.adapter = adapter
	}
}