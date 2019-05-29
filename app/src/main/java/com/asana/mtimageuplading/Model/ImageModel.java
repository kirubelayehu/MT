package com.asana.mtimageuplading.Model;

import android.net.Uri;

public class ImageModel {
    private String imagepath;
    private Uri imageuri;
    public ImageModel() {
    }

    public ImageModel(String imagepath) {
        this.imagepath = imagepath;
    }

    public Uri getImageuri() {
        return imageuri;
    }

    public void setImageuri(Uri imageuri) {
        this.imageuri = imageuri;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }
}
