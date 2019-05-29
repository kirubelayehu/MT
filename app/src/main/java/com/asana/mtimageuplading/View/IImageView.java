package com.asana.mtimageuplading.View;

import android.net.Uri;

import java.io.File;

public interface IImageView {

        boolean checkPermission();

        void showPermissionDialog();

        File getFilePath();

        void openSettings();

        void startCamera();

        void chooseGallery();

        void showErrorDialog();
        String getRealPathFromUri(Uri contentUri);
        void showUploadingProgress(String message);
        void hideUploadingProgress();
    }

