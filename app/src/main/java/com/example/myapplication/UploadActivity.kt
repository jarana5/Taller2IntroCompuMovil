package com.example.myapplication

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UploadActivity : AppCompatActivity() {
	private lateinit var imageView: ImageView
	private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
	private lateinit var currentPhotoPath: String
	private val takePicture: ActivityResultLauncher<Intent> =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				val imageBitmap = result.data?.extras?.get("data") as Bitmap
				imageView.setImageBitmap(imageBitmap)
				val imageFile = createImageFile()
				try {
					
					val contentValues = ContentValues().apply {
						put(MediaStore.Images.Media.DISPLAY_NAME, "my_image.jpg")
						put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
					}
					val resolver = contentResolver
					val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
					val outputStream = resolver.openOutputStream(imageUri!!)
					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
					outputStream?.close()
				} catch (e: Exception) {
					Log.d("ghost", "ghost: $e")
				}
				
				// Display the captured image in the ImageView
			}
		}
	
	companion object {
		private const val REQUEST_CAMERA_PERMISSION = 101
	}
	
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upload)
		imageView = findViewById(R.id.imageView)
		
		val btnGallery = findViewById<Button>(R.id.btnGallery)
		val takePhotoButton: Button = findViewById(R.id.btnCamera)
		
		
		
		cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result: Boolean? ->
			if (result == true) {
				val photoUri = Uri.parse(currentPhotoPath)
				displayImage(photoUri)
			}
		}
			
			takePhotoButton.setOnClickListener {
				dispatchTakePictureIntent()
			}
		
		
		val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				val data: Intent? = result.data
				data?.data?.let { selectedImageUri ->
					imageView.setImageURI(selectedImageUri)
				}
			}
		}
		
		btnGallery.setOnClickListener {
			// Launch the gallery to pick an image
			val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
			galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)
			galleryIntent.type = "image/*"
			galleryLauncher.launch(galleryIntent)
		}
		
	}
	
	private fun createImageFile(): File {
		val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
		val imageFileName = "JPEG_$timeStamp.jpg"
		val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
		return File(storageDir, imageFileName)
	}
	
	private fun dispatchTakePictureIntent() {
		val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		if (takePictureIntent.resolveActivity(packageManager) != null) {
			takePicture.launch(takePictureIntent)
		}
	}
	
	private fun displayImage(imageUri: Uri) {
		imageView.setImageURI(imageUri)
	}
	
}