
package me.udnekjupiter.graphic.scene;

import me.udnekjupiter.graphic.Camera;
import me.udnekjupiter.graphic.object.DoubleSpringObject;
import me.udnekjupiter.graphic.object.GraphicObject;
import me.udnekjupiter.graphic.object.SpringObject;
import me.udnekjupiter.graphic.object.VertexObject;
import me.udnekjupiter.graphic.object.light.LightSource;
import me.udnekjupiter.graphic.object.light.PointLight;
import me.udnekjupiter.physic.net.CellularNet;
import me.udnekjupiter.physic.object.vertex.NetStaticVertex;
import me.udnekjupiter.physic.object.vertex.NetVertex;
import me.udnekjupiter.physic.scene.NetPhysicScene;
import org.realityforge.vecmath.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetGraphicScene extends GraphicScene3d {
    private List<VertexObject> vertices;
    private List<SpringObject> springs;
    private NetPhysicScene netPhysicScene;
    public NetGraphicScene(NetPhysicScene netPhysicScene){
        this.netPhysicScene = netPhysicScene;
    }



    @Override
    protected List<GraphicObject> initializeSceneObjects() {
        vertices = new ArrayList<>();
        springs = new ArrayList<>();

        HashMap<NetVertex, List<NetVertex>> addedNeighbours = new HashMap<>();

        CellularNet net = netPhysicScene.getNet();

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
                    if (addedNeighbours.containsKey(neighbour)) continue;

                    VertexObject neighbourObject = new VertexObject(new Vector3d(neighbour.getPosition()), neighbour);
                    vertices.add(neighbourObject);
                    springs.add(new DoubleSpringObject(new Vector3d(), vertexObject, neighbourObject));

                }
            }
        }

        List<GraphicObject> graphicObjects = new ArrayList<>();
        graphicObjects.addAll(vertices);
        graphicObjects.addAll(springs);
        return graphicObjects;
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
    protected Camera initializeCamera() {
        Camera camera = new Camera(new Vector3d(2, 2, -1.5));
        camera.rotatePitch(35);
        return camera;
    }

    @Override
    protected LightSource initializeLightSource() {
        return new PointLight(new Vector3d(0, 2, 0));
    }

    @Override
    public void beforeFrameUpdate(int width, int height) {
        super.beforeFrameUpdate(width, height);
        synchroniseObjects();
    }
}
