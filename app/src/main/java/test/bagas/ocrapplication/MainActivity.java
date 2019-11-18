package test.bagas.ocrapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private TextView textView;
    private TextView textKTP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        textKTP = findViewById(R.id.textPrimary);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(), Camera3Activity.class));
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                toCamera();
            }
        });

        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    scanMediaCapture();

                    startRecognation();
                }
                break;
        }
    }

    private void startRecognation() {
        FirebaseVisionImage image = null;
        try {
            File file = new File(mCurrentFilePath);
            image = FirebaseVisionImage.fromFilePath(this,
                    ProviderUtils.generateProviderFile(this, file));
        } catch (IOException e) {
            throw new RuntimeException("VisionImage failed to get file from ");
        }

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        final StringBuilder textBuilder = new StringBuilder();

//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setTitle("Recognizing...");
//        dialog.setCancelable(false);
////        dialog.setIndeterminate(true);
//        dialog.show();

        Task<FirebaseVisionText> result = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String stringResult = firebaseVisionText.getText();

                        textBuilder.append("Direct Result: \n");
                        textBuilder.append(stringResult);

                        textBuilder.append("\n");
                        textBuilder.append("\n");
                        textBuilder.append("\n");

                        int count = 0;
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {

                            String fuckyou = block.getText();
                            String fixedFackyou = fuckyou.replaceAll("[^\\d]", "");
                            if (isNumeric(fixedFackyou)) {
                                Log.i("OCR_TEST", "Result KTP: " + fixedFackyou);
                                textKTP.setText(fixedFackyou);
                                return;
                            }

                            textBuilder.append("Block Level ");
                            textBuilder.append(++count);
                            textBuilder.append("\n");

                            Float blockConfidence = block.getConfidence();
                            textBuilder.append("Confidence: ");
                            textBuilder.append(blockConfidence);
                            textBuilder.append("\n");

                            textBuilder.append("Text Block Value: \n");

                            String textBlock = block.getText();
                            textBuilder.append(textBlock);
                            textBuilder.append("\n");
                            textBuilder.append("\n");

                            int countLine = 0;
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                textBuilder.append(" Line ");
                                textBuilder.append(++countLine);
                                textBuilder.append(", in block ");
                                textBuilder.append(count);
                                textBuilder.append("\n");

                                textBuilder.append(" Line Value:\n");

                                String stringLine = line.getText();
                                textBuilder.append(" ");
                                textBuilder.append(stringLine);
                                textBuilder.append("\n");
                                textBuilder.append("\n");

                                int countElement = 0;
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    countElement++;
                                    textBuilder.append("  Element ");
                                    textBuilder.append(countElement);
                                    textBuilder.append(", in line ");
                                    textBuilder.append(countLine);
                                    textBuilder.append("\n");

                                    textBuilder.append("  Element Value: ");
                                    textBuilder.append(element.getText());
                                    textBuilder.append("\n");
                                    textBuilder.append("\n");
                                }

                                textBuilder.append("\n");
                            }

                            textBuilder.append("\n");
                        }

                        textView.setText(textBuilder.toString());

//                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showDialog(e.getMessage() + "\nIn line: "
                                + e.getStackTrace()[0].getFileName()
                                + e.getStackTrace()[0].getLineNumber());

//                        dialog.dismiss();
                    }
                });
    }

    private boolean isNumeric(String text) {
        String[] strings = new String[]{
                "0", "1", "2", "3", "4", "5",
                "6", "7", "8", "9"
        };

        for (String val : strings) {
            if (text.contains(val)) {
                return true;
            }
        }
        return false;
    }

    private void showDialog(String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(message);
        b.setPositiveButton("OK", null);
        b.show();
    }

    private void toCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (camera.resolveActivity(getPackageManager()) != null) {
            File fuckingFile = null;
            try {
                fuckingFile = createTempFile();

                Uri uri = ProviderUtils.generateProviderFile(getApplicationContext(), fuckingFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                startActivityForResult(camera, 1);
            }
        }
    }

    private String mCurrentFilePath;

    private File createTempFile() {
        String filename = "TEMP_" + System.currentTimeMillis() + ".jpg";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!dir.exists())
            dir.mkdir();
        File path = new File(dir, filename);

        mCurrentFilePath = path.getAbsolutePath();
        return path;
    }

    private void scanMediaCapture() {
        Intent scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentFilePath);
        scan.setData(Uri.fromFile(file));
        this.sendBroadcast(scan);
    }

}
