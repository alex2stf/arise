package com.arise.rapdroid.media.server.fragments;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.arise.core.tools.FileUtil;
import com.arise.weland.model.ContentHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;

	public CameraPreview(Context context) {
		super(context);
//		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}


	public static byte[] captureFrameToJpeg(byte[] data, Camera cam){
		//TODO error check
		Camera.Parameters localParams = cam.getParameters();
		int frameFormat = localParams.getPreviewFormat();
		if (frameFormat == ImageFormat.JPEG) {
			return data;
		}

		Camera.Size size = localParams.getPreviewSize();
		int frameWidth = size.width;
		int frameHeight = size.height;

		YuvImage image = new YuvImage(data, frameFormat, frameWidth, frameHeight, null);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		image.compressToJpeg(new Rect(0, 0, frameWidth, frameHeight), 100, stream);
		return stream.toByteArray();

	}



	private void setDisplayAndStartPreview(SurfaceHolder localHolder){
		try {
			// create the surface and start camera preview
			if (mCamera != null) {


				mCamera.setPreviewCallback(null);
				mCamera.setPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera cam) {
						byte[] jpeg = captureFrameToJpeg(data, cam);
						ContentHandler.getLiveMjpegStream().pushJPEGFrame(jpeg);
						ContentHandler.getLiveJpeg().offerJPEG(jpeg);
						if (_should_take_snap){
							FileUtil.writeBytesToFile(jpeg, new File(FileUtil.findAppDir(), "snapshot.jpeg"));
							_should_take_snap = false;
						}
					}
				});

//				mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//					@Override
//					public void onPreviewFrame(byte[] data, Camera cam) {
//
//						if (!previewRunning){
//							return;
//						}
//						Camera.Parameters localParams = cam.getParameters();
//						int frameFormat = localParams.getPreviewFormat();
//
//
//						if (frameFormat == ImageFormat.JPEG) {
//							ContentHandler.getLiveMjpegStream().pushJPEGFrame(data);
//						} else {
//							Camera.Size size = localParams.getPreviewSize();
//							int frameWidth = size.width;
//							int frameHeight = size.height;
//							Rect rectangle = new Rect(0, 0, frameWidth, frameHeight);
//
//							YuvImage image = new YuvImage(data, frameFormat, frameWidth, frameHeight, null);
//							ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//							image.compressToJpeg(rectangle, 20, stream);
//							byte[] imgBytes = stream.toByteArray();
//							ContentHandler.getLiveMjpegStream().pushJPEGFrame(imgBytes);
//						}
//					}
//				});
				mCamera.setPreviewDisplay(null);
				mCamera.setPreviewDisplay(localHolder);
				mCamera.startPreview();
			}
		} catch (IOException e) {
			Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		setDisplayAndStartPreview(holder);
	}



	public void refreshCamera(Camera camera) {
		if (mHolder == null || mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}
		// onStop preview before making changes


		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to onStop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		// start preview with new settings
		setCamera(camera);
		setDisplayAndStartPreview(mHolder);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to onStop the preview before resizing or reformatting it.
		refreshCamera(mCamera);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
//		 mCamera.release();

	}

	public void removeCallback() {
		mHolder.removeCallback(this);
	}

	private volatile boolean _should_take_snap = false;


	public void takeSnapshot() {
		_should_take_snap = true;
	}
}