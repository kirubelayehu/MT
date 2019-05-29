package com.asana.mtimageuplading.Presenter;

import android.app.Application;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.asana.mtimageuplading.Model.ImageModel;
import com.asana.mtimageuplading.View.IImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class ImagePresenter implements IImagePresenter{

    private final IImageView imageView;
    Application application;
    ImageModel imageModel;
    String path;
    public ImagePresenter(IImageView imageView) {
        this.imageView = imageView;
        this.application=application;
        imageModel=new ImageModel();
    }

    @Override
    public void cameraClick() {
        if (!imageView.checkPermission()) {
            imageView.showPermissionDialog();
            return;
        }
        imageView.startCamera();
    }

    @Override
    public void ChooseGalleryClick() {
        if (!imageView.checkPermission()) {
            imageView.showPermissionDialog();
            return;
        }

        imageView.chooseGallery();
    }

    @Override
    public void permissionDenied() {
        imageView.showPermissionDialog();

    }

    @Override
    public void setfilepath(String path) {
        Log.e("Path","In Presenter"+path);
        imageModel.setImagepath(path);
        this.path=path;
        ImageModel imageModel=new ImageModel();
        imageModel.setImagepath(path);
        Log.e("Path","In MOdel prese"+imageModel.getImagepath());
    }

    @Override
    public String getfilepath() {
        ImageModel imageModel=new ImageModel();
        return imageModel.getImagepath();
    }

    @Override
    public void uploadImage(ArrayList<Uri> uri, StorageReference storageReference) {
        StorageReference reference=storageReference.child("images");
        uri.size();
        Log.e("Presenter Side",String.valueOf(uri.size()));
        for (int upload_count=0;upload_count<uri.size();upload_count++){
            Uri individualuri=uri.get(upload_count);
            StorageReference ref=reference.child(individualuri.getLastPathSegment());
            ref.putFile(individualuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            progressDialog.dismiss();
//                            Toast.makeText(MainActivity.this, "Uploaded..", Toast.LENGTH_SHORT).show();
                            imageView.hideUploadingProgress();
                            Log.e("OnSuccessListener","Uploaded ="+individualuri);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(MainActivity.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("OnFailureListener","Failed");
                            imageView.hideUploadingProgress();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            String message="Uploaded  "+(int) progress+" % ";
                            imageView.showUploadingProgress(message);
                            Log.e("OnProgressListener","Pregressing...." +individualuri);
                        }
                    });
        }

    }
}
