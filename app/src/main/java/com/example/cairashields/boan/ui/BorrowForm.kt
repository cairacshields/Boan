package com.example.cairashields.boan.ui

import android.app.Activity
import android.os.Bundle
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.R
import java.util.*
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import android.app.Dialog
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import com.example.cairashields.boan.Objects.BorrowRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.squareup.picasso.Picasso


class BorrowForm : Activity(), NumberPicker.OnValueChangeListener {
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {

    }

    //region views
    @BindView(R.id.borrow_repay_date)lateinit var mPayDate: EditText
    @BindView(R.id.borrow_amount)lateinit var mBorrowAmount: Button
    @BindView(R.id.amount)lateinit var mAmount: TextView
    @BindView(R.id.user_collateral_pic)lateinit var mCollateralImage: ImageView
    @BindView(R.id.collateral_description)lateinit var mCollateralDesctiption: EditText
    @BindView(R.id.borrow_reason)lateinit var mBorrowReason: EditText
    @BindView(R.id.submit_request)lateinit var mSubmitRequest: Button

    //endregion


    var myCalendar = Calendar.getInstance()
    var mDatabaseReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth
    var imageUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_borrow)
        ButterKnife.bind(this)

        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        mPayDate = findViewById(R.id.borrow_repay_date)
        mBorrowAmount = findViewById(R.id.borrow_amount)
        mAmount = findViewById(R.id.amount)
        mBorrowReason = findViewById(R.id.borrow_reason)
        mCollateralDesctiption = findViewById(R.id.collateral_description)
        mCollateralImage = findViewById(R.id.user_collateral_pic)
        mSubmitRequest = findViewById(R.id.submit_request)


        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateLabel()
        }


        mPayDate.setOnClickListener {  DatePickerDialog(this@BorrowForm, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show() }

        mBorrowAmount.setOnClickListener {
            showNumberPicker()
        }

        mCollateralImage.setOnClickListener {
            val pickIntent = Intent()
            pickIntent.type = "image/*"
            pickIntent.action = Intent.ACTION_GET_CONTENT

            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val pickTitle = "Select or take a new Picture" // Or get from strings.xml
            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
            chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    arrayOf(takePhotoIntent)
            )

            startActivityForResult(chooserIntent, SELECT_PICTURE)
        }

        mSubmitRequest.setOnClickListener{
            submitRequest()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == SELECT_PICTURE) {
            if (data == null) {
                //Display an error
                return
            }else {
                //val inputStream = this@BorrowForm.getContentResolver().openInputStream(data.data)
                imageUrl = data.data.toString()
                Picasso.get().load(data.data).into(mCollateralImage)
            }
        }
    }


    //Function to open number picker used to select amount to borrow in 10 dollar incriments
    fun showNumberPicker() {

        val d = Dialog(this@BorrowForm)
        d.setTitle("NumberPicker")
        d.setContentView(R.layout.dialog)
        val b1 = d.findViewById(R.id.button1) as Button
        val b2 = d.findViewById(R.id.button2) as Button
        val np = d.findViewById(R.id.numberPicker1) as NumberPicker
        var init = 10
        val stringValues: MutableList<String> = arrayListOf()
        while(init <= 300){
            stringValues.add("$$init")
            init += 10
        }

        np.minValue = 0
        np.maxValue = 30
        np.wrapSelectorWheel = false
        np.displayedValues = stringValues.toTypedArray()
        np.setOnValueChangedListener(this)

        b1.setOnClickListener{
            mAmount.text = stringValues[np.value];
            d.dismiss();
        }
        b2.setOnClickListener{
            d.dismiss();
        }
        d.show()
    }


   fun updateDateLabel(){
       val myFormat = "MM/dd/yy" //In which you need put here
       val sdf = SimpleDateFormat(myFormat, Locale.US)

       mPayDate.setText(sdf.format(myCalendar.time))
    }

    fun submitRequest(){
        var username = auth.currentUser!!.displayName
        var email = auth.currentUser!!.email
        var collateralPic = imageUrl
        var collateralDescription = mCollateralDesctiption.text.toString()
        var borrowAmount = mAmount.text.toString()
        var borrowReason = mBorrowReason.text.toString()

        val format = "MM/dd/yy" //In which you need put here
        val sdf = SimpleDateFormat(format, Locale.US)
        val date:Date = sdf.parse(mPayDate.text.toString())
        var repayDate: Date = date

        mDatabaseReference!!.child(BORROW_REQUEST).push().setValue(BorrowRequest(
                username,email,collateralPic,collateralDescription,borrowAmount,repayDate,borrowReason)).addOnCompleteListener { it ->

            if(it.isSuccessful){
                Toast.makeText(this@BorrowForm, "Borrow Request published!", Toast.LENGTH_LONG).show()
                Log.v("Borrow Request status: ", "Success")
                //Take to main profile?

            }else if(it.isCanceled){
                Log.v("Borrow Request status: ","Cancled")
            }else{
                Log.v("Borrow Request status: ", "Something went wrong")
            }
        }
    }

    companion object {
        private val BORROW_REQUEST = "borrowRequests"
        private val SELECT_PICTURE = 1
    }
}