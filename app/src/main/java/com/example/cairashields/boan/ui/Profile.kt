package com.example.cairashields.boan.ui

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.graphics.BlurMaskFilter
import android.net.Uri
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
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import jp.wasabeef.picasso.transformations.BlurTransformation

class Profile : AppCompatActivity() {
    @BindView(R.id.big_image)lateinit var mBigImageView: ImageView
    @BindView(R.id.image)lateinit var mProfileImage: CircleImageView
    @BindView(R.id.username)lateinit var mUsername: TextView
    @BindView(R.id.ratingBar)lateinit var mRatingBar: RatingBar
    @BindView(R.id.listView)lateinit var mListView: ListView
    @BindView(R.id.bottom_nav) lateinit var mBottomNav: AHBottomNavigation
    @BindView(R.id.facebook)lateinit var mFacebook: ImageView
    @BindView(R.id.instagram)lateinit var mInstagram: ImageView

    var mDatabaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    var mStorage: FirebaseStorage? = null
    var mStorageRef: StorageReference? = null
    private lateinit var auth: FirebaseAuth
    var isLender: Boolean? = null
    private var mArrayList = arrayListOf<ProfileListItems>()
    private lateinit var mAdapter: ListItemAdapter
    private var user: Users? = null
    private var mFacebookLink: String? = null
    private var mInstagramLink: String? = null
    var navItem1 : AHBottomNavigationItem? = null
    var navItem2 : AHBottomNavigationItem? = null
    var navItem3: AHBottomNavigationItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference.child("users")
        mStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        mBigImageView = findViewById(R.id.big_image)
        mProfileImage = findViewById(R.id.image)
        mUsername = findViewById(R.id.username)
        mRatingBar = findViewById(R.id.ratingBar)
        mListView = findViewById(R.id.listView)
        mBottomNav = findViewById(R.id.bottom_nav)
        mFacebook = findViewById(R.id.facebook)
        mInstagram = findViewById(R.id.instagram)

        //Set user name
        auth.currentUser?.let{ user ->
            mUsername.text = user.displayName
        }

        Glide.with(this).load(R.drawable.profile_background)
                // .apply(RequestOptions.bitmapTransform(jp.wasabeef.glide.transformations.BlurTransformation(100)))
                .into(mBigImageView)

        isLender = intent.extras.getBoolean("isLender")

        mDatabaseReference!!.child(auth.currentUser!!.uid).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                user =  snapshot.getValue(Users::class.java)
                user?.let { user ->
                    isLender = user.isLender

                    user.profileImage?.let {
                        mStorageRef = mStorage!!.reference.child(user.profileImage)
                        mStorageRef!!.downloadUrl.addOnSuccessListener {
                            Log.v("GOT THE URI", "SUCCESS")
                            // Got the download URL for 'users/me/profile.png'
                            Picasso.get().load(it).fit().into(mProfileImage)
                        }.addOnFailureListener {
                            // Handle any errors
                            it.printStackTrace()
                        }
                    }

                    user.facebookLink?.let{
                        mFacebookLink = it
                    }

                    user.instagramLink?.let{
                        mInstagramLink = it
                    }

                }
            }
        })

        navItem1  =  AHBottomNavigationItem(R.string.my_account, R.drawable.ic_user, R.color.colorPrimary)
        if(isLender!!) {
            navItem2 = AHBottomNavigationItem(R.string.borrow_requests, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent)
        }else{
            navItem2 = AHBottomNavigationItem(R.string.borrow_form, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent)
        }
        navItem3  =  AHBottomNavigationItem(R.string.settings, R.drawable.ic_settings, R.color.colorPrimaryDark)

        mBottomNav.addItem(navItem1)
        mBottomNav.addItem(navItem2)
        mBottomNav.addItem(navItem3)


        mBottomNav.defaultBackgroundColor = Utils.fetchColor(R.color.white, applicationContext)
        mBottomNav.accentColor = Utils.fetchColor(R.color.mainOrange, applicationContext)
        mBottomNav.inactiveColor = Utils.fetchColor(R.color.grey, applicationContext)
        mBottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW;
        mBottomNav.setCurrentItem(0)

        mBottomNav.setOnTabSelectedListener { position, wasSelected ->
            //Move to another activity/fragment based on click position
            var intent = Intent()
            when(position){
                0 ->{}
                1 ->{
                    if(isLender!!) {
                        intent = Intent(this, SwipeBorrowRequests::class.java)
                        startActivity(intent)
                    }else{
                        intent = Intent(this, BorrowForm::class.java)
                        startActivity(intent)
                    }
                }
                2 ->{intent = Intent(this, SettingsPage::class.java)
                    intent.putExtra("isLender", isLender!!)
                    startActivity(intent)}
                else -> {}
            }

            wasSelected
        }

        //TODO ADD a check to determine if the user is a Lender or Borrower... then change list items accordingly

        if(isLender!!){
            mArrayList.add(ProfileListItems(R.drawable.ic_borrow_requests, "Pending Re-payments"))
            // Will need to be 'Terms Agreements' if the user is a Lender
            mArrayList.add(ProfileListItems(R.drawable.ic_term, "Terms Agreements"))
        }else{
            mArrayList.add(ProfileListItems(R.drawable.ic_borrow_requests, "View Active Borrow Request"))
            // Will need to be 'Terms Agreements' if the user is a Lender
            mArrayList.add(ProfileListItems(R.drawable.ic_notifications, "Terms Agreements"))
        }
        mAdapter = ListItemAdapter(this, mArrayList)
        mListView.adapter = mAdapter

        mListView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = mListView.getItemAtPosition(position) as ProfileListItems

            when(position){
                0 -> {
                    if(isLender!!) {
                        //Not sure what to show here for Lenders *thinking face*
                        val intent = Intent(this, ProfileNotifications::class.java)
                        intent.putExtra("title", selectedItem.title)
                        intent.putExtra("icon", selectedItem.icon)
                        startActivity(intent)
                    }else{
                        //Show the borrower any Terms Agreements that they have pending
                        val intent =Intent(this, ProfileActiveBorrowRequest::class.java)
                        intent.putExtra("title", selectedItem.title)
                        intent.putExtra("icon", selectedItem.icon)
                        startActivity(intent)
                    }
                }
                1-> {
                    if(isLender!!){
                        //Show the lender any Terms Agreements that they sent out
                        val intent =Intent(this, ProfileLenderTermsAgreements::class.java)
                        intent.putExtra("title", selectedItem.title)
                        intent.putExtra("icon", selectedItem.icon)
                        startActivity(intent)
                    }else{
                        //Allow the borrower to edit their borrow request
                        val intent =Intent(this, ProfileBorrowRequests::class.java)
                        intent.putExtra("title", selectedItem.title)
                        intent.putExtra("icon", selectedItem.icon)
                        startActivity(intent)
                    }
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


        mFacebook.setOnClickListener {
            if(mFacebookLink != null){
                //open facebook page
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mFacebookLink))
                startActivity(browserIntent)
            }
        }

        mInstagram.setOnClickListener {
            if(mInstagramLink != null){
                //Open instagram Link
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mInstagramLink))
                startActivity(browserIntent)
            }
        }

        bindUI()
    }

    fun bindUI(){

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