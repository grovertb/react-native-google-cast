package com.reactnative.googlecast;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.reactnative.googlecast.api.RNGCCastContext;
import com.reactnative.googlecast.api.RNGCCastSession;
import com.reactnative.googlecast.api.RNGCRemoteMediaClient;
import com.reactnative.googlecast.api.RNGCSessionManager;
import com.reactnative.googlecast.components.RNGoogleCastButtonManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleCastPackage implements ReactPackage {

  @Override
  public List<NativeModule>
  createNativeModules(ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();

    if (!RNGCCastContext.isTV(reactContext)) {
      modules.add(new RNGCCastContext(reactContext));
      modules.add(new RNGCCastSession(reactContext));
      modules.add(new RNGCRemoteMediaClient(reactContext));
      modules.add(new RNGCSessionManager(reactContext));
    }
    return modules;
  }

  public List<Class<? extends JavaScriptModule>> createJSModules() {
    return Collections.emptyList();
  }


  public static boolean isTV(@NonNull Context context) {
    UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);

    try {
      return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;

    } catch (NullPointerException exception) {
      return false;
    }
  }

  @Override
  public List<ViewManager>
  createViewManagers(ReactApplicationContext reactContext) {
    List<ViewManager> managers = new ArrayList<>();

    managers.add(new RNGoogleCastButtonManager());

    return managers;
  }
}
