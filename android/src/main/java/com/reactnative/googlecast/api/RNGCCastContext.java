package com.reactnative.googlecast.api;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.mediarouter.app.MediaRouteButton;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManager;
import com.reactnative.googlecast.RNGCExpandedControllerActivity;
import com.reactnative.googlecast.components.RNGoogleCastButtonManager;
import com.reactnative.googlecast.types.RNGCCastState;

import java.util.HashMap;
import java.util.Map;

public class RNGCCastContext
  extends ReactContextBaseJavaModule implements LifecycleEventListener {

  @VisibleForTesting
  public static final String REACT_CLASS = "RNGCCastContext";

  private static final String CAST_STATE_CHANGED = "GoogleCast:CastStateChanged";
  private CastStateListener castStateListener = new CastStateListener() {
    @Override
    public void onCastStateChanged(int i) {
      sendEvent(CAST_STATE_CHANGED, RNGCCastState.toJson(i));
    }
  };

  public RNGCCastContext(final ReactApplicationContext reactContext) {
    super(reactContext);

    reactContext.addLifecycleEventListener(this);
  }

  public static CastContext getSharedInstance(@NonNull Context context) {
    if (!RNGCCastContext.isTV(context)) {
      return CastContext.getSharedInstance(context);
    }

    return null;
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
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();

    constants.put("CAST_STATE_CHANGED", CAST_STATE_CHANGED);

    return constants;
  }

  public void sendEvent(@NonNull String eventName, @Nullable Object params) {
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @ReactMethod
  public void getCastState(final Promise promise) {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        CastContext castContext =
          CastContext.getSharedInstance(getReactApplicationContext());
        promise.resolve(RNGCCastState.toJson(castContext.getCastState()));
      }
    });
  }

  @ReactMethod
  public void endSession(final boolean stopCasting, final Promise promise) {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        SessionManager sessionManager =
          CastContext.getSharedInstance(getReactApplicationContext())
            .getSessionManager();
        sessionManager.endCurrentSession(stopCasting);
        promise.resolve(true);
      }
    });
  }

  @ReactMethod
  public void showCastDialog(final Promise promise) {
    getReactApplicationContext().runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        MediaRouteButton button = RNGoogleCastButtonManager.getCurrent();
        if (button != null) {
          button.performClick();
          promise.resolve(true);
        } else {
          promise.resolve(false);
        }
      }
    });
  }

  @ReactMethod
  public void showExpandedControls() {
    ReactApplicationContext context = getReactApplicationContext();
    Intent intent =
      new Intent(context, RNGCExpandedControllerActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @ReactMethod
  public void showIntroductoryOverlay(final ReadableMap options, final Promise promise) {
    final MediaRouteButton button = RNGoogleCastButtonManager.getCurrent();

    if ((button != null) && button.getVisibility() == View.VISIBLE) {
      getReactApplicationContext().runOnUiQueueThread(new Runnable() {
        @Override
        public void run() {
          IntroductoryOverlay.Builder builder = new IntroductoryOverlay.Builder(getCurrentActivity(), button);

          if (options.getBoolean("once")) {
            builder.setSingleTime();
          }

          builder.setOnOverlayDismissedListener(
            new IntroductoryOverlay.OnOverlayDismissedListener() {
              @Override
              public void onOverlayDismissed() {
                promise.resolve(true);
              }
            });

          IntroductoryOverlay overlay = builder.build();

          overlay.show();
        }
      });
    }
  }

  @Override
  public void onHostResume() {
    final ReactApplicationContext reactContext = getReactApplicationContext();

    reactContext.runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        CastContext castContext = CastContext.getSharedInstance(reactContext);
        castContext.addCastStateListener(castStateListener);
      }
    });

  }

  @Override
  public void onHostPause() {
    final ReactApplicationContext reactContext = getReactApplicationContext();

    reactContext.runOnUiQueueThread(new Runnable() {
      @Override
      public void run() {
        CastContext castContext = CastContext.getSharedInstance(reactContext);
        castContext.removeCastStateListener(castStateListener);
      }
    });
  }

  @Override
  public void onHostDestroy() {

  }
}
