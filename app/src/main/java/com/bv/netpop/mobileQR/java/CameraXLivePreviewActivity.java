/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bv.netpop.mobileQR.java;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bv.netpop.mobileQR.java.barcodechecker.BarcodeChecker;
import com.bv.netpop.mobileQR.java.barcodechecker.BarcodeCheckerAdapter;
import com.bv.netpop.mobileQR.java.barcodes.BarcodeBase;
import com.bv.netpop.mobileQR.java.barcodes.POPQRBarcode;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.bv.netpop.mobileQR.CameraXViewModel;
import com.bv.netpop.mobileQR.GraphicOverlay;
import com.bv.netpop.mobileQR.R;
import com.bv.netpop.mobileQR.VisionImageProcessor;
import com.bv.netpop.mobileQR.java.barcodescanner.BarcodeScannerProcessor;
import com.bv.netpop.mobileQR.preference.PreferenceUtils;
import com.bv.netpop.mobileQR.preference.SettingsActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Live preview demo app for ML Kit APIs using CameraX. */
@KeepName
@RequiresApi(VERSION_CODES.O)
public final class CameraXLivePreviewActivity extends AppCompatActivity
    implements OnRequestPermissionsResultCallback{
  private static final String TAG = "CameraXLivePreview";
  private static final int PERMISSION_REQUESTS = 1;
  private static final String BARCODE_SCANNING = "Barcode Scanning";

  private final int prev_resWidth = 400;
  private final int prev_resHeight = 300;
  private final int anal_resWidth = 400;
  private final int anal_resHeight = 300;

  private static final String STATE_SELECTED_MODEL = "selected_model";

  private PreviewView previewView;
  private GraphicOverlay graphicOverlay;

  @Nullable private ProcessCameraProvider cameraProvider;
  @Nullable private Preview previewUseCase;
  @Nullable private ImageAnalysis analysisUseCase;
  @Nullable private VisionImageProcessor imageProcessor;
  private boolean needUpdateGraphicOverlayImageSourceInfo;

  private String selectedModel = BARCODE_SCANNING;
  private int lensFacing = CameraSelector.LENS_FACING_BACK;
  private CameraSelector cameraSelector;

  private ArrayList<POPQRBarcode> barcodes = new ArrayList<>(0);
  private BarcodeCheckerAdapter checkerAdapter;
  private Boolean activeFlag = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      Toast.makeText(
              getApplicationContext(),
              "CameraX is only supported on SDK version >=21. Current SDK version is "
                  + VERSION.SDK_INT,
              Toast.LENGTH_LONG)
          .show();
      return;
    }

    if (savedInstanceState != null) {
      selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, BARCODE_SCANNING);
      activeFlag = savedInstanceState.getBoolean("ActiveFlag");
    }

    ArrayList<BarcodeBase> bc_list = getIntent().getParcelableArrayListExtra("BarcodeArray");
    activeFlag = getIntent().getBooleanExtra("ActiveFlag", false);
    if (bc_list != null) {
      barcodes = new ArrayList<>(0);
      for (BarcodeBase bc : bc_list) {
        POPQRBarcode qrBC = new POPQRBarcode(bc.rawValue);
        qrBC.barcodeStatus = bc.barcodeStatus;
        qrBC.errorComment = bc.errorComment;
        qrBC.errorType = bc.errorType;
        barcodes.add(qrBC);
      }
    }

    cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

    setContentView(R.layout.activity_vision_camerax_live_preview);
    previewView = findViewById(R.id.preview_view);
    if (previewView == null) {
      Log.d(TAG, "previewView is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    RecyclerView rvBarcodeChecks = findViewById(R.id.barcode_check_menu)
            .findViewById(R.id.barcodeCheckerRecyclerView);
    checkerAdapter = new BarcodeCheckerAdapter(barcodes);
    rvBarcodeChecks.setAdapter(checkerAdapter);
    rvBarcodeChecks.setLayoutManager(new LinearLayoutManager(this));

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvBarcodeChecks.getContext(),
            DividerItemDecoration.VERTICAL);
    rvBarcodeChecks.addItemDecoration(dividerItemDecoration);

    new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
        .get(CameraXViewModel.class)
        .getProcessCameraProvider()
        .observe(
            this,
            provider -> {
              cameraProvider = provider;
              if (allPermissionsGranted()) {
                bindAllCameraUseCases();
              }
            });

    if (!allPermissionsGranted()) {
      getRuntimePermissions();
    }

    ImageView sendButton = findViewById(R.id.send_button);
    sendButton.setOnClickListener(
            v -> {
              AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
              dlgAlert.setMessage("POP要求印刷リストを売技ナビへ出力します。");
              dlgAlert.setTitle("POP要求印刷リスト出力");
              dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  String ToastText = "";
                  BarcodeChecker bcc = new BarcodeChecker();

                  ArrayList<POPQRBarcode> IncorrectBarcodes = barcodes.stream()
                          .filter(x -> {
                            final boolean b = x.barcodeStatus == BarcodeBase.status.Incorrect;
                            return b;
                          })
                          .collect(Collectors.toCollection(ArrayList::new));
                  if (IncorrectBarcodes.size() > 0) {
                    bcc.SendReprintItems(IncorrectBarcodes);
                    ToastText = "出力完了";
                  } else {
                    ToastText = "出力数「０」件";
                  }

                  Toast toast = Toast.makeText(getApplicationContext(), ToastText, Toast.LENGTH_LONG);
                  toast.show();
                }
              }).setCancelable(true);
              dlgAlert.create().show();
            }
    );

    ImageView resetButton = findViewById(R.id.reset_button);
    resetButton.setOnClickListener(
            v -> {
              AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle("削除")
                .setMessage("アイテム一覧を削除しますか。")
                .setPositiveButton("はい", (dialog, which) -> {
                  int size = barcodes.size();
                  barcodes.clear();
                  checkerAdapter.notifyItemRangeRemoved(0,size);
                  dialog.dismiss();

                  Toast toast = Toast.makeText(this, "削除完了", Toast.LENGTH_LONG);
                  toast.show();
                });
              alert.setNegativeButton("いいえ", (dialog, which) -> dialog.dismiss());

              alert.show();
            }
    );

    ImageView startButton = findViewById(R.id.start_button);
    ImageView stopButton = findViewById(R.id.stop_button);


    startButton.setOnClickListener(
            v -> {
              activeFlag = true;
              bindAnalysisUseCase();

              Toast toast = Toast.makeText(this, "スキャン開始", Toast.LENGTH_SHORT);
              toast.show();

              stopButton.setVisibility(View.VISIBLE);
              startButton.setVisibility(View.GONE);
            }
    );


    stopButton.setOnClickListener(
            v -> {
              activeFlag = false;
              bindAnalysisUseCase();

              Toast toast = Toast.makeText(this, "スキャン停止", Toast.LENGTH_SHORT);
              toast.show();

              stopButton.setVisibility(View.GONE);
              startButton.setVisibility(View.VISIBLE);
            }
    );
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putString(STATE_SELECTED_MODEL, selectedModel);
    bundle.putParcelableArrayList("BarcodeArray",barcodes);
    bundle.putBoolean("ActiveFlag",activeFlag);
  }

  @Override
  public void onResume() {
    super.onResume();
    bindAllCameraUseCases();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (imageProcessor != null) {
      imageProcessor.stop();
    }
  }

  private void bindAllCameraUseCases() {
    if (cameraProvider != null) {
      // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
      cameraProvider.unbindAll();
      bindPreviewUseCase();
      bindAnalysisUseCase();
    }
  }

  private void bindPreviewUseCase() {
    if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
      return;
    }
    if (cameraProvider == null) {
      return;
    }
    if (previewUseCase != null) {
      cameraProvider.unbind(previewUseCase);
    }

    Preview.Builder builder = new Preview.Builder();
    Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
    if (targetResolution != null) {
      builder.setTargetResolution(targetResolution);
    }
    builder.setTargetResolution(new Size(prev_resWidth,prev_resHeight));

    previewUseCase = builder.build();
    previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
  }

  private void bindAnalysisUseCase() {
    if (cameraProvider == null) {
      return;
    }
    if (analysisUseCase != null) {
      cameraProvider.unbind(analysisUseCase);
    }
    if (imageProcessor != null) {
      imageProcessor.stop();
    }

    try {
      if (BARCODE_SCANNING.equals(selectedModel)) {
        Log.i(TAG, "Using Barcode Detector Processor");
        imageProcessor = new BarcodeScannerProcessor(this, barcodes, checkerAdapter, activeFlag);
      } else {
        throw new IllegalStateException("Invalid model name");
      }
    } catch (Exception e) {
      Log.e(TAG, "Can not create image processor: " + selectedModel, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getLocalizedMessage(),
              Toast.LENGTH_LONG)
          .show();
      return;
    }

    ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
    Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
    if (targetResolution != null) {
      builder.setTargetResolution(targetResolution);
    }
    builder.setTargetResolution(new Size(anal_resWidth,anal_resHeight));
    analysisUseCase = builder.build();

    needUpdateGraphicOverlayImageSourceInfo = true;
    analysisUseCase.setAnalyzer(
        // imageProcessor.processImageProxy will use another thread to run the detection underneath,
        // thus we can just runs the analyzer itself on main thread.
        ContextCompat.getMainExecutor(this),
        imageProxy -> {
          if (needUpdateGraphicOverlayImageSourceInfo) {
            boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            if (rotationDegrees == 0 || rotationDegrees == 180) {
              graphicOverlay.setImageSourceInfo(
                  imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
            } else {
              graphicOverlay.setImageSourceInfo(
                  imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
            }
            needUpdateGraphicOverlayImageSourceInfo = false;
          }
          try {
            imageProcessor.processImageProxy(imageProxy, graphicOverlay);
          } catch (MlKitException e) {
            Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                .show();
          }
        });

    cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
          this.getPackageManager()
              .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    Log.i(TAG, "Permission granted!");
    if (allPermissionsGranted()) {
      bindAllCameraUseCases();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }
}
