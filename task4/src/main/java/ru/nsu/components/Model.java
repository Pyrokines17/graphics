package ru.nsu.components;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import ru.nsu.components.redactor.SplineLogic;
import ru.nsu.components.scene.Logic;
import ru.nsu.components.scene.View3D;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Model {
    private int N;
    private int K;
    private int M;
    private int M1;

    private double XBorder;
    private double YBorder;

    private ArrayList<Point2D> splinePoints;
    private ArrayList<Point2D> basePoints;
    private double[][][] points3D;
    private double[][][] basePoints3D;

    private View3D dependentPanel = null;

    private final ArrayList<Point2D> points = new ArrayList<>();

    public Model(JSONObject jsonObject) {
        loadFromJSON(jsonObject);
    }

    public void loadFromJSON(JSONObject obj) {
        Object NObj = obj.get("N");
        Object KObj = obj.get("K");
        Object MObj = obj.get("M");
        Object M1Obj = obj.get("M1");

        Object YBorderObj = obj.get("YBorder");
        Object XBorderObj = obj.get("XBorder");

        Object pointsObj = obj.get("points");

        if (NObj == null || KObj == null || MObj == null || M1Obj == null ||
                YBorderObj == null || XBorderObj == null || pointsObj == null) {
            throw new IllegalArgumentException("Invalid JSON data");
        }

        N = ((Long) NObj).intValue();
        K = ((Long) KObj).intValue();
        M = ((Long) MObj).intValue();
        M1 = ((Long) M1Obj).intValue();

        YBorder = (Double) YBorderObj;
        XBorder = (Double) XBorderObj;

        JSONArray pointsArray = (JSONArray) pointsObj;

        points.clear();
        for (Object pointObj : pointsArray) {
            JSONObject pointJson = (JSONObject) pointObj;
            double x = (Double) pointJson.get("x");
            double y = (Double) pointJson.get("y");
            points.add(new Point2D.Double(x, y));
        }

        List<ArrayList<Point2D>> pointsList = SplineLogic.getSplinePath(points, N);

        splinePoints = pointsList.get(0);
        basePoints = pointsList.get(1);

        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);

        if (dependentPanel != null) {
            Object angleX, angleY, nearPlane;

            nearPlane = obj.get("nearPlane");
            angleX = obj.get("angleX");
            angleY = obj.get("angleY");

            if (nearPlane == null || angleX == null || angleY == null) {
                updateDependentPanel();
                return;
            }

            double dNearPlane, dAngleX, dAngleY;

            dNearPlane = (Double) nearPlane;
            dAngleX = (Double) angleX;
            dAngleY = (Double) angleY;

            dependentPanel.setNearPlane(dNearPlane);
            dependentPanel.setFullX(dAngleX);
            dependentPanel.setFullY(dAngleY);

            dependentPanel.updateRotMatrix();
        }

        updateDependentPanel();
    }
    
    public void saveToJSON(JSONObject obj) {
        obj.put("N", N);
        obj.put("K", K);
        obj.put("M", M);
        obj.put("M1", M1);
        obj.put("YBorder", YBorder);
        obj.put("XBorder", XBorder);

        JSONArray pointsArray = new JSONArray();
        for (Point2D point : points) {
            JSONObject pointJson = new JSONObject();
            pointJson.put("x", point.getX());
            pointJson.put("y", point.getY());
            pointsArray.add(pointJson);
        }
        obj.put("points", pointsArray);

        if (dependentPanel != null) {
            obj.put("nearPlane", dependentPanel.getNearPlane());
            obj.put("angleX", dependentPanel.getFullX());
            obj.put("angleY", dependentPanel.getFullY());
        }
    }

    public Model() {
        N = 1;
        K = 4;
        M = 2;
        M1 = 1;

        XBorder = 10;
        YBorder = 10;

        for (int i = 0; i < 4; i++) {
            double x = Math.random() * (XBorder * 2) - XBorder;
            double y = Math.random() * (YBorder * 2) - YBorder;
            points.add(new Point2D.Double(x, y));
        }

        List<ArrayList<Point2D>> pointsList = SplineLogic.getSplinePath(points, N);

        splinePoints = pointsList.get(0);
        basePoints = pointsList.get(1);

        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public void setDependentPanel(View3D dependentPanel) {
        this.dependentPanel = dependentPanel;
    }

    public void changePoints() {
        int tempCount = points.size();

        if (tempCount < K) {
            for (int i = 0; i < K - tempCount; ++i) {
                double x = Math.random() * (XBorder * 2) - XBorder;
                double y = Math.random() * (YBorder * 2) - YBorder;
                points.add(new Point2D.Double(x, y));
            }
        } else if (tempCount > K) {
            points.subList(K, tempCount).clear();
        }

        List<ArrayList<Point2D>> pointsList = SplineLogic.getSplinePath(points, N);

        splinePoints = pointsList.get(0);
        basePoints = pointsList.get(1);

        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public ArrayList<Point2D> getBasePoints() {
        return basePoints;
    }

    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;

        List<ArrayList<Point2D>> pointsList = SplineLogic.getSplinePath(points, N);

        splinePoints = pointsList.get(0);
        basePoints = pointsList.get(1);

        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public int getK() {
        return K;
    }

    public void setK(int K) {
        this.K = K;
    }

    public int getM() {
        return M;
    }

    public void setM(int M) {
        this.M = M;

        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public int getM1() {
        return M1;
    }

    public void setM1(int M1) {
        this.M1 = M1;
        updateDependentPanel();
    }

    public ArrayList<Point2D> getPoints() {
        return points;
    }

    public ArrayList<Point2D> getSplinePoints() {
        return splinePoints;
    }

    public double[][][] getPoints3D() {
        return points3D;
    }

    public double[][][] getBasePoints3D() {
        return basePoints3D;
    }

    public void recalculateSplinePoints() {
        List<ArrayList<Point2D>> pointsList = SplineLogic.getSplinePath(points, N);
        splinePoints = pointsList.get(0);
        basePoints = pointsList.get(1);
        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public void recalculatePoints3D() {
        points3D = Logic.get3DPoints(splinePoints, M);
        basePoints3D = Logic.get3DPoints(basePoints, M);
        updateDependentPanel();
    }

    public double getXBorder() {
        return XBorder;
    }

    public double getYBorder() {
        return YBorder;
    }

    private void updateDependentPanel() {
        if (dependentPanel != null) {
            dependentPanel.revalidate();
            dependentPanel.repaint();
        }
    }
}
