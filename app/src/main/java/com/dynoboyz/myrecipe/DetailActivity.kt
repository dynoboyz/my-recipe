package com.dynoboyz.myrecipe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaredrummler.materialspinner.MaterialSpinner
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class DetailActivity : AppCompatActivity() {
    private lateinit var llDetail: LinearLayout
    private lateinit var llAdd: LinearLayout
    private lateinit var updateItem: MenuItem
    private lateinit var saveItem: MenuItem
    private lateinit var deleteItem: MenuItem
    private lateinit var sharedPref: SharedPreferences
    private lateinit var spinnerItem: MutableList<String>
    private var parentLayout: LinearLayout? = null
    private var editImgRecipeData: EditText? = null
    private var editImgRecipe: ImageView? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        parentLayout = findViewById(R.id.content)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        editImgRecipeData = findViewById(R.id.editImgRecipeData)
        editImgRecipe = findViewById(R.id.editImgRecipe)

        editImgRecipe!!.setOnClickListener { showPictureDialog() }

        // get spinner recipe types from recipetypes.xml
        val spinner = findViewById<MaterialSpinner>(R.id.editType)
        spinnerItem = mutableListOf()
        try {
            val inStream = assets.open("recipetypes.xml")
            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = builderFactory.newDocumentBuilder()
            val doc = docBuilder.parse(inStream)
            val nList = doc.getElementsByTagName("recipetype")
            for (i in 0 until nList.length) {
                spinnerItem.add(nList.item(i).firstChild.nodeValue)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        spinner.setItems(spinnerItem)

        llDetail = findViewById(R.id.llDetail)
        llAdd = findViewById(R.id.llAdd)

        sharedPref = this.getSharedPreferences(getString(R.string.app_storage), Context.MODE_PRIVATE)

        val id = intent.getStringExtra("id")
        if (id == "") {
            supportActionBar?.title = "Add New Recipe"

            llDetail.isVisible = false
            llAdd.isVisible = true
        } else {
            supportActionBar?.title = "My Recipe Detail"

            val txtTitle = findViewById<TextView>(R.id.txtTitle)
            val txtType = findViewById<TextView>(R.id.txtType)
            val img = findViewById<ImageView>(R.id.imgRecipe)
            val txtIngredients = findViewById<TextView>(R.id.txtIngredients)
            val txtSteps = findViewById<TextView>(R.id.txtSteps)

            txtTitle.text = intent.getStringExtra("title")
            txtType.text = intent.getStringExtra("type")
            val imageBytes = Base64.decode(intent.getStringExtra("image"), Base64.DEFAULT)
            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            img.setImageBitmap(decodedImage)
            if (intent.getStringExtra("ingredients") == "") {
                txtIngredients.text = "N/A"
            } else {
                txtIngredients.text = intent.getStringExtra("ingredients")
            }
            if (intent.getStringExtra("steps") == "") {
                txtSteps.text = "N/A"
            } else {
                txtSteps.text = intent.getStringExtra("steps")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        // hide/show update button
        val id:String? = intent.getStringExtra("id")
        val isVisible = (id != "")

        updateItem = menu.findItem(R.id.update)
        updateItem.isVisible = isVisible

        deleteItem = menu.findItem(R.id.delete)
        deleteItem.isVisible = !isVisible

        saveItem = menu.findItem(R.id.save)
        saveItem.isVisible = !isVisible
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = intent.getStringExtra("id")

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editType = findViewById<MaterialSpinner>(R.id.editType)
        val editIngredients = findViewById<EditText>(R.id.editIngredients)
        val editSteps = findViewById<EditText>(R.id.editSteps)

        var newRecipe: MainActivity.Recipe = MainActivity.Recipe(id, editType.text.toString(), editTitle.text.toString(), editIngredients.text.toString(), editSteps.text.toString(), editImgRecipeData?.text.toString())

        return when (item.itemId) {
            android.R.id.home -> {
                if (editTitle.text.toString() == "") {
                    finish()
                } else {
                    onBackPressed()
                }
                true
            }
            R.id.update -> {
                supportActionBar?.title = "Edit My Recipe"

                llDetail.isVisible = false
                llAdd.isVisible = true
                updateItem.isVisible = false
                saveItem.isVisible = true
                deleteItem.isVisible = true

                var title = intent.getStringExtra("title")
                if (newRecipe.getTitle() != "") { title = newRecipe.getTitle() }

                var type = intent.getStringExtra("type")
                if (newRecipe.getType() != "") { type = newRecipe.getType() }

                var image = intent.getStringExtra("image")
                if (newRecipe.getImage() != "") { image = newRecipe.getImage() }

                var ingredients = intent.getStringExtra("ingredients")
                if (newRecipe.getIngredients() != "") { ingredients = newRecipe.getIngredients() }

                var steps = intent.getStringExtra("steps")
                if (newRecipe.getSteps() != "") { steps = newRecipe.getSteps() }

                editTitle.setText(title)
                // select the correct index for recipe type
                for ((i, sItem) in spinnerItem.withIndex()) {
                    if (sItem == type) {
                        editType.selectedIndex = i + 1
                    }
                }
                editImgRecipeData?.setText(image)
                val imageBytes = Base64.decode(image, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                editImgRecipe?.setImageBitmap(decodedImage)
                if (ingredients == "") {
                    editIngredients.setText("")
                } else {
                    editIngredients.setText(ingredients)
                }
                if (steps == "") {
                    editSteps.setText("")
                } else {
                    editSteps.setText(steps)
                }
                true
            }
            R.id.save -> {
                if (editTitle.text.toString() == "") {
                    parentLayout?.let {
                        Snackbar.make(
                            it,
                            getString(R.string.error_title),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    editTitle.error = getString(R.string.error_title)
                    true
                } else {
                    editTitle.error = null

                    // get data from persist storage
                    sharedPref = this.getSharedPreferences(getString(R.string.app_storage), Context.MODE_PRIVATE)
                    val gson = Gson()
                    val json: String? = sharedPref.getString(getString(R.string.app_storage_key), null)
                    val collectionType = object : TypeToken<Collection<MainActivity.Recipe?>?>() {}.type
                    val persistRecipes: ArrayList<MainActivity.Recipe>
                    persistRecipes = gson.fromJson(json, collectionType)

                    // save recipes to storage based on id
                    var largestId = "0"
                    val newRecipes: ArrayList<MainActivity.Recipe> = ArrayList()
                    for (recipe in persistRecipes) {
                        if (recipe.getId() == id) {
                            newRecipes.add(newRecipe)
                        } else {
                            newRecipes.add(recipe)
                        }

                        if (largestId.toInt() < recipe.getId().toInt()) {
                            largestId = recipe.getId()
                        }
                    }
                    if (id == "") {
                        newRecipe = MainActivity.Recipe((largestId.toInt() + 1).toString(), editType.text.toString(), editTitle.text.toString(), editIngredients.text.toString(), editSteps.text.toString(), editImgRecipeData?.text.toString())
                        newRecipes.add(newRecipe)
                    }
                    with (sharedPref.edit()) {
                        putString(getString(R.string.app_storage_key), gson.toJson(newRecipes))
                        commit()
                    }

                    // return to detail page
                    supportActionBar?.title = "My Recipe Detail"

                    llDetail.isVisible = true
                    llAdd.isVisible = false
                    updateItem.isVisible = true
                    saveItem.isVisible = false
                    deleteItem.isVisible = false

                    val txtTitle = findViewById<TextView>(R.id.txtTitle)
                    val txtType = findViewById<TextView>(R.id.txtType)
                    val img = findViewById<ImageView>(R.id.imgRecipe)
                    val txtIngredients = findViewById<TextView>(R.id.txtIngredients)
                    val txtSteps = findViewById<TextView>(R.id.txtSteps)

                    txtTitle.text = newRecipe.getTitle()
                    txtType.text = newRecipe.getType()
                    val imageBytes = Base64.decode(newRecipe.getImage(), Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    img.setImageBitmap(decodedImage)
                    if (newRecipe.getIngredients() == "") {
                        txtIngredients.text = "N/A"
                    } else {
                        txtIngredients.text = newRecipe.getIngredients()
                    }
                    if (newRecipe.getSteps() == "") {
                        txtSteps.text = "N/A"
                    } else {
                        txtSteps.text = newRecipe.getSteps()
                    }
                    true
                }
            }
            R.id.delete -> {
                // get data from persist storage
                sharedPref = this.getSharedPreferences(getString(R.string.app_storage), Context.MODE_PRIVATE)
                val gson = Gson()
                val json: String? = sharedPref.getString(getString(R.string.app_storage_key), null)
                val collectionType = object : TypeToken<Collection<MainActivity.Recipe?>?>() {}.type
                val persistRecipes: ArrayList<MainActivity.Recipe>
                persistRecipes = gson.fromJson(json, collectionType)

                // delete recipes to storage based on id
                val newRecipes: ArrayList<MainActivity.Recipe> = ArrayList()
                for (recipe in persistRecipes) {
                    if (recipe.getId() != id) {
                        newRecipes.add(recipe)
                    }
                }
                with (sharedPref.edit()) {
                    putString(getString(R.string.app_storage_key), gson.toJson(newRecipes))
                    commit()
                }

                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (!llDetail.isVisible) {
            llDetail.isVisible = true
            llAdd.isVisible = false
            updateItem.isVisible = true
            saveItem.isVisible = false
            deleteItem.isVisible = false
        } else {
            super.onBackPressed()
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    editImgRecipe!!.setImageBitmap(bitmap)
                    editImgRecipeData?.setText(encodeImage(bitmap))

                } catch (e: IOException) {
                    e.printStackTrace()
                    parentLayout?.let {
                        Snackbar.make(
                            it,
                            "Failed to load image from Gallery!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

            }

        }
        else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            editImgRecipe!!.setImageBitmap(thumbnail)
            editImgRecipeData?.setText(encodeImage(thumbnail))
        }
    }

    private fun encodeImage(bm: Bitmap): String? {
        val byteArr = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 70, byteArr)
        val b: ByteArray = byteArr.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
