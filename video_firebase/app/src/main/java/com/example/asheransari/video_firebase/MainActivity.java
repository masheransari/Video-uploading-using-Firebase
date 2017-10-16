package com.example.asheransari.video_firebase;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    Button getbtn, uploadBtn;
    TextView urlView, statusView;
    Uri filePath;
    String fileRealName;
    private StorageReference storageReference;
    FirebaseAuth firebaseAuth;
//    private DatabaseReference databaseReference;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        getbtn = (Button) findViewById(R.id.getBtn);
        uploadBtn = (Button) findViewById(R.id.btn_upload);
        urlView = (TextView) findViewById(R.id.imagePath);
        statusView = (TextView) findViewById(R.id.statusView);
        storageReference = FirebaseStorage.getInstance().getReference().child(firebaseAuth.getCurrentUser().getEmail().replace("@","_"));
        getbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                askPermission();
                if (checkPermission()) {
//                    Intent intent = new Intent();
//                    intent.setType("video/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(Intent.createChooser(intent, "Choose Video"), REQUEST_TAKE_GALLERY_VIDEO);
                } else {
                    askPermission();
                }
            }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
    }
    DecimalFormat df = new DecimalFormat("#");
    private void uploadFile() {
        if (filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading..!!");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            StorageReference mstorageReference = storageReference.child(fileRealName);
            mstorageReference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Video uploaded successfully..!!", Toast.LENGTH_SHORT).show();
                    statusView.setText(downloadUri.getPath());

                }
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                            String data = "%2",progess;
                            progressDialog.setTitle(df.format(progress)+ " % done");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.getMessage();
                            Toast.makeText(MainActivity.this, "Not Uploaded..!!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }

    private boolean checkPermission() {
        Log.e("Camera_act", "CheckPermission()");

        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    private static final int REQUEST_PERMISSIONS_STORAGE = 111;

    private void askPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS_STORAGE
        );
    }

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, REQUEST_TAKE_GALLERY_VIDEO);

                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
//                filePath = data.getData();
//                String selectedVideoPath = getPath(this,data.getData());
//                if (selectedVideoPath == null) {
//                    Toast.makeText(this, "No image select", Toast.LENGTH_SHORT).show();
//                } else {
//
//                    urlView.setText(selectedVideoPath);
//                }
                String realPath = null;
                fileRealName = data.getData().getPath().replace(":","_").replace("/","__");
                // SDK < API11
                if (Build.VERSION.SDK_INT < 11)
                    realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());

                    // SDK >= 11 && SDK < 19
                else if (Build.VERSION.SDK_INT < 19)
                    realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());

                    // SDK > 19 (Android 4.4)
                else
                    realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());


                setTextViews(Build.VERSION.SDK_INT, data.getData().getPath(),realPath,urlView);

            }
        }

//        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK
//                && null != data) {
//
//            filePath = data.getData();
//            Uri selectedImage = data.getData();
//            String[] filePathColumn = { MediaStore.Video.Media.DATA };
//
//            // Get the cursor
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            // Move to first row
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String videoPath = cursor.getString(columnIndex);
//            cursor.close();
//            if (videoPath != null) {
//                urlView.setText(videoPath);
//            } else {
//                Toast.makeText(this, "no video selected..!!", Toast.LENGTH_SHORT).show();
//            }
//
//        }
    }
    public String getPath(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


private void setTextViews(int sdk, String uriPath,String realPath, TextView textView){

    textView.setText("Build.VERSION.SDK_INT: "+sdk);
    textView.append("\nURI Path: "+uriPath);
    textView.append("\nReal Path: "+realPath);

    Uri uriFromPath = Uri.fromFile(new File(realPath));
    filePath = uriFromPath;

    // you have two ways to display selected image

    // ( 1 ) imageView.setImageURI(uriFromPath);

    // ( 2 ) imageView.setImageBitmap(bitmap);
//    Bitmap bitmap = null;
//    try {
//        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
//    } catch (FileNotFoundException e) {
//        e.printStackTrace();
//    }
//    imageView.setImageBitmap(bitmap);

    Log.d("HMKCODE", "Build.VERSION.SDK_INT:"+sdk);
    Log.d("HMKCODE", "URI Path:"+uriPath);
    Log.d("HMKCODE", "Real Path: "+realPath);
}
}
