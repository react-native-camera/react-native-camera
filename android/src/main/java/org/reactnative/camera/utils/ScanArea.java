package org.reactnative.camera.utils;
import java.util.HashMap;

public class ScanArea {
  private Float ratio, adjustedScanY;
  private HashMap<String, Float> coord, cropParams;
  private HashMap<String, Integer> cam, cropArea;
  private Integer width, height, left, top, scanWidth, scanHeight, aspectRatio;
  private Boolean limitScanArea;

  public ScanArea(
      Boolean limitScanArea,
      HashMap<String, Float> coord,
      Integer width, 
      Integer height, 
      HashMap<String, Float> cropParams,
      HashMap<String, Integer> cam,
      Float ratio
  ) {
    this.limitScanArea = limitScanArea;
    this.coord = coord;
    this.width = width;
    this.height = height;
    this.cropParams = cropParams;
    this.ratio = ratio;
    this.cam = cam;
    setAdjustedScanY();
    this.left = (int) (coord.get("x") * width);
    this.top = (int) (this.adjustedScanY * height);
    setCropArea();
  }
 
  private void setCropArea() {
    float cropWidth = this.cropParams.get("width");
    int computed_width = (int) (cropWidth * this.width);
    float cropHeight = this.cropParams.get("height");
    Float area = cropHeight * cam.get("width");
    int computed_height = (int) ((area / this.aspectRatio) * this.height);
    this.cropArea = new HashMap<String, Integer>(); 
    this.cropArea.put("width", computed_width);
    this.cropArea.put("height", computed_height);
  }

  private void setAdjustedScanY() {
    this.aspectRatio = (int) (this.cam.get("height") / this.ratio);
    int camWidth = this.cam.get("width");
    Float widthMargin = (float) (aspectRatio - camWidth) / 2;
    Float computedArea = this.coord.get("y") * camWidth;
    this.adjustedScanY = (widthMargin + computedArea) / aspectRatio;
  }

  public void setLimitScanArea(Boolean value) { this.limitScanArea = value; }
  public void setCoord(Float x, Float y) {
    this.coord = new HashMap<String, Float>();
    this.coord.put("x", x);
    this.coord.put("y", y);
  }
  public void setCropParams(Float width, Float height) {
    this.cropParams = new HashMap<String, Float>();
    this.cropParams.put("width", width);
    this.cropParams.put("height", height);
  }
  public void setCam(Integer width, Integer height) {
    this.cam = new HashMap<String, Integer>();
    this.cam.put("width", width);
    this.cam.put("height", height);
  }

  public Boolean getLimitScanArea() { return this.limitScanArea; }
  public Integer getLeft() { return this.left; }
  public Integer getTop() { return this.top; }
  public Integer getWidth() { return this.width; }
  public Integer getHeight() { return this.height; }
  public Integer getCropArea(String dimension) { 
    return this.cropArea.get(dimension); 
  }
}
