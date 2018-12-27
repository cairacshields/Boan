package com.example.cairashields.boan.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static butterknife.internal.Utils.arrayOf;

public class imageHelper {
        static FirebaseDatabase database = FirebaseDatabase.getInstance();
        static DatabaseReference mDatabaseReference = database.getReference().child("users");
        static FirebaseStorage mStorage  = FirebaseStorage.getInstance();
        static StorageReference mStorageRef = null;
        static FirebaseAuth auth = FirebaseAuth.getInstance();

        static String  imageUrl  = null;
        static int SELECT_PICTURE = 1;

        //Getting an image from the camera or gallery
        public static void getImage(Activity activity){
            Intent pickIntent = new Intent();
            pickIntent.setType("image/*");
            pickIntent.setAction(Intent.ACTION_GET_CONTENT);

            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            String pickTitle = "Select or take a new Picture"; // Or get from strings.xml
            Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
            chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    arrayOf(takePhotoIntent)
            );

            startActivityForResult(activity, chooserIntent, SELECT_PICTURE, null);
        }

        //Putting the image in Firebase Storage if
        public static String putImage(int requestCode, int resultCode, Intent data ,Context context){
            if (requestCode == SELECT_PICTURE) {
                if (data == null) {
                    //Display an error
                    return "";
                }else {

                    Uri selectedImageUri = data.getData();
                    String url = data.getData().toString();
                    if (url.startsWith("content://com.google.android.apps.photos.content") || url.startsWith("content://com.android.providers.media.documents") ) {

                        InputStream inputStream = null;
                        try {
                            inputStream = context.getContentResolver().openInputStream(selectedImageUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        imageUrl = UUID.randomUUID().toString();

                        mStorageRef = mStorage.getReference().child(imageUrl);
                        UploadTask uploadTask = mStorageRef.putStream(inputStream);

                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                // Handle unsuccessful uploads
                            }
                        }); {
                         uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                             @Override
                             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                 Log.v("IMAGE UPLOADED!", "Success");
                                 mDatabaseReference.child(auth.getCurrentUser().getUid()).child("profileImage").setValue(imageUrl);
                                 // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                                 // ...
                             }
                         });
                        }

                    }else {
                        //  startCrop(tempPath);

                    }

                }
            }
            return imageUrl;
        }

    }
