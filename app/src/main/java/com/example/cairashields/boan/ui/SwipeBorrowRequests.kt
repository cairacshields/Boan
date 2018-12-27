package com.example.cairashields.boan.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import com.example.cairashields.boan.R
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import android.view.View
import android.view.ViewGroup
import com.example.cairashields.boan.Objects.BorrowRequest
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.*
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.bumptech.glide.Glide
import com.example.cairashields.boan.Helper.Strings
import com.example.cairashields.boan.Helper.Utils
import com.example.cairashields.boan.Objects.UserModel
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


class SwipeBorrowRequests : AppCompatActivity(), Serializable {

    @BindView(R.id.frame)
    lateinit var mFlingContainer: SwipeFlingAdapterView
    @BindView(R.id.frame_container)
    lateinit var mFrameContainer: FrameLayout
    @BindView(R.id.refresh_container)
    lateinit var mEmptyContainer: RelativeLayout
    @BindView(R.id.refresh_list)
    lateinit var mRefreshList: Button
    @BindView(R.id.bottom_nav) lateinit var mBottomNav: AHBottomNavigation

    var viewHolder: ViewHolder? = null

    var mDatabaseReference: DatabaseReference? = null
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    var progressDialog: ProgressDialog? = null
    var mStorage: FirebaseStorage? = null
    var mStorageRef: StorageReference? = null
    private lateinit var auth: FirebaseAuth


    var list: ArrayList<BorrowRequest> = ArrayList<BorrowRequest>()

    var adapter: BaseAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrow_swipe)

        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference.child("borrowRequests")
        mDatabaseReferenceUsers = database!!.reference.child("users")
        mStorage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()


        mFlingContainer = findViewById(R.id.frame)
        mFrameContainer = findViewById(R.id.frame_container)
        mEmptyContainer = findViewById(R.id.refresh_container)
        mRefreshList = findViewById(R.id.refresh_list)
        mBottomNav = findViewById(R.id.bottom_nav)
        val navItem1  =  AHBottomNavigationItem(R.string.my_account, R.drawable.ic_user, R.color.colorPrimary);
        val navItem2  =  AHBottomNavigationItem(R.string.borrow_requests, R.drawable.ic_borrow_requests_icon_1, R.color.colorAccent);
        val navItem3  =  AHBottomNavigationItem(R.string.settings, R.drawable.ic_settings, R.color.colorPrimaryDark);

        mEmptyContainer.visibility = View.GONE
        progressDialog = ProgressDialog(this)

        progressDialog!!.setMessage("Loading Data from Firebase Database")
        progressDialog!!.show()

        mBottomNav.addItem(navItem1)
        mBottomNav.addItem(navItem2)
        mBottomNav.addItem(navItem3)
        mBottomNav.defaultBackgroundColor = Utils.fetchColor(R.color.white, this)
        mBottomNav.accentColor = Utils.fetchColor(R.color.colorPrimary, this)
        mBottomNav.inactiveColor = Utils.fetchColor(R.color.grey, this)
        mBottomNav.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW;

        mBottomNav.currentItem = 1
        mBottomNav.setOnTabSelectedListener { position, wasSelected ->
            //Move to another activity/fragment based on click position
            var intent = Intent()
            when(position){
                0 ->{intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                }
                1 ->{}
                2 ->{
                }
                else -> {}
                }

            wasSelected
        }

        mFlingContainer.setFlingListener(object : SwipeFlingAdapterView.onFlingListener {
            override fun removeFirstObjectInAdapter() {

            }

            override fun onLeftCardExit(p0: Any?) {
                list.removeAt(0)
                adapter!!.notifyDataSetChanged();
            }

            override fun onRightCardExit(dataObject: Any?) {
                val borrowRequest = (list.get(dataObject as Int)) as Serializable
                val intent = Intent(this@SwipeBorrowRequests, TermsAgreement::class.java)
                intent.putExtra("borrow_request", borrowRequest)
                startActivity(intent)
                list.removeAt(0)
                adapter!!.notifyDataSetChanged();
            }

            override fun onAdapterAboutToEmpty(adapterCount: Int) {
                if (adapterCount == 0) {
                    mFrameContainer.visibility = View.GONE
                    mEmptyContainer.visibility = View.VISIBLE
                    mRefreshList.setOnClickListener {
                        mFrameContainer.visibility = View.VISIBLE
                        mEmptyContainer.visibility = View.GONE

                        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for (dataSnapshot in snapshot.children) {

                                    val borrowRequest = dataSnapshot.getValue<BorrowRequest>(BorrowRequest::class.java)

                                    list.add(borrowRequest!!)
                                }

                                adapter = RecyclerViewAdapter(this@SwipeBorrowRequests, list)
                                mFlingContainer.adapter = adapter!!
                                adapter!!.notifyDataSetChanged()


                                progressDialog!!.dismiss()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                                progressDialog!!.dismiss()

                            }
                        })
                    }
                }

            }

            override fun onScroll(scrollProgressPercent: Float) {
                val view: SwipeFlingAdapterView = mFlingContainer
                view.findViewById<View>(R.id.background).alpha = 0f
//                view.findViewById<View>(R.id.item_swipe_right_indicator).alpha = (if (scrollProgressPercent < 0) -scrollProgressPercent else 0F)
                //            view.findViewById<View>(R.id.item_swipe_left_indicator).alpha = (if (scrollProgressPercent > 0) scrollProgressPercent else 0F)
            }


        })

        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (dataSnapshot in snapshot.children) {

                    val borrowRequest = dataSnapshot.getValue<BorrowRequest>(BorrowRequest::class.java)

                    list.add(borrowRequest!!)
                }

                adapter = RecyclerViewAdapter(this@SwipeBorrowRequests, list)

                mFlingContainer.adapter = adapter!!
                adapter!!.notifyDataSetChanged()


                progressDialog!!.dismiss()
            }

            override fun onCancelled(databaseError: DatabaseError) {

                progressDialog!!.dismiss()

            }
        })

    }

    class ViewHolder {
        var background: FrameLayout? = null
        var username: TextView? = null
        var profilePic: CircleImageView? = null
        var borrowReason: TextView? = null
        var borrowRepayDate: TextView? = null
        var borrowAmount: TextView? = null


    }

    inner class RecyclerViewAdapter(internal var context: Context, internal var BorrowRequest: List<BorrowRequest>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var rowView = convertView


            if (rowView == null) {

                val inflater = layoutInflater
                rowView = inflater.inflate(R.layout.item_borrow_request, parent, false)
                // configure view holder
                viewHolder = ViewHolder()
                viewHolder?.let { viewHolder ->
                    viewHolder.username = rowView.findViewById(R.id.username)
                    viewHolder.background = rowView.findViewById(R.id.background)
                    viewHolder.profilePic = rowView.findViewById(R.id.profile_pic)
                    viewHolder.borrowReason = rowView.findViewById(R.id.borrow_reason)
                    viewHolder.borrowAmount = rowView.findViewById(R.id.borrow_amount)
                    viewHolder.borrowRepayDate = rowView.findViewById(R.id.borrow_repay_date)
                }
                rowView.tag = viewHolder

            } else {
                viewHolder = convertView!!.tag as ViewHolder
            }
            viewHolder?.let { viewHolder ->

                var imageLocation: String? = null
                mDatabaseReferenceUsers!!.child(BorrowRequest.get(position).userId).child("profileImage").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(data : DataSnapshot) {
                        imageLocation = data.value.toString()
                        mStorageRef = mStorage!!.reference.child(imageLocation!!)
                        mStorageRef!!.downloadUrl.addOnSuccessListener {
                            Log.v("GOT THE URI", "SUCCESS")
                            Picasso.get().load(it).placeholder(R.drawable.ic_user).fit().into(viewHolder.profilePic)
                        }.addOnFailureListener {
                            // Handle any errors
                            it.printStackTrace()
                        }

                    }

                })

                viewHolder.username!!.text = BorrowRequest.get(position).username + ""
                viewHolder.borrowAmount!!.text ="$" + BorrowRequest.get(position).borrowAmount.toString()
                viewHolder.borrowReason!!.text = BorrowRequest.get(position).borrowReason
                val myFormat = "MM/dd/yy" //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                val date = sdf.format(BorrowRequest.get(position).repayDate)

                viewHolder.borrowRepayDate!!.text = "Repay date: $date"

            }



            return rowView!!
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return BorrowRequest.size
        }


    }
}