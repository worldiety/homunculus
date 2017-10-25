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

import android.Manifest.permission;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import org.homunculus.android.compat.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.compat.ActivityEventDispatcher.ActivityEventCallback;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A helper class to write permission aware components. Task callbacks are optionally bound to {@link Scope}
 * by using {@link ContextScope}. Don't forget to declare the correct permissions in your manifest.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Permissions implements Destroyable {
    public final static String TAG_PERMISSION_DENIED = "permission.denied";

    private final Scope mScope;
    private final ActivityEventDispatcher mEventDispatcher;
    private final ActivityEventCallback mCallback;
    private final List<ShowRationaleForFeature> mExplainingFeatureRequests;
    private final List<RequestHolder> mFeatureRequests;
    private boolean mShowDialogOnFails = false;

    public Permissions(Scope scope, ActivityEventDispatcher<?> eventDispatcher) {
        mScope = scope;
        mEventDispatcher = eventDispatcher;
        mExplainingFeatureRequests = new ArrayList<>();
        mFeatureRequests = new ArrayList<>();
        mCallback = new AbsActivityEventCallback() {
            @Override
            public void onActivityRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
                for (RequestHolder holder : mFeatureRequests) {
                    if (requestCode == holder.id) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            holder.task.set(Result.create(holder.feature));
                        } else {
                            holder.task.set(Result.<Feature>create().put(TAG_PERMISSION_DENIED, "denied access to " + holder.feature));

                        }

                        mFeatureRequests.remove(holder);
                        return;
                    }
                }
            }
        };
        eventDispatcher.register(mCallback);
    }

    public void setShowDialogOnFails(boolean mShowDialogOnFails) {
        this.mShowDialogOnFails = mShowDialogOnFails;
    }

    public void registerExplanation(ShowRationaleForFeature func) {
        mExplainingFeatureRequests.add(func);
    }

    public void unregisterExplanation(ShowRationaleForFeature func) {
        mExplainingFeatureRequests.remove(func);
    }


    /**
     * See {@link #request(Feature, ShowRationaleForFeature)}
     */
    public Task<Result<Feature>> request(Feature feature) {
        return request(feature, null);
    }

    /**
     * Checks if permission for given feature is granted.
     *
     * @param feature to check
     * @return true if permission is granted, otherwise false
     */
    public boolean check(Feature feature) {
        return ContextCompat.checkSelfPermission(getActivity(), feature.getPermissionId()) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Requests a feature and returns a task for it.
     * If registered, a callback is used to explain a feature request to the user.
     * <p>
     * Consider using requestFeature* methods because it serves you a view to the actual allowed function set which you have requested.
     * See:
     * <ul>
     * <li>{@link #requestFeatureReadContacts()}</li>
     * </ul>
     *
     * @param feature            the feature you want to use
     * @param explanationFeature if not null, used to explain the feature for the user
     */
    public Task<Result<Feature>> request(Feature feature, ShowRationaleForFeature explanationFeature) {
        Scope scope = mScope;
        final int id = ActivityEventDispatcher.generateNextRequestId();
        SettableTask<Result<Feature>> task = SettableTask.create(scope, "Permissions-request-" + feature);
        SettableTask<Result<Feature>> returnValue = SettableTask.create(scope, "Permissions-request-ret" + feature);
        mFeatureRequests.add(new RequestHolder(id, feature, task));
        if (ContextCompat.checkSelfPermission(getActivity(), feature.getPermissionId()) == PackageManager.PERMISSION_GRANTED) {
            returnValue.set(Result.create(feature));
        } else {
            task.whenDone((rFeature) -> {
                returnValue.set(rFeature);
            });
        }
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), feature.getPermissionId())) {
            LoggerFactory.getLogger(getClass()).info("android expects that we explain {} to the user", feature);
            SettableTask<Void> explaining = SettableTask.create(scope, "permission_explaining");
            boolean everExplained = explanationFeature != null && explanationFeature.show(feature, explaining);
            if (!everExplained) {
                for (ShowRationaleForFeature func : mExplainingFeatureRequests) {
                    if (func.show(feature, explaining)) {
                        everExplained = true;
                        break;
                    }
                }
            }
            if (everExplained) {
                explaining.whenDone(rNone -> {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{feature.getPermissionId()}, id);
                });
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{feature.getPermissionId()}, id);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{feature.getPermissionId()}, id);
        }
        return returnValue;
    }


    public Task<Result<FeatureCamera>> requestFeatureCamera() {
        return request(Feature.Camera).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureCamera(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureReadContacts>> requestFeatureReadContacts() {
        return request(Feature.ReadContacts).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureReadContacts(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }

    public boolean checkFeatureLocationGPS() {
        return check(Feature.AccessFineLocation);
    }


    public Task<Result<FeatureLocationGPS>> requestFeatureLocationGPS() {
        return request(Feature.AccessFineLocation).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureLocationGPS(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureLocationNetwork>> requestFeatureLocationNetwork() {
        return request(Feature.AccessCoarseLocation).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureLocationNetwork(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureReadExternalStorage>> requestFeatureReadExternal() {
        return request(Feature.ReadExternalStorage).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureReadExternalStorage(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureWriteExternalStorage>> requestFeatureWriteExternal() {
        return request(Feature.WriteExternalStorage).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureWriteExternalStorage(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureMediaStore>> requestFeatureReadMediaStore() {
        return request(Feature.ReadExternalStorage).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureMediaStore(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }


    public Task<Result<FeatureMediaStore>> requestFeatureWriteMediaStore() {
        return request(Feature.WriteExternalStorage).continueWith(rFeature -> {
            if (rFeature.get() != null) {
                return Result.create(new FeatureMediaStore(rFeature.get(), getActivity()));
            } else {
                return Result.nullValue(rFeature);
            }
        });
    }

    public Activity getActivity() {
        return mEventDispatcher.getActivity();
    }

    @Override
    public void destroy() {
        mEventDispatcher.unregister(mCallback);
        mExplainingFeatureRequests.clear();
    }

    private class RequestHolder {
        final SettableTask<Result<Feature>> task;
        final Feature feature;
        final int id;

        RequestHolder(int id, Feature feature, SettableTask<Result<Feature>> task) {
            this.id = id;
            this.feature = feature;
            this.task = task;
        }
    }

    /**
     * Use this contract to display an explanation why you need a specific feature in your app.
     */
    public interface ShowRationaleForFeature {
        /**
         * Returns true if this rationale supports showing the feature. If so, the settable task needs to be set to continue, otherwise
         * the feature request will never complete
         */
        boolean show(Feature feature, SettableTask<Void> shownFeatureTask);
    }


    /**
     * Android 6 packs permissions into groups, but these groups is just another ill-defined set. Even google says that we should not use it at all. Taken from the doc:
     * <p>
     * "Your app still needs to explicitly request every permission it needs, even if the user has already granted another permission in the same group. In addition, the grouping of permissions into groups may change in future Android releases. Your code should not rely on the assumption that particular permissions are or are not in the same group."
     * Also note that only "dangerous" permissions can be influenced by the user, see https://developer.android.com/guide/topics/security/permissions.html#normal-dangerous
     * <p>
     * Because Google has such a bad compatibility API design, we will provide our own type safe logic here
     */
    public enum Feature {
        ReadContacts(permission.READ_CONTACTS),
        AccessFineLocation(permission.ACCESS_FINE_LOCATION),
        AccessCoarseLocation(permission.ACCESS_COARSE_LOCATION),
        ReadExternalStorage("android.permission.READ_EXTERNAL_STORAGE"),
        WriteExternalStorage(permission.WRITE_EXTERNAL_STORAGE),
        Camera(permission.CAMERA),;

        private final String mPermissionId;


        Feature(String permissionId) {
            this.mPermissionId = permissionId;

        }


        public String getPermissionId() {
            return mPermissionId;
        }
    }

//========== type and permission safe feature implementation ==========

    public static class AbsFeature {
        private final Feature mFeature;
        private final Activity mActivity;

        public AbsFeature(Feature feature, Activity activity) {
            this.mActivity = activity;
            this.mFeature = feature;
        }

        public Activity getActivity() {
            return mActivity;
        }

        public Feature getFeature() {
            return mFeature;
        }
    }

    public static class FeatureReadContacts extends AbsFeature {
        public FeatureReadContacts(Feature feature, Activity activity) {
            super(feature, activity);
        }

        /**
         * Returns a content resolver which allows you to read the contacts using the appropriate URI
         */
        public ContentResolver getContentResolver() {
            return getActivity().getContentResolver();
        }
    }

    public static class FeatureCamera extends AbsFeature {

        public FeatureCamera(Feature feature, Activity activity) {
            super(feature, activity);
        }

        /**
         * Returns the camera2 manager. You need at least API level 21.
         *
         * @return
         */
        public CameraManager getCameraManager() {
            return (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        }
    }

    public static abstract class FeatureLocation extends AbsFeature {

        public FeatureLocation(Feature feature, Activity activity) {
            super(feature, activity);
        }

        public abstract LocationManager getLocationManager();

        @Nullable
        public abstract Location getLocation();

        public abstract boolean isEnabled();
    }

    public static class FeatureLocationGPS extends FeatureLocation {
        public FeatureLocationGPS(Feature feature, Activity activity) {
            super(feature, activity);
        }

        public LocationManager getLocationManager() {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            return locationManager;
        }


        public Location getLocation() {
            Location locationGPS = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return locationGPS;
        }

        public boolean isEnabled() {
            return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
    }

    public static class FeatureLocationNetwork extends FeatureLocation {
        public FeatureLocationNetwork(Feature feature, Activity activity) {
            super(feature, activity);
        }

        public LocationManager getLocationManager() {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            return locationManager;
        }

        public Location getLocation() {
            Location locationCoarse = getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return locationCoarse;
        }

        public boolean isEnabled() {
            return getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
    }

    public static class FeatureReadExternalStorage extends AbsFeature {
        public FeatureReadExternalStorage(Feature feature, Activity activity) {
            super(feature, activity);
        }

        /**
         * See {@link Activity#getExternalCacheDir()}
         */
        public File getExternalCacheDir() {
            return getActivity().getExternalCacheDir();
        }

        /**
         * See {@link Activity#getExternalFilesDir(String)}
         */
        public File getExternalFilesDir(String type) {
            return getActivity().getExternalFilesDir(type);
        }

        /**
         * See {@link Environment#getExternalStorageDirectory()}
         */
        public File getExternalStorageDirectory() {
            return Environment.getExternalStorageDirectory();
        }
    }

    public static class FeatureWriteExternalStorage extends FeatureReadExternalStorage {
        public FeatureWriteExternalStorage(Feature feature, Activity activity) {
            super(feature, activity);
        }

    }

    public static class FeatureMediaStore extends AbsFeature {
        public FeatureMediaStore(Feature feature, Activity activity) {
            super(feature, activity);
        }

        /**
         * Returns a content resolver which allows you to read the external images using the appropriate URI
         */
        public ContentResolver getContentResolver() {
            return getActivity().getContentResolver();
        }
    }
}
