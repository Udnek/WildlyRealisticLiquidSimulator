package me.udnekjupiter.physic.object;

import me.udnekjupiter.physic.collision.SphereCollider;
import org.realityforge.vecmath.Vector3d;

import static me.udnekjupiter.physic.engine.PrimitiveScenePhysicEngine.GRAVITATIONAL_ACCELERATION;

public class SphereObject extends RKMObject{
    public SphereObject(Vector3d position, double colliderRadius, double stiffness, double mass) {
        super(position);
        this.deltaTime = settings.deltaTime;
        this.mass = mass;
        this.decayCoefficient = settings.decayCoefficient;
        this.basePhaseVector = new Vector3d[]{position, new Vector3d()};
        collider = new SphereCollider(colliderRadius, stiffness, this);
    }

    public SphereObject(Vector3d position, double colliderRadius, double mass){
        this(position, colliderRadius, 10_000, mass);
    }

    @Override
    protected Vector3d getAppliedForce(Vector3d position){
        Vector3d appliedForce = new Vector3d();
        appliedForce.y += GRAVITATIONAL_ACCELERATION * mass;
        appliedForce.add(getCollisionForce());
        return appliedForce;
    }

    @Override
    protected Vector3d RKMethodCalculateAcceleration(Vector3d position, Vector3d velocity){
        Vector3d resultAcceleration = getAppliedForce(position);
        Vector3d decayValue = velocity.dup().mul(decayCoefficient);
        //resultAcceleration.sub(decayValue);
        resultAcceleration.div(mass);
        return resultAcceleration;
    }

    @Override
    public SphereCollider getCollider() {
        return (SphereCollider) collider;
    }

    @Override
    public boolean isCollisionIgnored(RKMObject object) {return false;}
}
