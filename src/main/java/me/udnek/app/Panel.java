package me.udnek.app;

import me.jupiter.file_managment.FileManager;
import me.udnek.app.console.Command;
import me.udnek.app.console.ConsoleHandler;
import me.udnek.scene.Camera;
import me.udnek.scene.Scene;
import me.udnek.util.UserAction;
import org.decimal4j.util.DoubleRounder;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.realityforge.vecmath.Vector3d;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Panel extends JPanel implements ConsoleHandler {

    private boolean screenIsOpen = true;
    private Scene scene;
    private final Frame frame;
    private AWTSequenceEncoder videoEncoder;
    private final AppSettings appSettings;
    private final DebugMenu debugMenu;

    private boolean renderInProgress = false;

    // STATS
    private int framesAmount = 0;
    private double fpsSum = 0;
    private double renderTime;
    private double averageFpsLastSecond;


    public Panel(Frame frame, Scene scene, AppSettings appSettings){
        setBackground(Color.GRAY);
        this.frame = frame;
        this.scene = scene;
        this.appSettings = appSettings;
        if (appSettings.recordVideo){
            File file = FileManager.readFile(FileManager.Directory.VIDEO, appSettings.videoName+".mp4");
            try {videoEncoder = AWTSequenceEncoder.createSequenceEncoder(file, 25);
            } catch (IOException e) {throw new RuntimeException(e);}
        }
        scene.init(appSettings.polygonHolderType);
        this.debugMenu = new DebugMenu(15);

    }

    @Override
    public void paint(Graphics graphics) {
        scene.tick();

        int renderWidth;
        int renderHeight;
        if (appSettings.recordVideo){
            renderWidth = appSettings.videoWidth;
            renderHeight = appSettings.videoHeight;
        }
        else {
            renderWidth = getWidth();
            renderHeight = getHeight();
        }

        BufferedImage frame = scene.renderFrame(renderWidth, renderHeight, appSettings.pixelScaling, appSettings.cores);


        if (appSettings.recordVideo){
            try {
                videoEncoder.encodeImage(frame);
            } catch (IOException e) {throw new RuntimeException(e);}
        }

        graphics.drawImage(frame, 0, 0, getWidth(), getHeight(), null);

        if (debugMenu.isEnabled()){
            debugMenu.resetForNewFrame(graphics, getWidth());
            showDebug();
        }

        renderInProgress = false;
    }

    private void showDebug(){
        Camera camera = scene.getCamera();
        Vector3d position = camera.getPosition();
        debugMenu.addTextToLeft(
                "FPS: "+DoubleRounder.round(averageFpsLastSecond, 2) +
                " SPF: "+DoubleRounder.round(renderTime, 4)
        );
        debugMenu.addTextToRight(
                "x:"+ DoubleRounder.round(position.x, 2) +
                " y:" + DoubleRounder.round(position.y, 2) +
                " z:" + DoubleRounder.round(position.z, 2) +
                " yaw:" + camera.getYaw() +
                " pitch:" + camera.getPitch()
        );
        String[] extraDebug = scene.getExtraDebug();
        if (extraDebug == null) return;
        for (String text : extraDebug) {
            debugMenu.addTextToLeft(text);
        }
    }

    public void nextFrame(){
        renderInProgress = true;
        repaint();
    }

    public void onWindowClosed(){
        if (appSettings.recordVideo){
            try {
                videoEncoder.finish();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println();
        System.out.println("Frames: "+ framesAmount);
        System.out.println("Avg FPS: "+ fpsSum/framesAmount);

    }
    public void handleKeyInput(UserAction userAction) {
        if (userAction == UserAction.DEBUG_MENU){
            debugMenu.toggle();
        }
        scene.handleUserInput(userAction);
    }
    @Override
    public void handleCommand(Command command, String[] args) {
        scene.handleCommand(command, args);
    }

    public void loop(){
        double timeSinceAverageFpsUpdate = 0;
        double fpsSumLastTime = 0;
        double framesSinceAverageFpsUpdate = 0;
        while (true) {
            long startTime = System.nanoTime();
            this.nextFrame();

            while (renderInProgress) {
                try {
                    Thread.sleep(1, 0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            renderTime = (System.nanoTime() - startTime)/Math.pow(10, 9);

            timeSinceAverageFpsUpdate += renderTime;
            fpsSumLastTime += 1/renderTime;
            framesSinceAverageFpsUpdate++;

            if (timeSinceAverageFpsUpdate >= 0.5){
                averageFpsLastSecond = fpsSumLastTime/framesSinceAverageFpsUpdate;
                timeSinceAverageFpsUpdate = 0;
                fpsSumLastTime = 0;
                framesSinceAverageFpsUpdate = 0;
            }

            frame.setTitle("WRLS" + " ("+getWidth()+"x"+getHeight()+")");
            // stats
            fpsSum += 1/renderTime;
            framesAmount++;
        }
    }

}
