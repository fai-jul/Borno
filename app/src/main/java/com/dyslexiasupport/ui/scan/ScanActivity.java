package com.dyslexiasupport.ui.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;

import com.dyslexiasupport.R;
import com.dyslexiasupport.databinding.ActivityScanBinding;
import com.dyslexiasupport.ui.common.BaseActivity;
import com.dyslexiasupport.ocr.OcrTextCleaner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * Scan a page: camera (or gallery) -> crop -> read text -> edit -> save to the library.
 * Three panels share one screen; ScanViewModel says which one is showing.
 */
public class ScanActivity extends BaseActivity {

    private ActivityScanBinding binding;
    private ScanViewModel vm;
    @Nullable private ProcessCameraProvider cameraProvider;
    @Nullable private ImageCapture imageCapture;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    showPermissionNeeded();
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    vm.loadFromUri(uri);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets(binding.getRoot());
        vm = new ViewModelProvider(this).get(ScanViewModel.class);

        wireButtons();
        wireEditFields();

        vm.getStep().observe(this, this::showStep);
        vm.getSource().observe(this, bmp -> binding.cropView.setBitmap(bmp));
        vm.getWorking().observe(this, busy ->
                binding.progressOverlay.setVisibility(busy ? View.VISIBLE : View.GONE));
        vm.getError().observe(this, res -> {
            if (res != null) {
                Toast.makeText(this, res, Toast.LENGTH_LONG).show();
                vm.clearError();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    private void wireButtons() {
        binding.btnClose.setOnClickListener(v -> handleBack());
        binding.btnCapture.setOnClickListener(v -> takePhoto());
        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        binding.btnAllowCamera.setOnClickListener(v ->
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA));
        binding.btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });

        binding.btnRetake.setOnClickListener(v -> vm.setStep(ScanViewModel.STEP_CAMERA));
        binding.btnReadText.setOnClickListener(v -> vm.runOcr(binding.cropView.getCroppedBitmap()));

        binding.btnAddPage.setOnClickListener(v -> vm.setStep(ScanViewModel.STEP_CAMERA));
        binding.btnSave.setOnClickListener(v -> {
            if (vm.save()) {
                Toast.makeText(this, R.string.scan_saved, Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, R.string.scan_body_empty, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Keeps the view model's copy of the title/text up to date while the user types. */
    private void wireEditFields() {
        binding.inputTitle.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                vm.editedTitle = s.toString();
            }
        });
        binding.inputBody.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                vm.editedBody = s.toString();
            }
        });
    }

    private void showStep(int step) {
        boolean camera = step == ScanViewModel.STEP_CAMERA;
        boolean crop = step == ScanViewModel.STEP_CROP;
        boolean edit = step == ScanViewModel.STEP_EDIT;

        binding.panelCamera.setVisibility(camera ? View.VISIBLE : View.GONE);
        binding.panelCrop.setVisibility(crop ? View.VISIBLE : View.GONE);
        binding.panelEdit.setVisibility(edit ? View.VISIBLE : View.GONE);
        binding.getRoot().setBackgroundColor(ContextCompat.getColor(this,
                edit ? R.color.ds_cream : R.color.ds_ink));

        // Dark panels need light status-bar icons; the cream edit screen needs dark ones.
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(edit);

        if (camera) {
            ensureCamera();
        } else if (cameraProvider != null) {
            cameraProvider.unbindAll(); // release the camera while we are not previewing
        }
        if (edit) {
            if (!binding.inputTitle.getText().toString().equals(vm.editedTitle)) {
                binding.inputTitle.setText(vm.editedTitle);
            }
            if (!binding.inputBody.getText().toString().equals(vm.editedBody)) {
                binding.inputBody.setText(OcrTextCleaner.highlightUncertainWords(
                        vm.editedBody, ContextCompat.getColor(this, R.color.ds_mango)));
            }
        }
    }

    // ---------------- camera ----------------

    private void ensureCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            binding.permissionView.setVisibility(View.GONE);
            startCamera();
        } else if (!vm.askedCameraPermission) {
            vm.askedCameraPermission = true;
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            showPermissionNeeded();
        }
    }

    private void showPermissionNeeded() {
        binding.permissionView.setVisibility(View.VISIBLE);
    }

    private void startCamera() {
        binding.permissionView.setVisibility(View.GONE);
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCamera();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, R.string.scan_error_camera, Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera() {
        if (cameraProvider == null) {
            return;
        }
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setJpegQuality(95)
                .build();
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA,
                    preview, imageCapture);
        } catch (Exception e) {
            Toast.makeText(this, R.string.scan_error_camera, Toast.LENGTH_LONG).show();
        }
    }

    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }
        binding.btnCapture.setEnabled(false);
        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        binding.btnCapture.setEnabled(true);
                        vm.loadFromCamera(image); // the view model closes the image
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        binding.btnCapture.setEnabled(true);
                        Toast.makeText(ScanActivity.this, R.string.scan_error_camera,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------------- back button ----------------

    private void handleBack() {
        Integer step = vm.getStep().getValue();
        if (step == null || step == ScanViewModel.STEP_CAMERA) {
            if (vm.hasText()) {
                vm.setStep(ScanViewModel.STEP_EDIT); // came here via "Add page": go back to the text
            } else {
                finish();
            }
        } else if (step == ScanViewModel.STEP_CROP) {
            vm.setStep(ScanViewModel.STEP_CAMERA);
        } else if (vm.hasText()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.scan_discard_title)
                    .setPositiveButton(R.string.scan_discard, (d, w) -> finish())
                    .setNegativeButton(R.string.scan_keep, null)
                    .show();
        } else {
            finish();
        }
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
