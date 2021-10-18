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
import com.bv.netpop.mobileQR.java.BarcodeBase;
import com.bv.netpop.mobileQR.java.POPQRBarcode;
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
  private final ArrayList<BarcodeBase> bc;
  private final BarcodeActivityAdapter projectAdapter;
  private final SoundPool soundPool;
  private final boolean start;
  private final int sound1;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public BarcodeScannerProcessor(Context context, ArrayList<BarcodeBase> bc, BarcodeActivityAdapter projectAdapter, boolean start) {
    super(context);
    barcodeScanner = BarcodeScanning.getClient(
            new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build());

    this.bc = bc;
    this.projectAdapter = projectAdapter;
    this.start = start;

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

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onSuccess(
      @NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay) {
    if (barcodes.isEmpty()) {
      Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
    }
    if(!start) {
      return;
    }
    for (int i = 0; i < barcodes.size(); ++i) {
      Barcode barcode = barcodes.get(i);
      String rawBC = barcode.getRawValue();

      BarcodeBase objBarcode = new POPQRBarcode(rawBC);
      if (!objBarcode.isValid) {
        return;
      }
      graphicOverlay.add(new BarcodeGraphic(graphicOverlay, barcode, objBarcode));
      if(bc.stream().noneMatch(x -> x.rawValue.equals(rawBC))) {
        bc.add(1,objBarcode);
        projectAdapter.notifyItemInserted(1);

        soundPool.play(sound1,1,1,0,0,1);
      }
//      if(!bc.contains(rawBC)) {
//        bc.add(1,objBarcode);
//        projectAdapter.notifyItemInserted(1);
//
//        soundPool.play(sound1,1,1,0,0,1);
//      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Barcode detection failed " + e);
  }
}
