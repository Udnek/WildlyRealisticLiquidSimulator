package me.udnekjupiter.physic.scene;

import me.udnekjupiter.physic.object.RKMObject;
import org.realityforge.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class RKMPhysicsScene implements PhysicScene {
    protected List<RKMObject> RKMObjects;

    public void addObject(RKMObject object){
        RKMObjects.add(object);
    }

    public void updateNextObjectsCoefficients() {
        for (RKMObject object : RKMObjects) {
            object.calculateNextCoefficient();
        }
    }
    public void updateNextObjectsPhaseVectors(){
        for (RKMObject object : RKMObjects) {
            object.calculateNextPhaseVector();
        }
    }

//    public void updateObjectsCoefficients() {
//        for (RKMObject object : RKMObjects) {
//            object.calculateCoefficient1();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.updateRKMPhaseVector1();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.calculateCoefficient2();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.updateRKMPhaseVector2();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.calculateCoefficient3();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.updateRKMPhaseVector3();
//        }
//        for (RKMObject object : RKMObjects) {
//            object.calculateCoefficient4();
//        }
//    }

    public void updateObjectsPositionDifferentials(){
        for (RKMObject object : RKMObjects) {
            object.RKMCalculatePositionDifferential();
        }
    }
    public void updateObjectsPositions(){
        for (RKMObject object : RKMObjects) {
            object.updatePosition();
        }
    }
    public void syncObjectsRKMPhaseVectors(){
        for (RKMObject object : RKMObjects) {
            object.setCurrentRKMPhaseVector(new Vector3d[]{object.getPosition(), object.getVelocity()});
        }
    }

    public void updateObjects(){
        syncObjectsRKMPhaseVectors();

        System.out.print("Calculated 1st coefficient --");
        updateNextObjectsCoefficients(); //coefficient1
        System.out.print("Calculated 1st step --");
        updateNextObjectsPhaseVectors(); //step1
        System.out.print("Calculated 2nd coefficient --");
        updateNextObjectsCoefficients(); //coefficient2
        System.out.print("Calculated 2nd step --");
        updateNextObjectsPhaseVectors(); //step2
        System.out.print("Calculated 3rd coefficient --");
        updateNextObjectsCoefficients(); //coefficient3
        System.out.print("Calculated 3rd step --");
        updateNextObjectsPhaseVectors(); //step3
        System.out.print("Calculated 4th coefficient --");
        updateNextObjectsCoefficients(); //coefficient4

        updateObjectsPositionDifferentials();
        updateObjectsPositions();
    }

    public void tick() {updateObjects();}
}
