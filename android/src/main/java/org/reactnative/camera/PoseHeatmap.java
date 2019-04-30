package org.reactnative.camera;

public class PoseHeatmap {
    private float[][][] mHeatmap;
    public PoseHeatmap(float[][][] heatmap){
        mHeatmap = heatmap;
    }
    public float[][][] getHeatmap() { return mHeatmap;}
}
