package me.udnekjupiter.graphic;

import me.udnekjupiter.app.ApplicationSettings;
import me.udnekjupiter.graphic.object.light.LightSource;
import me.udnekjupiter.graphic.polygonholder.PolygonHolder;
import me.udnekjupiter.util.ColoredTriangle;
import me.udnekjupiter.util.Triangle;
import me.udnekjupiter.util.VectorUtils;
import org.realityforge.vecmath.Vector3d;

import java.awt.*;

public class RayTracer {

    private Camera camera;
    private Vector3d cameraPosition;
    private double cameraYaw, cameraPitch;
    private int width, height;
    private double fovMultiplier;

    private GraphicFrame frame;

    private PolygonHolder polygonHolder;
    private LightSource lightSource;
    private Vector3d lightPosition;

    private boolean doLight;
    private boolean debugColorizePlanes;

    public RayTracer(LightSource lightSource){
        this.doLight = ApplicationSettings.GLOBAL.doLight;
        this.lightSource = lightSource;
    }

    ///////////////////////////////////////////////////////////////////////////
    // UTILS
    ///////////////////////////////////////////////////////////////////////////
    public void rotateDirectionAsCamera(Vector3d direction){
        VectorUtils.rotatePitch(direction, cameraPitch);
        VectorUtils.rotateYaw(direction, cameraYaw);
    }

    ///////////////////////////////////////////////////////////////////////////
    // TRACING
    ///////////////////////////////////////////////////////////////////////////

    public int rayTrace(Vector3d direction){
        Vector3d nearestHitPosition = null;
        Triangle nearestPlane = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (Triangle plane : polygonHolder.getCachedPlanes(direction)) {
            Vector3d hitPosition = VectorUtils.triangleRayIntersection(direction, plane);

            if (hitPosition != null) {
                if (hitPosition.lengthSquared() < nearestDistance) {
                    nearestHitPosition = hitPosition;
                    nearestPlane = plane;
                    nearestDistance = hitPosition.lengthSquared();
                }
            }
        }

        if (nearestPlane == null) return 0;
        return colorizeRayTrace(nearestHitPosition, nearestPlane);
    }

    public void renderFrame(GraphicFrame frame, PolygonHolder polygonHolder, Camera camera){;
        this.frame = frame;
        this.width = frame.getWidth();
        this.height = frame.getHeight();

        this.camera = camera;
        this.polygonHolder = polygonHolder;
        this.cameraPosition = camera.getPosition();
        this.cameraYaw = Math.toRadians(camera.getYaw());
        this.cameraPitch = Math.toRadians(camera.getPitch());
        this.fovMultiplier = width/camera.getFov();
        this.debugColorizePlanes = ApplicationSettings.GLOBAL.debugColorizePlanes;
        this.doLight = ApplicationSettings.GLOBAL.doLight;
        if (doLight) lightPosition = lightSource.getPosition();

        int cores = ApplicationSettings.GLOBAL.cores;
        if (cores != 1){

            Thread[] threads = new Thread[cores];
            int threadXStep = width / cores;
            for (int i = 0; i < cores; i++) {
                Thread thread = new Thread(new RayTracerRunnable(threadXStep * i, threadXStep * (i + 1), 0, height));
                threads[i] = thread;
                thread.start();
            }
            try {
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException e) {throw new RuntimeException(e);}


        } else {
            RayTracerRunnable runnable = new RayTracerRunnable(0, width, 0, height);
            runnable.run();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // COLORIZING
    ///////////////////////////////////////////////////////////////////////////

    private double positionLighted(Vector3d position, Triangle plane){

        // to absolute position;
        position.add(cameraPosition);
        // to light relative position
        position.sub(lightPosition);
        // from light to point direction
        Vector3d direction = position;

        final float EPSILON = 0.0001f;

        for (Triangle triangle : polygonHolder.getLightCachedPlanes(direction)) {
            Vector3d hitPosition = VectorUtils.triangleRayIntersection(direction, triangle);
            if (hitPosition != null){
                if (direction.lengthSquared() - EPSILON > hitPosition.lengthSquared()){
                    return 0;
                }
            }
        }
        double perpendicularity = 1 - new Vector3d().cross(plane.getNormal().normalize(), direction.normalize()).length();
        return perpendicularity;
    }

    private int colorizeRayTrace(Vector3d hitPosition, Triangle plane){

        if (!debugColorizePlanes && plane instanceof ColoredTriangle coloredPlane){
            return coloredPlane.getColor();
        }

        double d0 = VectorUtils.distance(hitPosition, plane.getVertex0());
        double d1 = VectorUtils.distance(hitPosition, plane.getVertex1());
        double d2 = VectorUtils.distance(hitPosition, plane.getVertex2());
        Vector3d distances = new Vector3d(d0, d1, d2);
        double minDistance = VectorUtils.getMin(distances);
        Vector3d color;
        if (minDistance <= 0.01){
            color = new Vector3d(1f, 1f, 1f);
        } else {
            color = new Vector3d(1/d0, 1/d1 ,1/d2);
            color.div(VectorUtils.getMax(color));
            VectorUtils.cutTo(color, 1f);
        }

        if (doLight){
            float light = (float) positionLighted(hitPosition, plane);
            light += 0.1f;
            if (light < 0.15) light = 0.15f;
            else if (light > 1) light = 1;

            color.mul(light);
        }


        return new Color((float) color.x, (float) color.y, (float) color.z).getRGB();

    }

    ///////////////////////////////////////////////////////////////////////////
    // THREAD
    ///////////////////////////////////////////////////////////////////////////

    public class RayTracerRunnable implements Runnable{
        private final int xFrom, xTo, yFrom, yTo;
        public RayTracerRunnable(int xFrom, int xTo, int yFrom, int yTo){
            this.xFrom = xFrom;
            this.xTo = xTo;
            this.yFrom = yFrom;
            this.yTo = yTo;
        }

        @Override
        public void run() {
            double xOffset = -width/2.0;
            double yOffset = -height/2.0;

            for (int x = xFrom; x < xTo; x++) {
                for (int y = yFrom; y < yTo; y++) {

                    Vector3d direction = new Vector3d(
                            (x+xOffset),
                            (y+yOffset),
                            fovMultiplier
                    );

                    rotateDirectionAsCamera(direction);

                    int color = rayTrace(direction);
                    frame.setPixel(x, y, color);
                }
            }
        }
    }
}
