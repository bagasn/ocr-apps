package test.bagas.ocrapplication.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import test.bagas.ocrapplication.R;

public class Camera2Activity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private static final String TAG = "Camera2Activity";

    private View decoderView;

    private Thread mThread = null;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                decoderView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                Log.i(TAG, "run: Runnable is running ");
            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            }
        }
    };

    private TextureView textureView;

    private Size mImageDimension;
    private CameraDevice mCameraDevice = null;

    private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        decoderView = getWindow().getDecorView();
        setContentView(R.layout.activity_camera_2);

        textureView = findViewById(R.id.textureView);
        View capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFuckingPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    takeTheFuckingPicture();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(this);
        }

        if (decoderView.getSystemUiVisibility() == View.VISIBLE) {
            decoderView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

            mThread = new Thread(mRunnable);
            mThread.start();
            decoderView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    Log.i(TAG, "onSystemUiVisibilityChange: is Running");
                    if (visibility == View.VISIBLE) {
                        if (mThread != null) {
                            mThread.run();
                        }
                    }
                }
            });
        }

        if (checkFuckingPermission(Manifest.permission.CAMERA)) {
            openCamera();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_rigth);
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIds = manager.getCameraIdList();
            Log.i(TAG, "openCamera: camera ids - " + cameraIds.toString());

            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraIds[0]);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            mImageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if (checkFuckingPermission(Manifest.permission.CAMERA)) {
                manager.openCamera(cameraIds[0], mCameraCallback, null);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera: ", e);
        }
    }

    private void createCameraPreview() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        if (texture != null) {
            texture.setDefaultBufferSize(mImageDimension.getWidth(), mImageDimension.getHeight());
        }
    }

    private void takeTheFuckingPicture() {

    }

    private boolean checkFuckingPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_DENIED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
            return false;
        }
    }

}
