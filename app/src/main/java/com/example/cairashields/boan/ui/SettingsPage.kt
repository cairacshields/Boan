package com.example.cairashields.boan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.example.cairashields.boan.Helper.Utils
import com.example.cairashields.boan.Helper.imageHelper
import com.example.cairashields.boan.MainActivity
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import org.json.JSONObject

class SettingsPage: AppCompatActivity() {
    @BindView(R.id.listView)lateinit var mListView: ListView
    @BindView(R.id.bottom_nav) lateinit var mBottomNav: AHBottomNavigation

    var mDatabaseReference: DatabaseReference? = null
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var mStorage: FirebaseStorage? = null
    var mStorageRef: StorageReference? = null
    var database: FirebaseDatabase? = null
    lateinit var auth: FirebaseAuth
    private var mArrayList = arrayListOf<SettingsItems>()
    private lateinit var mAdapter: ListItemAdapter
    var isLender: Boolean? = null
    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)
        ButterKnife.bind(this)


        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference
        mDatabaseReferenceUsers = database!!.reference.child("users")
        mStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        mListView = findViewById(R.id.listView)
        mBottomNav = findViewById(R.id.bottom_nav)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Settings"

        isLender = intent.extras.getBoolean("isLender")

        var navItem1 : AHBottomNavigationItem? = null
        var navItem2 : AHBottomNavigationItem? = null
        var navItem3 : AHBottomNavigationItem? = null

        //We're going to need the users info
        mDatabaseReferenceUsers!!.child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                user =  snapshot.getValue(Users::class.java)
                user?.let { user ->
                    isLender = user.isLender

                }

            }

        })

        navItem1 = AHBottomNavigationItem(R.string.my_account, R.drawable.ic_user, R.color.colorPrimary)
        if (isLender!!) {
            navItem2 = AHBottomNavigationItem(R.string.borrow_requests, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent)
        } else {
            navItem2 = AHBottomNavigationItem(R.string.borrow_form, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent)
        }
        navItem3 = AHBottomNavigationItem(R.string.settings, R.drawable.ic_settings, R.color.colorPrimaryDark)

        mBottomNav.addItem(navItem1)
        mBottomNav.addItem(navItem2)
        mBottomNav.addItem(navItem3)

        mBottomNav.defaultBackgroundColor = Utils.fetchColor(R.color.white, applicationContext)
        mBottomNav.accentColor = Utils.fetchColor(R.color.mainOrange, applicationContext)
        mBottomNav.inactiveColor = Utils.fetchColor(R.color.grey, applicationContext)
        mBottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW;
        mBottomNav.setCurrentItem(2)

       // mBottomNav.currentItem = 2
        mBottomNav.setOnTabSelectedListener { position, wasSelected ->
            //Move to another activity/fragment based on click position
            var intent = Intent()
            when(position){
                0 ->{intent = Intent(this, Profile::class.java)
                    intent.putExtra("isLender", isLender!!)
                    startActivity(intent)}
                1 ->{
                    if(isLender!!) {
                        intent = Intent(this, SwipeBorrowRequests::class.java)
                        startActivity(intent)
                    }else{
                        intent = Intent(this, BorrowForm::class.java)
                        intent.putExtra("isLender",isLender!!)
                        startActivity(intent)
                    }
                }
                2 ->{}
                else -> {}
            }

            wasSelected
        }

        mArrayList.add(SettingsItems("Edit Profile"))
        mArrayList.add(SettingsItems("Edit Payment Details"))
        mArrayList.add(SettingsItems("Delete Account"))

        mAdapter = ListItemAdapter(this, mArrayList)
        mListView.adapter = mAdapter

        mListView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = mListView.getItemAtPosition(position) as Profile.ProfileListItems

            when(position){
                0 -> {
                   //Edit Profile

                    //We'll start v1 of the app with a simple dialog to update the profile pic and user summary?
                    val layoutInflater = LayoutInflater.from(this)
                    val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
                    var alertDialog: AlertDialog? = null
                    val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialog_AppCompat)
                    alertDialogBuilder.setView(view)

                    val close = view.findViewById<ImageButton>(R.id.close)
                    val save = view.findViewById<Button>(R.id.save_profile)
                    val summary = view.findViewById<EditText>(R.id.summary)
                    val facebookLink = view.findViewById<EditText>(R.id.facebookLink)
                    val instagramLink = view.findViewById<EditText>(R.id.instagramLink)
                    val profilePic = view.findViewById<ImageView>(R.id.image)

                    mDatabaseReferenceUsers!!.child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            val user = data.getValue(Users::class.java)
                            user?.let {user ->

                                user.summary?.let {
                                    summary.setText(it)
                                }

                                user.facebookLink?.let {
                                    facebookLink.setText(it)
                                }

                                user.instagramLink?.let {
                                    instagramLink.setText(it)
                                }

                                user.profileImage?.let {imageLocation ->
                                    mStorageRef = mStorage!!.reference.child(imageLocation)
                                    mStorageRef!!.downloadUrl.addOnSuccessListener {
                                        Log.v("GOT THE URI", "SUCCESS")

                                        Picasso.get().load(it).fit().into(profilePic)

                                    }.addOnFailureListener {
                                        // Handle any errors
                                        it.printStackTrace()
                                    }
                                }
                            }
                        }
                    })

                    close.setOnClickListener {
                        alertDialog?.dismiss()
                    }

                    profilePic.setOnClickListener {
                        imageHelper.getImage(this)
                    }

                    save.setOnClickListener {
                        val newSummary = summary.text.toString()
                        val newFacebookLink = facebookLink.text.toString()
                        val newInstagramLink = instagramLink.text.toString()

                        val newInfo = HashMap<String, Any>()
                        if(newSummary.isNotEmpty()) {
                            newInfo.put("summary", newSummary)
                        }
                        if(newFacebookLink.isNotEmpty()) {
                            newInfo.put("facebookLink", newFacebookLink)
                        }
                        if (newInstagramLink.isNotEmpty()) {
                            newInfo.put("instagramLink", newInstagramLink)
                        }

                        mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).updateChildren(newInfo).addOnCompleteListener {
                            if(it.isSuccessful){
                                Log.v("Profile", "Updated successfully")
                                Toast.makeText(this, "Profile Update Success!", Toast.LENGTH_SHORT).show()
                            }else{
                                Log.v("Profile", "Update Failed")
                                Toast.makeText(this, "Profile Update Failed, please try again.", Toast.LENGTH_SHORT).show()
                            }
                            alertDialog!!.dismiss()
                        }
                    }

                    var imageLocation: String? = null
                    mDatabaseReferenceUsers!!.child(auth.currentUser!!.uid).child("profileImage").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            imageLocation = data.value.toString()
                            mStorageRef = mStorage!!.reference.child(imageLocation!!)
                            mStorageRef!!.downloadUrl.addOnSuccessListener {
                                Log.v("GOT THE URI", "SUCCESS")
                                Picasso.get().load(it).fit().into(profilePic)
                            }.addOnFailureListener {
                                // Handle any errors
                                it.printStackTrace()
                            }
                        }
                    })

                    alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    alertDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                }
                1-> {
                   //Edit Payment Details
                    val layoutInflater = LayoutInflater.from(this)
                    val view = layoutInflater.inflate(R.layout.dialog_credit_card, null)
                    var alertDialog: AlertDialog? = null
                    val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialog_AppCompat)
                    alertDialogBuilder.setView(view)

                    val close = view.findViewById<ImageButton>(R.id.close)
                    val save = view.findViewById<Button>(R.id.save_card)
                    val cardInputWidget = view.findViewById<CardInputWidget>(R.id.card_input_widget)

                    close.setOnClickListener {
                        alertDialog?.dismiss()
                    }

                    save.setOnClickListener {
                        val cardToSave = cardInputWidget.card

                        if (cardToSave == null) {
                            //Card not good
                            // mErrorDialogHandle r.showError("Invalid Card Data")
                        } else {
                            //Card info is good
                            val stripe = Stripe(this, "pk_test_DkwOovybuSQN6dDkVLOODzn1");
                            // TODO Need to make sure it is not a prepaid card ....
                            stripe.createToken(
                                    cardToSave,
                                    object : TokenCallback {
                                        override fun onSuccess(token: Token?) {
                                            Log.v("Token!", "Token Created!!" + token!!.getId())
                                            //Update the users token in the DB
                                            mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).child("token").setValue(token.id).addOnCompleteListener {
                                                if(it.isSuccessful){
                                                    Toast.makeText(this@SettingsPage, "DB updated card token!", Toast.LENGTH_LONG).show()
                                                }else{
                                                    Toast.makeText(this@SettingsPage, "Unable to update the token in DB.", Toast.LENGTH_LONG).show()
                                                }
                                            }

                                        }

                                        override fun onError(error: Exception?) {
                                            Log.v("Settings Activity", "Unable to update the card info")
                                            Toast.makeText(this@SettingsPage, "Unable to generate a token.", Toast.LENGTH_LONG).show()
                                            error!!.printStackTrace()
                                            //TODO Handle what happens when card is invalid... currently app crashes *face palm*
                                        }

                                    })
                            alertDialog?.dismiss()
                        }
                    }

                    alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    alertDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

                }
                2 -> {
                    //Delete Account

                    val  user = FirebaseAuth.getInstance().currentUser

                    val layoutInflater = LayoutInflater.from(this)
                    val view = layoutInflater.inflate(R.layout.item_confirm_email_password, null)
                    var alertDialog: AlertDialog? = null
                    val alertDialogBuilder = AlertDialog.Builder(this, R.style.AlertDialog_AppCompat)
                    alertDialogBuilder.setView(view)

                    val close = view.findViewById<ImageButton>(R.id.close)
                    val confirm = view.findViewById<Button>(R.id.confirm)
                    val email = view.findViewById<EditText>(R.id.confirm_email)
                    val password = view.findViewById<EditText>(R.id.confirm_password)

                    close.setOnClickListener {
                        alertDialog?.dismiss()
                    }

                    confirm.setOnClickListener {

                        if((email.text != null && email.text.isNotEmpty())  && (password.text != null && password.text.isNotEmpty())){
                            val credential = EmailAuthProvider
                                    .getCredential(email.text.toString(), password.text.toString())

                            user!!.reauthenticate(credential)
                                    .addOnCompleteListener {
                                        if(it.isSuccessful) {
                                            mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).removeValue().addOnCompleteListener {
                                                if (it.isSuccessful) {
                                                    user.delete().addOnCompleteListener { isDeleted ->
                                                        if (isDeleted.isSuccessful) {
                                                            Toast.makeText(this@SettingsPage, "User Deleted.", Toast.LENGTH_LONG).show()

                                                        }else{
                                                            Toast.makeText(this@SettingsPage, "User NOT Deleted from DB.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            }
                                            alertDialog!!.dismiss()
                                            val intent = Intent(this@SettingsPage, MainActivity::class.java)
                                            startActivity(intent)
                                        }else{
                                            Toast.makeText(this@SettingsPage, "Wrong information, please try again.", Toast.LENGTH_LONG).show()
                                            email.text.clear()
                                            password.text.clear()
                                        }
                                    }
                        }else{
                            Toast.makeText(this@SettingsPage, "Values cannot be empty, try again.", Toast.LENGTH_LONG).show()
                        }
                    }

                    alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    alertDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageHelper.putImage(requestCode,resultCode,data,this)
    }

    class ListItemAdapter(private val context: Context,
                          private val dataSource: ArrayList<SettingsItems>) : BaseAdapter() {
        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //1
        override fun getCount(): Int {
            return dataSource.size
        }

        //2
        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        //3
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        //4
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // Get view for row item
            val rowView = inflater.inflate(R.layout.item_profile, parent, false)

            // Get title element
            val titleTextView = rowView.findViewById(R.id.list_item_name) as TextView

            val listItem = getItem(position) as SettingsItems
            titleTextView.text = listItem.title

            return rowView
        }
    }

    data class SettingsItems(var title: String)
}