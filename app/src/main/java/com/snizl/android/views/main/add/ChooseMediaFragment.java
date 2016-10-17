package com.snizl.android.views.main.add;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.views.base.BaseFragment;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseMediaFragment extends BaseFragment {
    private final int PICK_MEDIA_REQUEST_CODE    = 0;
    private final int CAPTURE_IMAGE_REQUEST_CODE = 1;
    private final int RECORD_VIDEO_REQUEST_CODE  = 2;

    @BindView(R.id.iv_photo) ImageView ivPhoto;
    @BindView(R.id.vv_video) VideoView vvVideo;
    @BindView(R.id.iv_play)  ImageView ivPlay;

    @OnClick(R.id.iv_play)
    public void onPlay() {
        ivPhoto.setVisibility(View.INVISIBLE);
        ivPlay.setVisibility(View.INVISIBLE);
        vvVideo.setVisibility(View.VISIBLE);

        vvVideo.requestFocus();
        vvVideo.start();
    }

    @OnClick(R.id.fab)
    public void chooseMedia() {
        final Dialog dialog = new BottomSheetDialog(mContext);
        dialog.setContentView(R.layout.dialog_choose_media);
        dialog.setCancelable(true);

        Button btnUpload  = (Button) dialog.findViewById(R.id.btn_upload);
        Button btnCapture = (Button) dialog.findViewById(R.id.btn_capture);
        Button btnRecord  = (Button) dialog.findViewById(R.id.btn_record);
        Button btnSelect  = (Button) dialog.findViewById(R.id.btn_select);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/*");
                startActivityForResult(intent, PICK_MEDIA_REQUEST_CODE);
                dialog.dismiss();
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission(PERMISSIONS_REQUEST_CAMERA_FOR_IMAGE);
                dialog.dismiss();

            }
        });

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission(PERMISSIONS_REQUEST_CAMERA_FOR_VIDEO);
                dialog.dismiss();

            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_media, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    private void initData() {
        mContext = getContext();
    }

    private void initUI() {
        Uri uri = AppController.selected_media_uri;
        if (uri != null) {
            if (uri.toString().contains("images")) {
                displayPhoto(uri);
            } else if (uri.toString().contains("video")) {
                displayVideo(uri);
            }
        }
    }

    private void displayPhoto(Uri uri) {
        Log.d(TAG, uri.toString());

        ivPhoto.setVisibility(View.VISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);
        ivPlay.setVisibility(View.INVISIBLE);

        Glide.with(mContext)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipMemoryCache(true)
                .crossFade()
                .centerCrop()
                .into(ivPhoto);
    }

    private void displayVideo(Uri uri) {
        Log.d(TAG, uri.toString());

        ivPhoto.setVisibility(View.VISIBLE);
        vvVideo.setVisibility(View.INVISIBLE);
        ivPlay.setVisibility(View.VISIBLE);

        Glide.with(mContext)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .crossFade()
                .centerCrop()
                .into(ivPhoto);

        vvVideo.setVideoURI(uri);
        vvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                ivPhoto.setVisibility(View.VISIBLE);
                vvVideo.setVisibility(View.INVISIBLE);
                ivPlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkCameraPermission(int request_code) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, request_code );
        } else {
            if (request_code == PERMISSIONS_REQUEST_CAMERA_FOR_IMAGE) {
                openImageCamera();
            } else if (request_code == PERMISSIONS_REQUEST_CAMERA_FOR_VIDEO) {
                openVideoCamera();
            }
        }
    }

    private void openImageCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void openVideoCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, RECORD_VIDEO_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_MEDIA_REQUEST_CODE) {
                Uri uri = data.getData();
                AppController.selected_media_uri = uri;

                if (uri.toString().contains("images")) {
                    displayPhoto(uri);
                } else  if (uri.toString().contains("video")) {
                    displayVideo(uri);
                }
            }

            if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Uri uri = data.getData();
                AppController.selected_media_uri = uri;
                displayPhoto(uri);
            }

            if (requestCode == RECORD_VIDEO_REQUEST_CODE) {
                Uri uri = data.getData();
                AppController.selected_media_uri = uri;
                displayVideo(uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA_FOR_IMAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageCamera();
                } else {
                    commonUtils.showAlertDialog(mContext, "You must allow this permission to capture a image.");
                }
                break;
            case PERMISSIONS_REQUEST_CAMERA_FOR_VIDEO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openVideoCamera();
                } else {
                    commonUtils.showAlertDialog(mContext, "You must allow this permission to record a video.");
                }
                break;
        }
    }
}