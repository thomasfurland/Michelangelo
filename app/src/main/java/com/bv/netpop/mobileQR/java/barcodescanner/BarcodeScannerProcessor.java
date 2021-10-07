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

package com.bv.netpop.mobileQR.java.barcodescanner;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.bv.netpop.mobileQR.R;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.bv.netpop.mobileQR.GraphicOverlay;
import com.bv.netpop.mobileQR.java.VisionProcessorBase;

import java.util.ArrayList;
import java.util.List;

/** Barcode Detector */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

  private static final String TAG = "BarcodeProcessor";
  private final BarcodeScanner barcodeScanner;
  private ArrayList<String> bc;
  private BarcodeActivityAdapter projectAdapter;
  private SoundPool soundPool;
  private int sound1;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public BarcodeScannerProcessor(Context context, ArrayList<String> bc, BarcodeActivityAdapter projectAdapter) {
    super(context);
    barcodeScanner = BarcodeScanning.getClient(
            new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build());

    this.bc = bc;
    this.projectAdapter = projectAdapter;

    AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    soundPool = new SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build();

    sound1 = soundPool.load(context, R.raw.beep_sound,1);
  }

  @Override
  public void stop() {
    super.stop();
    barcodeScanner.close();
  }

  @Override
  protected Task<List<Barcode>> detectInImage(InputImage image) {
    return barcodeScanner.process(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay) {
    if (barcodes.isEmpty()) {
      Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
    }
    for (int i = 0; i < barcodes.size(); ++i) {
      Barcode barcode = barcodes.get(i);
      graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode));
      String rawBC = barcode.getRawValue().toString();
      if(!bc.contains(rawBC)) {
        bc.add(1,rawBC);
        projectAdapter.notifyItemInserted(1);
        // add sound
        soundPool.play(sound1,1,1,0,0,1);
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Barcode detection failed " + e);
  }
}
