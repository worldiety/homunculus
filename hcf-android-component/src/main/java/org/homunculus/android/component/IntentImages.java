/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculus.android.component;

import android.Manifest.permission;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import org.homunculus.android.component.Intents.ResultIntent;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.ActivityEventDispatcher.ActivityEventCallback;
import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Procedure;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;


/**
 * A simple helper class to make some use cases easier around handling image intents.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class IntentImages implements Destroyable {
    /**
     * This is hardcoded because we need to find it back when the activity restarts. Using dynamic values would not work here.
     */
    private int requestCodeCamera = 32123;
    private int requestCodeImage = 32124;
    private final Intents intentManager;
    private final Permissions permissions;
    private final List<Procedure<List<Uri>>> listeners;
    private final ActivityEventDispatcher<?> events;
    private final ActivityEventCallback callback;
    private List<Uri> lastParsedUris;
    private final Scope scope;

    /**
     * Creates a new instance to handle image intents and onNewIntent events. Always checks the current activities intent for any uris when created. Due to the nature of the matter, it cannot decide if an uri
     * refers actually to an image without resolving it.
     *
     * @param events may be null. If so, no onNewIntent events are handled.
     */
    public IntentImages(Scope scope, ActivityEventDispatcher<?> events) {
        intentManager = new Intents(scope, events);
        permissions = new Permissions(scope, events);
        listeners = new ArrayList<>();
        this.events = events;
        this.scope = scope;

        lastParsedUris = parseUris(permissions.getActivity().getIntent());
        callback = new AbsActivityEventCallback() {
            @Override
            public void onActivityNewIntent(Activity activity, Intent intent) {
                lastParsedUris = parseUris(intent);
                notifyUrisChanged();
            }
        };
        if (this.events != null) {
            events.register(callback);
        }

    }

    /**
     * Updates the default camera request code, in case you need to fix collisions
     *
     * @param requestCodeCamera the new request code
     */
    public void setRequestCodeCamera(int requestCodeCamera) {
        this.requestCodeCamera = requestCodeCamera;
    }

    /**
     * Updates the default image request code, in case you need to fix collisions
     *
     * @param requestCodeImage the new request code
     */
    public void setRequestCodeImage(int requestCodeImage) {
        this.requestCodeImage = requestCodeImage;
    }

    private void notifyUrisChanged() {
        for (Procedure<List<Uri>> cb : listeners) {
            cb.apply(lastParsedUris);
        }
    }

    /**
     * Starts an intent to pick a gallery photo using the recommended android way.
     * The only modifications for your android manifest is to add the android.permission.READ_EXTERNAL_STORAGE.
     *
     * @return a task which indicates if the intent has been fired successfully (e.g. camera access granted and camera activity available)
     */
    public ResultIntent<Result<Uri>> imageIntent() {
        return new PickImage();
    }

    /**
     * Starts an intent to pick a camera photo using the recommended android way by providing a FileProvider (hcf_files) target uri.
     * The only modifications for your android manifest is to add the android.permission.CAMERA permission.
     * This method has been tested and proved to work on the following devices:
     * <ul>
     * <li>Dell Venue 7, Stock Camera, Android 4.4.2</li>
     * <li>Samsung S4 Mini, Samsung Camera, Android 4.4.2</li>
     * <li>Samsung S3, Samsung Camera, Android 4.3</li>
     * <li>Google Pixel XL, Stock Camera, Android 8.1</li>
     * <li>Samsung S5 Neo, Samsung Camera, Android 6.0.1</li>
     * <li>Sony Z5 Compact, Sony Camera, Android 7.1.1</li>
     * <li></li>
     * </ul>
     * <p>
     * A note of warning: You cannot rely on anything here, because Android's intent system is entirely underspecified and has no guarantees.
     * This may be the reason why e.g. WhatsApp has created their own camera app, instead of using the system one. Also you cannot tell your customer, that he should
     * install the stock android app, because it is simply not available for all devices. However, it is also not recommended to implement your own camera
     * because the Google API is hard to impossible to get right, depending on your expectations.
     *
     * @return returns a task which indicates if the intent has been fired successfully (e.g. camera access granted and camera activity available)
     */
    public ResultIntent<Result<Uri>> cameraIntent() {
        return new PickCamera();
    }

    /**
     * There are weired crashing bugs with the samsung camera app on at least android 4.4. The stock camera on at least android 8.1 seems not to have any problem
     */
    private void fixPermissionsForLegacy(Intent intent, Uri uri) {
        if (VERSION.SDK_INT < VERSION_CODES.O) {

            List<ResolveInfo> resolvedIntentActivities = intentManager.getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                String packageName = resolvedIntentInfo.activityInfo.packageName;

                intentManager.getContext().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }


    private File getCameraTmpFile() {
        File dir = new File(permissions.getActivity().getFilesDir(), "hcf_files");
        return new File(dir, "lastCameraImage.jpg");
    }


    public static Task<Result<List<Uri>>> asyncParseUris(Scope context, Intent intent) {
        return Async.createTask(context, (ctx) -> Result.create(parseUris(intent)));
    }

    /**
     * Tries to parse and extract uris from any information given in the intent.
     *
     * @param intent null-safe
     * @return a list of uris or an empty list
     */
    public static List<Uri> parseUris(Intent intent) {
        Set<Uri> myUris = new TreeSet<>();
        if (intent == null) {
            return new ArrayList<>();
        }
        Bundle extras = intent.getExtras();

        //check the simple case (actually may be also a non image, but that's not our problem)
        if (intent.getData() != null) {
            myUris.add(intent.getData());
        }
        if (extras != null) {

            //check for a single uri in extra stream
            if (extras.getParcelable(Intent.EXTRA_STREAM) instanceof Uri) {
                Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
                if (uri != null)
                    myUris.add(uri);
            }

            //check for multiple uris in extra stream
            if (extras.getParcelable(Intent.EXTRA_STREAM) instanceof ArrayList) {
                ArrayList<Parcelable> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (list != null) {
                    for (Parcelable p : list) {
                        if (p instanceof Uri) {
                            Uri uri = (Uri) p;
                            myUris.add(uri);
                        }
                    }
                }
            }

            //check a single text for an uri
            {
                CharSequence seq = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                if (seq != null) {
                    String src = seq.toString();
                    String[] strings = src.split(Pattern.quote("\n"));
                    for (String string : strings) {
                        try {
                            URI uri = new URI(string);
                            myUris.add(Uri.parse(uri.toString()));
                        } catch (URISyntaxException e) {
                            LoggerFactory.getLogger(IntentImages.class).debug("cannot parse as uri: {}", string);
                            continue;
                        }
                    }
                }
            }

            //check multiple texts for uris
            if (intent.getCharSequenceArrayExtra(Intent.EXTRA_TEXT) != null) {
                String[] text = (String[]) intent.getCharSequenceArrayExtra(Intent.EXTRA_TEXT);
                for (String txt : text) {
                    String[] strings = txt.split(Pattern.quote("\n"));
                    for (String string : strings) {
                        try {
                            URI uri = new URI(string);
                            myUris.add(Uri.parse(uri.toString()));
                        } catch (URISyntaxException e) {
                            LoggerFactory.getLogger(IntentImages.class).debug("cannot parse as uri: {}", string);
                            continue;
                        }

                    }
                }
            }
        }

        //the google photos app sends it as clipdata
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            ClipData clipData = intent.getClipData();
            if (clipData != null) {
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri == null) {
                        try {
                            myUris.add(Uri.parse(clipData.getItemAt(i).getText().toString()));
                        } catch (Exception e) {
                            LoggerFactory.getLogger(IntentImages.class).debug("cannot parse as uri: {}", clipData.getItemAt(i).getText().toString());
                            continue;
                        }
                    } else {
                        myUris.add(uri);
                    }
                }
            }
        }

        return new ArrayList<>(myUris);
    }

    /**
     * Registers an image callback. The callback is always called directly, even if no images are available (list of size 0).
     *
     * @param func
     */

    public void registerImagesReceivedCallback(Procedure<List<Uri>> func) {
        func.apply(lastParsedUris);
        listeners.add(func);
    }

    /**
     * unregisters a prior registered callback
     *
     * @param func
     */
    public void unregisterImagesReceivedCallback(Procedure<List<Uri>> func) {
        listeners.remove(func);
    }

    @Override
    public void destroy() {
        if (events != null) {
            events.unregister(callback);
        }
    }

    private class PickImage implements ResultIntent<Result<Uri>> {

        @Override
        public Task<Result<Boolean>> invoke() {
            SettableTask<Result<Boolean>> resFile = SettableTask.create("IntentImages.pickImage");
            permissions.handlePermission(permission.READ_EXTERNAL_STORAGE).whenDone(r -> {
                if (r.isGranted()) {
                    try {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        String title = intentManager.getContext().getString(R.string.hcf_intent_choose_image);
                        intentManager.startActivityForResult(Intent.createChooser(intent, title), requestCodeImage);
                    } catch (Exception e) {
                        resFile.set(Result.auto(e));
                    }
                } else {
                    resFile.set(Result.nullValue(r.asResult()));
                }
            });

            return resFile;
        }

        @Override
        public void whenReceived(Procedure<Result<Uri>> callback) {
            intentManager.registerOnActivityResult(requestCodeImage, activityResult -> {
                if (activityResult.getResultCode() != Activity.RESULT_OK) {
                    callback.apply(Result.<Uri>create().put("code", activityResult.getRequestCode()).setThrowable(new RuntimeException("unsupported result code: " + activityResult.getRequestCode())));
                } else {
                    asyncParseUris(scope, activityResult.getData()).whenDone(resList -> {
                        if (resList.exists() && resList.get().size() > 0) {
                            callback.apply(Result.create(resList.get().get(0)));
                        } else {
                            callback.apply(Result.nullValue(resList));
                        }
                    });
                }
                return true;
            });
        }
    }

    private class PickCamera implements ResultIntent<Result<Uri>> {

        @Override
        public Task<Result<Boolean>> invoke() {
            SettableTask<Result<Boolean>> resFile = SettableTask.create("IntentImages.pickCameraPhoto");

            permissions.handlePermission(permission.CAMERA).whenDone(r -> {
                if (r.isGranted()) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(permissions.getActivity().getPackageManager()) != null) {

                        File file;
                        try {
                            File dir = new File(permissions.getActivity().getFilesDir(), "hcf_files");
                            if (!dir.mkdirs()) {
                                if (!dir.isDirectory()) {
                                    throw new IOException("not a directory or permission denied: " + dir);
                                }
                            }
                            // Create the File where the photo should go
                            file = new File(dir, "lastCameraImage.jpg");
                            if (!file.delete()) {
                                if (file.exists()) {
                                    throw new IOException("cannot delete file: " + file);
                                }
                            }
                            if (!file.createNewFile()) {
                                if (!file.isFile()) {
                                    throw new IOException("cannot create empty file: " + file);
                                }
                            }

                            Uri photoURI = FileProvider.getUriForFile(permissions.getActivity(), getAuthority(), file);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            fixPermissionsForLegacy(takePictureIntent, photoURI);
                            intentManager.startActivityForResult(takePictureIntent, requestCodeCamera);
                            resFile.set(Result.create(true));
                        } catch (IOException e) {
                            Result res = Result.<Boolean>create().setThrowable(e);
                            resFile.set(res);
                        }

                    } else {
                        resFile.set(Result.<Boolean>create().put("intent.camera.missing"));
                    }
                } else {
                    resFile.set(Result.nullValue(r.asResult()));
                }
            });

            return resFile;
        }

        private String getAuthority() {
            return permissions.getActivity().getApplicationContext().getPackageName() + ".hcf.provider";
        }

        @Override
        public void whenReceived(Procedure<Result<Uri>> callback) {
            intentManager.registerOnActivityResult(requestCodeCamera, activityResult -> {
                if (activityResult.getResultCode() != Activity.RESULT_OK) {
                    callback.apply(Result.<Uri>create().put("code", activityResult.getRequestCode()).setThrowable(new RuntimeException("unsupported result code: " + activityResult.getRequestCode())));
                } else {
                    if (getCameraTmpFile().length() > 0) {
                        Uri photoURI = FileProvider.getUriForFile(permissions.getActivity(), getAuthority(), getCameraTmpFile());
                        callback.apply(Result.create(photoURI));
                    } else {
                        asyncParseUris(scope, activityResult.getData()).whenDone(resList -> {
                            if (resList.exists() && resList.get().size() > 0) {
                                callback.apply(Result.create(resList.get().get(0)));
                            } else {
                                callback.apply(Result.nullValue(resList));
                            }
                        });
                    }
                }
                return true;
            });
        }
    }
}
