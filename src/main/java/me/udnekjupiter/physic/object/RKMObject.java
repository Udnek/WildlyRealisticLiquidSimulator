package me.udnekjupiter.physic.object;

import me.udnekjupiter.app.Application;
import me.udnekjupiter.physic.EnvironmentSettings;
import me.udnekjupiter.physic.collision.Collidable;
import me.udnekjupiter.physic.collision.Collider;
import me.udnekjupiter.physic.collision.CollisionCalculator;
import me.udnekjupiter.physic.collision.SphereCollider;
import me.udnekjupiter.util.Freezable;
import me.udnekjupiter.util.VectorUtils;
import org.realityforge.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class RKMObject extends PhysicObject implements Freezable, Collidable {
    protected EnvironmentSettings settings;
    protected List<RKMObject> collidingObjects;
    protected Vector3d[] currentRKMPhaseVector;
    protected Vector3d[] basePhaseVector;
    protected Vector3d[] coefficient1;
    protected Vector3d[] coefficient2;
    protected Vector3d[] coefficient3;
    protected Vector3d[] coefficient4;
    protected Vector3d velocity;
    protected Vector3d acceleration;
    protected Vector3d positionDifferential;
    protected Vector3d velocityDifferential;
    protected Collider collider;
    protected boolean frozen;
    protected double deltaTime;
    protected double decayCoefficient;
    protected double mass;
    protected int coefficientCounter = 1;

    public RKMObject(Vector3d position) {
        super(position);
        settings = Application.ENVIRONMENT_SETTINGS;
        this.velocity = new Vector3d(0,0,0);
        this.acceleration = new Vector3d(0,0,0);
        this.basePhaseVector = new Vector3d[]{position, new Vector3d(0,0,0)};
        setCurrentRKMPhaseVector(basePhaseVector);
    }

    public Collider getCollider(){
        return collider;
    }
    public void addCollidingObject(RKMObject object){
        collidingObjects.add(object);
    }
    public List<RKMObject> getCollidingObjects(){return collidingObjects;}
    public void clearCollidingObjects(){
        collidingObjects = new ArrayList<>();
    }
    public boolean collidingObjectIsAlreadyListed(RKMObject object){
        return collidingObjects.contains(object);
    }

    public Vector3d getVelocity(){
        return velocity.dup();
    }
    public void setVelocity(Vector3d velocity) {
        this.velocity = velocity.dup();
    }
    public void setCurrentRKMPhaseVector(Vector3d[] newRKMPhaseVector) {
        this.currentRKMPhaseVector = newRKMPhaseVector;
    }
    public Vector3d[] getCurrentRKMPhaseVector() {
        return new Vector3d[]{currentRKMPhaseVector[0].dup(), currentRKMPhaseVector[1].dup()};
    }
    public Vector3d getCurrentRKMPosition(){return getCurrentRKMPhaseVector()[0].dup();}

    protected Vector3d[] RKMethodFunction(Vector3d[] inputComponents){
        Vector3d[] resultComponents = new Vector3d[2];
        resultComponents[0] = inputComponents[1];
        resultComponents[1] = RKMethodCalculateAcceleration(inputComponents[0], inputComponents[1]);
        return resultComponents;
    }

    protected Vector3d[] RKMethodCalculateNextPhaseVector(Vector3d[] basePhaseVector, Vector3d[] coefficient){
        Vector3d resultPositionComponent = basePhaseVector[0].dup().add(coefficient[0].dup().mul(deltaTime/2.0));
        Vector3d resultVelocityComponent = basePhaseVector[1].dup().add(coefficient[1].dup().mul(deltaTime/2.0));
        return new Vector3d[]{resultPositionComponent, resultVelocityComponent};
    }
    protected Vector3d[] RKMethodCalculateFinalPhaseVector(Vector3d[] basePhaseVector, Vector3d[] coefficient) {
        Vector3d resultPositionComponent = basePhaseVector[0].dup().add(coefficient[0].dup().mul(deltaTime));
        Vector3d resultVelocityComponent = basePhaseVector[1].dup().add(coefficient[1].dup().mul(deltaTime));
        return new Vector3d[]{resultPositionComponent, resultVelocityComponent};
    }

    // TODO [mess around] WITH SWITCH
    public void calculateNextPhaseVector(){
        if (isFrozen()) return;

        switch (coefficientCounter) {
            case 1 -> {
                setCurrentRKMPhaseVector(RKMethodCalculateNextPhaseVector(basePhaseVector, coefficient1));
                coefficientCounter = 2;
            }
            case 2 -> {
                setCurrentRKMPhaseVector((RKMethodCalculateNextPhaseVector(basePhaseVector, coefficient2)));
                coefficientCounter = 3;
            }
            case 3 -> {
                setCurrentRKMPhaseVector(RKMethodCalculateFinalPhaseVector(basePhaseVector, coefficient3));
                coefficientCounter = 4;
            }
            case 4 -> {}
            default -> System.out.println("RKMCounter error in calculateNextPhaseVector");
        }
    }
    public void calculateNextCoefficient(){
        if (isFrozen()) return;
        switch (coefficientCounter) {
            case 1 -> coefficient1 = RKMethodFunction(getCurrentRKMPhaseVector());
            case 2 -> coefficient2 = RKMethodFunction(getCurrentRKMPhaseVector());
            case 3 -> coefficient3 = RKMethodFunction(getCurrentRKMPhaseVector());
            case 4 -> {
                coefficient4 = RKMethodFunction(getCurrentRKMPhaseVector());
                coefficientCounter = 1;
            }
            default -> System.out.println("RKMCounter error in calculateNextCoefficient");
        }
    }

    protected Vector3d[] RKMCalculatePhaseDifferentialVector(){
        if (this.isFrozen()){
            return new Vector3d[]{new Vector3d(0,0,0), new Vector3d(0,0,0)};
        }
        Vector3d positionDifferentialComponent = new Vector3d(coefficient1[0].dup().add(
                coefficient2[0].dup().mul(2)).add(
                coefficient3[0].dup().mul(2)).add(
                coefficient4[0].dup()));
        positionDifferentialComponent.mul(deltaTime/6.0);

        Vector3d velocityDifferentialComponent = new Vector3d(coefficient1[1].dup().add(
                coefficient2[1].dup().mul(2)).add(
                coefficient3[1].dup().mul(2)).add(
                coefficient4[1].dup()));
        velocityDifferentialComponent.mul(deltaTime/6.0);

        return new Vector3d[]{positionDifferentialComponent, velocityDifferentialComponent};
    }

    public void RKMCalculatePositionDifferential(){
        Vector3d[] phaseDifferentialVector = RKMCalculatePhaseDifferentialVector();
        Vector3d velocity = phaseDifferentialVector[0];
        Vector3d acceleration = phaseDifferentialVector[1];
        velocityDifferential = acceleration.dup();
        positionDifferential = velocity.dup();
    }

    public void updatePosition() {
        setVelocity(getVelocity().add(velocityDifferential));
        setPosition(getPosition().add(positionDifferential));
        basePhaseVector = new Vector3d[]{this.getPosition(), this.getVelocity()};
    }

    protected abstract Vector3d RKMethodCalculateAcceleration(Vector3d position, Vector3d velocity);
    protected abstract Vector3d getAppliedForce(Vector3d position);
    protected Vector3d getCollisionForce() {
        return CollisionCalculator.getHookeCollisionForce(this);
    }

    @Override
    public void freeze(){
        frozen = true;
        setVelocity(new Vector3d());
    }
    @Override
    public void unfreeze(){frozen = false;}
    @Override
    public boolean isFrozen() {return frozen;}
}
