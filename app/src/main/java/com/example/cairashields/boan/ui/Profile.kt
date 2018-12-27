package com.example.cairashields.boan.ui

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.cairashields.boan.Helper.Utils
import com.example.cairashields.boan.Helper.imageHelper
import com.example.cairashields.boan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {
    @BindView(R.id.image)lateinit var mProfileImage: CircleImageView
    @BindView(R.id.username)lateinit var mUsername: TextView
    @BindView(R.id.ratingBar)lateinit var mRatingBar: RatingBar
    @BindView(R.id.listView)lateinit var mListView: ListView
    @BindView(R.id.bottom_nav) lateinit var mBottomNav: AHBottomNavigation

    var mDatabaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    var mStorage: FirebaseStorage? = null
    var mStorageRef: StorageReference? = null
    private lateinit var auth: FirebaseAuth

    private var mArrayList = arrayListOf<ProfileListItems>()
    private lateinit var mAdapter: ListItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference.child("users")
        mStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        mProfileImage = findViewById(R.id.image)
        mUsername = findViewById(R.id.username)
        mRatingBar = findViewById(R.id.ratingBar)
        mListView = findViewById(R.id.listView)
        mBottomNav = findViewById(R.id.bottom_nav)

        //Set user name
        auth.currentUser?.let{ user ->
            mUsername.text = user.displayName
        }

        //Set profile image if there is one
        mDatabaseReference!!.child(auth.currentUser!!.uid).child("profileImage").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                if(data.value != null){
                    mStorageRef = mStorage!!.reference.child(data.value.toString())
                    mStorageRef!!.downloadUrl.addOnSuccessListener {
                        Log.v("GOT THE URI", "SUCCESS")
                        // Got the download URL for 'users/me/profile.png'
                        Picasso.get().load(it).fit().into(mProfileImage)
                    }.addOnFailureListener {
                        // Handle any errors
                        it.printStackTrace()
                    }
                }
            }

        })
        val navItem1  =  AHBottomNavigationItem(R.string.my_account, R.drawable.ic_user, R.color.colorPrimary);
        val navItem2  =  AHBottomNavigationItem(R.string.borrow_requests, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent);
        val navItem3  =  AHBottomNavigationItem(R.string.settings, R.drawable.ic_settings, R.color.colorPrimaryDark);


        mBottomNav.addItem(navItem1)
        mBottomNav.addItem(navItem2)
        mBottomNav.addItem(navItem3)
        mBottomNav.defaultBackgroundColor = Utils.fetchColor(R.color.white, this)
        mBottomNav.accentColor = Utils.fetchColor(R.color.colorPrimary, this)
        mBottomNav.inactiveColor = Utils.fetchColor(R.color.grey, this)
        mBottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW;

        mBottomNav.currentItem = 0
        mBottomNav.setOnTabSelectedListener { position, wasSelected ->
            //Move to another activity/fragment based on click position
            var intent = Intent()
            when(position){
                0 ->{}
                1 ->{
                    intent = Intent(this, SwipeBorrowRequests::class.java)
                    startActivity(intent)}
                2 ->{}
                else -> {}
            }

            wasSelected
        }

        //TODO ADD a check to determine if the user is a Lender or Borrower... then change list items accordingly

        mArrayList.add(ProfileListItems(R.drawable.ic_notifications, "Notifications"))
        // Will need to be 'Terms Agreements' if the user is a Lender
        mArrayList.add(ProfileListItems(R.drawable.ic_borrow_requests, "Borrow Requests"))
        mAdapter = ListItemAdapter(this, mArrayList)
        mListView.adapter = mAdapter

        mListView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = mListView.getItemAtPosition(position) as ProfileListItems

            when(position){
                0 -> {
                    val intent =Intent(this, ProfileNotifications::class.java)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("icon", selectedItem.icon)
                    startActivity(intent)
                }
                1-> {
                    val intent =Intent(this, ProfileBorrowRequests::class.java)
                    intent.putExtra("title", selectedItem.title)
                    intent.putExtra("icon", selectedItem.icon)
                    startActivity(intent)
                }
            }
            }

        mProfileImage.setOnClickListener {
            imageHelper.getImage(this)
        }
        //Don't allow the user to alter the rating by returning true
        mRatingBar.setOnTouchListener { v, event ->
             true
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        imageHelper.putImage(requestCode,resultCode,data,this@Profile)
        val oldImageId = imageHelper.putImage(requestCode,resultCode,data,this@Profile)
        var imageLocation: String? = null
        mDatabaseReference!!.child(auth.currentUser!!.uid).child("profileImage").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data : DataSnapshot) {
                imageLocation = data.value.toString()
                mStorageRef = mStorage!!.reference.child(imageLocation!!)
                mStorageRef!!.downloadUrl.addOnSuccessListener {
                    Log.v("GOT THE URI", "SUCCESS")
                    Picasso.get().load(it).fit().into(mProfileImage)
                }.addOnFailureListener {
                    // Handle any errors
                    it.printStackTrace()
                }

                //TODO NEED TO FIND A WAY TO DELETE THE OLD PROFILE PICTURE FROM STORAGE IF A NEW ONE IS SELECTED...
//                mStorage!!.reference.child(oldImageId).delete().addOnCompleteListener {
//                    if(it.isSuccessful){
//                        Toast.makeText(this@Profile, "Old picture deleted", Toast.LENGTH_LONG).show()
//                    }else{
//                        it.exception!!.printStackTrace()
//                    }
//                }
            }

        })
    }


    class ListItemAdapter(private val context: Context,
                          private val dataSource: ArrayList<ProfileListItems>) :BaseAdapter() {
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
            // Get thumbnail element
            val thumbnailImageView = rowView.findViewById(R.id.list_item_icon) as ImageView

            val listItem = getItem(position) as ProfileListItems
            titleTextView.text = listItem.title

            Glide.with(context)
                    .load(listItem.icon)
                    .apply(RequestOptions().override(50,50))
                    .into(thumbnailImageView)


            return rowView
        }
    }
    data class ProfileListItems(var icon: Int, var title: String)
}