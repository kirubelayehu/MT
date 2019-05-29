package com.asana.mtimageuplading.Presenter;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public interface IImagePresenter {
    void cameraClick();

    void ChooseGalleryClick();

    void permissionDenied();

    void setfilepath(String path);
    String getfilepath();
    void uploadImage(ArrayList<Uri> uri, StorageReference storageReference);
}
