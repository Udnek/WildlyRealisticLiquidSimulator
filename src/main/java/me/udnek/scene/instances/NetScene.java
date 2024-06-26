package me.udnek.scene.instances;

import me.jupiter.net.CellularNet;
import me.jupiter.object.vertex.NetStaticVertex;
import me.jupiter.object.vertex.NetVertex;
import me.udnek.object.DoubleSpringObject;
import me.udnek.object.SceneObject;
import me.udnek.object.SpringObject;
import me.udnek.object.VertexObject;
import me.udnek.object.light.LightSource;
import me.udnek.object.light.PointLight;
import me.udnek.scene.Camera;
import me.udnek.scene.Scene;
import org.realityforge.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class NetScene extends Scene {
    protected CellularNet net;
    protected List<VertexObject> vertices;
    protected List<SpringObject> springs;
    public NetScene(){}


    @Override
    protected List<? extends SceneObject> initSceneObjects() {
        vertices = new ArrayList<>();
        springs = new ArrayList<>();
        //List<NetVertex> addedNetVertices = new ArrayList<>();
        HashMap<NetVertex, List<NetVertex>> addedNeighbours = new HashMap<>();


        for (int x = 0; x < net.getSizeX(); x++) {
            for (int z = 0; z < net.getSizeZ(); z++) {

                NetVertex netVertex = net.getVertex(x, z);

                if (netVertex == null) continue;
                if (netVertex instanceof NetStaticVertex) continue;

                if (addedNeighbours.containsKey(netVertex)) continue;


                VertexObject vertexObject = new VertexObject(new Vector3d(netVertex.getPosition()), netVertex);
                vertices.add(vertexObject);
                List<NetVertex> addedNeighbourVertices = addedNeighbours.getOrDefault(netVertex, null);
                if (addedNeighbourVertices == null) {
                    addedNeighbourVertices = new ArrayList<>();
                    addedNeighbours.put(netVertex, addedNeighbourVertices);
                }

                // TODO: 5/31/2024 FIX AND OPTIMIZE

                for (NetVertex neighbour : netVertex.getNeighbors()) {
                    //if (addedNeighbourVertices.contains(neighbour)) continue;
                    if (addedNeighbours.containsKey(neighbour)) continue;



                    VertexObject neighbourObject = new VertexObject(new Vector3d(neighbour.getPosition()), neighbour);
                    vertices.add(neighbourObject);
                    springs.add(new DoubleSpringObject(new Vector3d(), vertexObject, neighbourObject));

                }
            }
        }

        List<SceneObject> sceneObjects = new ArrayList<>();
        sceneObjects.addAll(vertices);
        sceneObjects.addAll(springs);
        return sceneObjects;
    }

    public void synchroniseObjects(){
        for (VertexObject vertex : vertices) {
            vertex.update();
        }
        for (SpringObject springObject : springs) {
            springObject.update();
        }
    }

    @Override
    protected Camera initCamera() {
        Camera camera = new Camera(new Vector3d(2, 2, -1.5));
        camera.rotatePitch(35);
        return camera;
    }

    @Override
    protected LightSource initLightSource() {
        return new PointLight(new Vector3d(0, 2, 0));
    }
}

