package dev.opencover.mirror;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.Looper;
import android.view.Display;
import dev.opencover.BuildConfig;

/** Runs through Shizuku, and only retargets this app's active, consented projection. */
public final class DesktopMirrorSource extends IDesktopMirror.Stub {
    private final Context context;
    public DesktopMirrorSource(Context context) { this.context=context; }
    public void destroy() { System.exit(0); }
    public boolean selectSource(int source,int destination,IBinder projectionToken) {
        try { return select(source,destination,projectionToken); }
        catch(Exception failure) { android.util.Log.e("OpenCoverMirror","Cannot select desktop source",failure);return false; }
    }
    private boolean select(int source,int destination,IBinder projectionToken) throws Exception {
        if(source<=1 || destination<=1 || source==destination || projectionToken==null)
            throw new IllegalArgumentException("Invalid displays or token");
        DisplayManager dm=context.getSystemService(DisplayManager.class);
        Display from=dm.getDisplay(source),to=dm.getDisplay(destination);
        if(from==null || to==null || ((Integer)Display.class.getMethod("getType").invoke(from))==1 || "OpenCover-mirror".equals(from.getName())
                || !"OpenCover-mirror".equals(to.getName()))throw new IllegalArgumentException("Mirror display gone");
        Class<?> infoApi=Class.forName("android.hardware.display.DisplayManagerGlobal");
        Object global=infoApi.getMethod("getInstance").invoke(null);
        Object displayInfo=infoApi.getMethod("getDisplayInfo",int.class).invoke(global,destination);
        if(!BuildConfig.APPLICATION_ID.equals(displayInfo.getClass().getField("ownerPackageName").get(displayInfo)))
            throw new SecurityException("Not this app's virtual display");
        Class<?> serviceManager=Class.forName("android.os.ServiceManager");
        IBinder binder=(IBinder)serviceManager.getMethod("getService",String.class).invoke(null,"media_projection");
        Class<?> api=Class.forName("android.media.projection.IMediaProjectionManager");
        Object manager=Class.forName("android.media.projection.IMediaProjectionManager$Stub")
            .getMethod("asInterface",IBinder.class).invoke(null,binder);
        Object info=api.getMethod("getActiveProjectionInfo").invoke(manager);
        if(info==null || !BuildConfig.APPLICATION_ID.equals(info.getClass().getMethod("getPackageName").invoke(info)))
            throw new SecurityException("Not this app's projection");
        Object projection=Class.forName("android.media.projection.IMediaProjection$Stub")
            .getMethod("asInterface",IBinder.class).invoke(null,projectionToken);
        Class<?> projectionApi=Class.forName("android.media.projection.IMediaProjection");
        if(!Boolean.TRUE.equals(api.getMethod("isCurrentProjection",projectionApi).invoke(manager,projection)))
            throw new SecurityException("Projection ended");
        Class<?> session=Class.forName("android.view.ContentRecordingSession");
        Object value=session.getMethod("createDisplaySession",int.class).invoke(null,source);
        session.getMethod("setVirtualDisplayId",int.class).invoke(value,destination);
        java.lang.reflect.Method set=api.getMethod("setContentRecordingSession",session,projectionApi);
        // Android ignores replacement sessions on the same destination; detach it before retargeting.
        if(!Boolean.TRUE.equals(set.invoke(manager,null,projection)) || !Boolean.TRUE.equals(set.invoke(manager,value,projection)))
            throw new IllegalStateException("Android rejected the source");
        android.util.Log.i("OpenCoverMirror","Desktop mirror ready: "+source+" -> "+destination);
        return true;
    }
}
