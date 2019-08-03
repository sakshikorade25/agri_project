package com.sakshikorade25.agri;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    FirebaseFirestore db;
    EditText type, age, price;
    Button upload;
    boolean flag=false;

    Button add_img;
    Button save_img;
    ProgressBar img_progressBar;
    ImageView img_view;
    EditText filename;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;

    private StorageReference mStorageRef;
    //private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        type = (EditText) findViewById(R.id.editText2);
        age = (EditText) findViewById(R.id.editText3);
        price = (EditText) findViewById(R.id.editText4);
        upload = (Button) findViewById(R.id.button6);

        add_img = (Button) findViewById(R.id.button2);
        save_img = (Button) findViewById(R.id.button5);
        img_progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        img_view = (ImageView) findViewById(R.id.imageView2);
        filename = (EditText) findViewById(R.id.editText);


        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(mAuth.getCurrentUser().getUid());
       // mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                check_empty_fields();
            }
        });


        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openfilechooser();
            }
        });

        save_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });
    }

    public void check_empty_fields(){

        flag = false;
        if(TextUtils.isEmpty((type.getText().toString())))
        {
            flag = true;
            type.setError("Type required!");
        }
        if(TextUtils.isEmpty((age.getText().toString())))
        {
            flag = true;
            age.setError("Age required!");
        }
        if(TextUtils.isEmpty((price.getText().toString())))
        {
            flag = true;
            price.setError("Price required!");
        }

        if(flag==false)
        {
            upload_add();
        }

    }

    public void upload_add(){
        // Create a new user with a first and last name
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getText().toString());
        data.put("age", Integer.parseInt(age.getText().toString()));
        data.put("price", Integer.parseInt(price.getText().toString()));

        DocumentReference documentRef;
        documentRef = db.collection("upload").document(mAuth.getCurrentUser().getUid());
        documentRef.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("", "DocumentSnapshot added with ID: " + mAuth.getCurrentUser().getUid());
                Toast.makeText(MainActivity.this,"Your post is uploaded successfully!",Toast.LENGTH_LONG).show();
                type.setText("");
                age.setText("");
                price.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("", "Error adding document", e);
                Toast.makeText(MainActivity.this,"Uploading failed!",Toast.LENGTH_LONG).show();
            }
        });

        /*// Add a new document with a generated ID
        db.collection("upload").add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(MainActivity.this,"Your post is uploaded successfully!",Toast.LENGTH_LONG).show();
                        type.setText("");
                        age.setText("");
                        price.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("", "Error adding document", e);
                        Toast.makeText(MainActivity.this,"Uploading failed!",Toast.LENGTH_LONG).show();
                    }
                });
    */
    }

    private void openfilechooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            img_view.setImageURI(mImageUri);
           // Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {


            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    img_progressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            Upload uploadfile = new Upload(filename.getText().toString().trim(),
                                    taskSnapshot.getDownloadUrl().toString());
                           // String uploadId = mDatabaseRef.push().getKey();
                           // mDatabaseRef.child(uploadId).setValue(uploadfile);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            img_progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
