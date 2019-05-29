package com.asana.mtimageuplading;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.asana.mtimageuplading.Model.ImageModel;
import com.asana.mtimageuplading.Presenter.ImagePresenter;
import com.asana.mtimageuplading.View.IImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IImageView {

    private List<ImageModel> imagelist = new ArrayList<>();


    static final int REQUEST_TAKE_PHOTO = 101;
    static final int REQUEST_GALLERY_PHOTO = 102;
    static String[] permissions = new String[]{
    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImagePresenter mPresenter;
    Uri photoURI;
    String filename;
    private ArrayList<Uri> fileUris=new ArrayList<Uri>();
    private int REQUEST_CHOOSER = 2;
   @BindView(R.id.fromCamera)Button fromcamera;
   @BindView(R.id.fromGallery)Button fromGallery;
   @BindView(R.id.uploadbtn)Button uploadbtn;
    ProgressDialog progressDialog;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageupload);
        mPresenter=new ImagePresenter(this);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        setUpFirebase();
        fromcamera.setOnClickListener(v -> mPresenter.cameraClick());
        fromGallery.setOnClickListener(v -> mPresenter.ChooseGalleryClick());
        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoURI!=null){
                    progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Uploading....");
                    progressDialog.show();
                    mPresenter.uploadImage(fileUris, storageReference);

                }
            }
        });

    }
    public void setUpFirebase(){
        firebaseStorage= FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
    }
    @Override
    public boolean checkPermission() {
        for (String mPermission : permissions) {
            int result = ActivityCompat.checkSelfPermission(this, mPermission);
            if (result == PackageManager.PERMISSION_DENIED) return false;
        }
        return true;
    }
    @Override
    public void showPermissionDialog() {
        Dexter.withActivity(this).withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();

                    }


                }).withErrorListener(error -> showErrorDialog())
                .onSameThread()
                .check();
    }
    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_need_permission));
        builder.setMessage(getString(R.string.message_grant_permission));
        builder.setPositiveButton(getString(R.string.label_setting), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }
    @Override
    public File getFilePath() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }
    @Override
    public void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
    @Override
    public void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
    }
    @Override
    public void chooseGallery() {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Please Select File"),
                        REQUEST_GALLERY_PHOTO);
            } catch (Exception e) {
                Log.e("chooseGallery Exception",String.valueOf(e.getMessage()));
                }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Uri> arrayList=new ArrayList<>();
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                if (data.hasExtra("data")) {
                    Bitmap photo = Bitmap.createScaledBitmap((Bitmap) data.getExtras().get("data"), 992, 992, false);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                                photo, UUID.randomUUID().toString(), null);
                        photoURI = Uri.parse(path);
                        fileUris.add(photoURI);
                        Log.e("PhotoUri",String.valueOf(photoURI));
                        //fileUris.add(photoURI);
                        Log.e("File size",String.valueOf(fileUris.size()));
//                        filename=getFileName(photoURI);
//                        mPresenter.setfilepath(filename);
                }

            }
            else if (requestCode == REQUEST_GALLERY_PHOTO) {
                fileUris = new ArrayList<>();
                if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY_PHOTO && data != null) {
                    ClipData clipData = data.getClipData();
                    if (clipData != null) {
                        if (clipData.getItemCount() > 1) { // for multiple image
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri uri = item.getUri();
                                photoURI = uri;
                                fileUris.add(uri);
                                String filename = getFileName(uri);
                                mPresenter.setfilepath(filename);
                                Log.e("Multiple", String.valueOf(photoURI + " " + fileUris.size()));
                            }
                        }
                    } else { // for single image
                        Uri singleuri = data.getData();
                        photoURI = singleuri;
                        fileUris.add(photoURI);
                        Log.e("Single size", String.valueOf(photoURI + " " + fileUris.size()));
                        String filename = getFileName(photoURI);
                        mPresenter.setfilepath(filename);
                    }
                } else if (resultCode == RESULT_OK && requestCode == REQUEST_CHOOSER && data != null) {
                    final Uri uri = data.getData();

                    if (uri != null) {

                        photoURI = uri;
                        Log.e("Single", String.valueOf(photoURI));
                    }
                }
            }

        }
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void showErrorDialog() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();

    }

    public String getRealPathFromUri(Uri contentUri) {
        String result = null;
        if (contentUri.getScheme().equals("content")) {
            Cursor cursor = this.getContentResolver().query(contentUri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = contentUri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void showUploadingProgress(String message) {
        progressDialog.setMessage(message);
    }

    @Override
    public void hideUploadingProgress() {
        progressDialog.dismiss();
    }

}
