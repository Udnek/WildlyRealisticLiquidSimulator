package me.udnek.app;

import me.udnek.Main;
import me.udnek.scene.polygonholder.PolygonHolder;

public class AppSettings {

    public final boolean recordVideo;
    public final String videoName;
    public final int videoWidth;
    public final int videoHeight;
    public int pixelScaling;
    public int cores;
    public boolean doLight;
    public boolean debugColorizePlanes;
    public final PolygonHolder.Type polygonHolderType;

    public static final AppSettings globalSettings = Main.initSettings();

    private AppSettings(boolean recordVideo, int videoWidth, int videoHeight, String videoName, int pixelScaling, int cores, PolygonHolder.Type holderType, boolean doLight, boolean debugColorizePlanes){
        this.recordVideo = recordVideo;
        this.videoName = videoName;
        this.videoHeight = videoHeight;
        this.videoWidth = videoWidth;
        this.pixelScaling = pixelScaling;
        this.cores = cores;
        this.polygonHolderType = holderType;
        this.doLight = doLight;
        this.debugColorizePlanes = debugColorizePlanes;
    }

    public static AppSettings noRecording(int pixelScaling, int cores, PolygonHolder.Type holderType, boolean doLight, boolean debugColorizePlanes){
        return new AppSettings(false, 0, 0, "", pixelScaling, cores, holderType, doLight, debugColorizePlanes);
    }
    public static AppSettings withRecording(int videoWidth, int videoHeight, String videoName, int cores, PolygonHolder.Type holderType, boolean doLight, boolean debugColorizePlanes){
        return new AppSettings(true, videoWidth, videoHeight, videoName, 1, cores, holderType, doLight, debugColorizePlanes);
    }

    public static AppSettings defaultNoRecording(int pixelScaling, int cores, PolygonHolder.Type holderType){
        return noRecording(pixelScaling, cores, holderType, false, false);
    }

    public static AppSettings defaultWithRecording(int videoWidth, int videoHeight, String videoName, int cores, PolygonHolder.Type holderType){
        return withRecording(videoWidth, videoHeight, videoName, cores, holderType, false, false);
    }

}
