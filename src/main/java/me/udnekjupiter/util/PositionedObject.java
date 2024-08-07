package me.udnekjupiter.util;

import org.realityforge.vecmath.Vector3d;

public abstract class PositionedObject implements Positioned{
    protected Vector3d position;

    public PositionedObject(Vector3d position){
        this.position = position;
    }

    public Vector3d getPosition() {
        return position.dup();
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }
    public void move(Vector3d newPosition){
        setPosition(position.add(newPosition));
    }
    public void move(double x, double y, double z){
        setPosition(position.add(x, y, z));
    }
}
