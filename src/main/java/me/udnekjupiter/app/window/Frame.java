package me.udnekjupiter.app.window;

import me.udnekjupiter.app.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Frame extends JFrame implements KeyListener {
    private final Controller controller;
    public Frame(){
        addKeyListener(this);
        controller = Controller.getInstance();
    }


    public void paint(Graphics graphics) {}
    public void repaint() {}
    public void update(Graphics g) {}

    @Override
    public void keyTyped(KeyEvent event) {}

    @Override
    public void keyPressed(KeyEvent event) {
        controller.keyChanges(event, true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        controller.keyChanges(event, false);
    }
}
