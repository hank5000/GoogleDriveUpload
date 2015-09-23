/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hankwu.example.com.googledrive;

import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.FileUploadPreferences;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.api.client.http.FileContent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * An activity to illustrate how to create a file.
 */
public class CreateFileActivity extends BaseDemoActivity {

    private static final String TAG = "CreateFileActivity";

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        // create new contents resource
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }
            final DriveContents driveContents = result.getDriveContents();
            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {

                    String fileName = "/sdcard/Download/log.txt";
                    //String fileName = "/mnt/sata/720.mp4";
                    //String type = "video/mkv";


                    String type = "text/plain";

                    File f = new File(fileName);


                    Log.d("HANK",f.getName()+":"+type);

                    FileInputStream fin = null;
                    try {
                        fin = new FileInputStream(f);
                        Log.d("HANK","create inputstream success");
                    } catch (IOException e) {
                        Log.d("HANK","create inputstream fail");
                    }

                    OutputStream outputStream = driveContents.getOutputStream();

                    try {
                        byte[] buf = new byte[1024];
                        int bytesRead;
                        while(-1 != (bytesRead=fin.read(buf))) {
                            outputStream.write(buf, 0, bytesRead);
                        }
                        outputStream.close();
                        outputStream = null;

                        fin.close();
                        fin = null;

                        Log.d("HANK","Copy Over");
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(f.getName())
                            .setMimeType(type)
                            .setStarred(true).build();

                    // create a file on root folder
                    Drive.DriveApi.getRootFolder(getGoogleApiClient())
                            .createFile(getGoogleApiClient(), changeSet, driveContents)
                            .setResultCallback(fileCallback);
                }
            }.start();
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(final DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }
            result.getDriveFile().addChangeListener(getGoogleApiClient(),changeListener);
            result.getDriveFile().getMetadata(getGoogleApiClient()).setResultCallback(metadataCallback);

        }
    };


    final ResultCallback<DriveResource.MetadataResult> metadataCallback =
            new ResultCallback<DriveResource.MetadataResult>() {
                @Override
                public void onResult(DriveResource.MetadataResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e("HANK", "Problem while trying to insert or update metadata");
                        return;
                    }

                    Log.e("HANK", "Custom property successfully inserted or updated " + result.getMetadata().getFileSize());
                }
            };

    final private ChangeListener changeListener = new ChangeListener() {
        @Override
        public void onChange(ChangeEvent event) {
            //mLogTextView.setText(String.format("File change event: %s", event));
            Log.e("HANK",String.format("File change event: %s", event));
        }
    };
}
