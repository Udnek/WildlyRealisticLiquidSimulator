package me.udnek.app.controller;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controller {

    private List<InputKey> pressedKeys = Collections.synchronizedList(new ArrayList<>());
    private Point mousePreviousPosition;
    private InputKey currentMouseKey;
    private Point mouseRelativePosition;

    private ControllerHandler controllerHandler;
    public Controller(ControllerHandler controllerHandler){
        this.controllerHandler = controllerHandler;
    }

    public void keyChanges(KeyEvent event, boolean nowPressed){
        InputKey key = InputKey.getByCode(event.getKeyCode());
        if (key == null) return;
        controllerHandler.keyEvent(key, nowPressed);
        if (key.longPress){
            boolean pressed = pressedKeys.contains(key);
            if (pressed == nowPressed) return;
            if (pressed) pressedKeys.remove(key);
            else pressedKeys.add(key);
        }
    }

    public List<InputKey> getPressedKeys(){return pressedKeys;}
    public void mouseChanges(MouseEvent event, boolean pressed){
        if (!mouseIsPressed() && !pressed) return;
        if (mouseIsPressed() && pressed) return;

        InputKey key = InputKey.getByCode(event.getButton());

        controllerHandler.keyEvent(key, pressed);

        // PRESSING
        if (pressed){
            currentMouseKey = key;
            mousePreviousPosition = getMousePosition();
        }
        // UN PRESSING
        else {
            currentMouseKey = null;
        }
    }

    public boolean mouseIsPressed(){
        return currentMouseKey != null;
    }

    public InputKey getMouseKey() {
        return currentMouseKey;
    }

    public void updateMouse(){
        mousePreviousPosition = getMousePosition();
    }

    public void setMouseRelativePosition(Point position){
        mouseRelativePosition = position;
    }

    public Point getMouseRelativePosition() {
        return mouseRelativePosition;
    }

    private Point getMousePosition(){
        return MouseInfo.getPointerInfo().getLocation();
    }

    public Point getMouseDifference(){
        Point newLocation = getMousePosition();
        return new Point(
                newLocation.x - mousePreviousPosition.x,
                newLocation.y - mousePreviousPosition.y
        );
    }
}
