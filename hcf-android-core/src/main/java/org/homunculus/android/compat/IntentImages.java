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
package org.homunculus.android.compat;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import org.homunculus.android.compat.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.compat.ActivityEventDispatcher.ActivityEventCallback;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * A simple helper class to make some use cases easier around handling image intents.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class IntentImages implements Destroyable {

    private final Intents mIntentManager;
    private final Permissions mPermissions;
    private final List<Procedure<List<Uri>>> mListeners;
    private final ActivityEventDispatcher<?> mEvents;
    private final ActivityEventCallback mCallback;
    private List<Uri> mLastParsedUris;

    /**
     * Creates a new instance to handle image intents and onNewIntent events. Always checks the current activities intent for any uris when created. Due to the nature of the matter, it cannot decide if an uri
     * refers actually to an image without resolving it.
     *
     * @param events      may be null. If so, no onNewIntent events are handled.
     * @param permissions
     * @param manager
     */
    public IntentImages(ActivityEventDispatcher<?> events, Permissions permissions, Intents manager) {
        mIntentManager = manager;
        mPermissions = permissions;
        mListeners = new ArrayList<>();
        mEvents = events;

        mLastParsedUris = parseUris(permissions.getActivity().getIntent());
        mCallback = new AbsActivityEventCallback() {
            @Override
            public void onActivityNewIntent(Activity activity, Intent intent) {
                mLastParsedUris = parseUris(intent);
                notifyUrisChanged();
            }
        };
        if (mEvents != null) {
            events.register(mCallback);
        }

    }

    /**
     * Creates a new instance to handle image intents. See {@link #IntentImages(ActivityEventDispatcher, Permissions, Intents)}
     *
     * @param permissions
     * @param manager
     */
    public IntentImages(Permissions permissions, Intents manager) {
        this(null, permissions, manager);
    }

    private void notifyUrisChanged() {
        for (Procedure<List<Uri>> cb : mListeners) {
            cb.apply(mLastParsedUris);
        }
    }


    /**
     * Starts an intent to pick a single image
     *
     * @return
     */
    public Task<Result<List<Uri>>> pickImage(Scope scope) {
        Intent pickImages = new Intent(Intent.ACTION_GET_CONTENT);
        pickImages.setType("image/*");
        return pickUris(scope, pickImages);
    }

    /**
     * Starts an intent to pick multiple images. Works not on all platforms (introduced in SDK 18 / JellyBean)
     *
     * @return
     */
    public Task<Result<List<Uri>>> pickImages(Scope scope) {
        Intent pickImages = new Intent(Intent.ACTION_GET_CONTENT);
        pickImages.setType("image/*");
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            pickImages.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        } else {
            LoggerFactory.getLogger(getClass()).debug(VERSION.SDK_INT + " does not support EXTRA_ALLOW_MULTIPLE");
        }
        return pickUris(scope, pickImages);
    }


    /**
     * Starts an intent to pick a camera photo. You cannot rely on anything here, because Android's intent system is entirely underspecified and untestet. Even their most simple SDK examples
     * won't work - even with their own stock camera (e.g. camera opens, stores image when pressed back but removes it if the user leaves the camera activity by accepting etc...)
     *
     * @param authority the android authority as defined in manifest in the FileProvider android:authorities attribute
     * @return a task returning the given file to write the image into. Fails if the image was not captured.
     */
    public Task<Result<File>> pickCameraPhoto(Scope scope, String authority) {
        SettableTask<Result<File>> resFile = SettableTask.create(scope, "IntentImages.pickCameraPhoto");

        mPermissions.requestFeatureCamera(mIntentManager.getContext()).whenDone(r -> {
            if (r.exists()) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(mPermissions.getActivity().getPackageManager()) != null) {

                    Async.createTask(scope, (ctx) -> {
                        File file;
                        try {
                            file = proposeImageFile();
                            // Create the File where the photo should go
                            file.delete();

                            if (!file.createNewFile()) {
                                throw new IOException("failed to create " + file);
                            }
                        } catch (IOException e) {
                            return Result.<File>create().setThrowable(e);
                        }
                        Uri photoURI = FileProvider.getUriForFile(mPermissions.getActivity(), authority, file);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        //hold this task until the intent returns. Actually we are not parsing uris from it
                        Result<List<Uri>> resPicIntent = Async.await(pickUris(scope, takePictureIntent));
                        if (resPicIntent.hasTag(Intents.TAG_UNACCEPTED_RESULT_CODE)) {
                            return Result.<File>create().putTag(Result.TAG_CANCELLED, null);
                        }
                        if (file.length() == 0) {
                            return Result.<File>create().setThrowable(new IOException("camera failed to write into " + file + ", either the app does not support the feature or access right are missing."));
                        }
                        return Result.<File>create(file);
                    }).whenDone(res -> {
                        resFile.set(res);
                    });

                } else {
                    resFile.set(Result.<File>create().putTag("intent.camera.misssing", null));
                }
            } else {
                resFile.set(Result.nullValue(r));
            }
        });


        return resFile;

    }


    private File proposeImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mPermissions.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }


    private Task<Result<List<Uri>>> pickUris(Scope scope, Intent queryIntent) {
        SettableTask<Result<List<Uri>>> resFile = SettableTask.create(scope, "IntentImages.pickUris");
        mPermissions.requestFeatureReadMediaStore(scope).whenDone(feature -> {
            if (feature.exists()) {
                mIntentManager.startIntent(scope, queryIntent).whenDone(rIntent -> {
                    if (rIntent.exists()) {
                        asyncParseUris(scope, rIntent.get().getResponse()).whenDone(resList -> {
                            resFile.set(resList);
                        });
                    } else {
                        resFile.set(Result.nullValue(rIntent));
                    }
                });
            } else {
                resFile.set(Result.nullValue(feature));
            }
        });
        return resFile;
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
        func.apply(mLastParsedUris);
        mListeners.add(func);
    }

    /**
     * unregisters a prior registered callback
     *
     * @param func
     */
    public void unregisterImagesReceivedCallback(Procedure<List<Uri>> func) {
        mListeners.remove(func);
    }

    @Override
    public void destroy() {
        if (mEvents != null) {
            mEvents.unregister(mCallback);
        }
    }
}
