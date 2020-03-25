package org.reactnative.camera.utils;

public class ScanArea {
  private Float scanX, scanY, scanAreaWidth, scanAreaHeight, ratio, adjustedScanY;
  private Integer width, height, cameraWidth, cameraHeight, left, top, scanWidth, scanHeight;
  private Boolean limitScanArea;

  public ScanArea(
      Boolean limitScanArea,
      Integer width, 
      Integer height, 
      Float scanX, 
      Float scanY, 
      Float scanAreaWidth, 
      Float scanAreaHeight, 
      Integer cameraWidth, 
      Integer cameraHeight, 
      Float ratio
  ) {
    // TODO: Group all parameters in size, coordinates, area, camera ..
    this.limitScanArea = limitScanArea;
    this.width = width;
    this.height = height;
    this.scanX = scanX;
    this.scanY = scanY;
    this.scanAreaHeight = scanAreaHeight;
    this.scanAreaWidth = scanAreaWidth;
    this.cameraWidth = cameraWidth;
    this.cameraHeight = cameraHeight;
    Integer aspectRatio = (int) (cameraHeight / ratio);
    Float widthMargin = (float) (aspectRatio - cameraWidth) / 2;
    Float computedArea = this.scanY * cameraWidth;
    this.adjustedScanY = (widthMargin + computedArea) / aspectRatio;
    this.left = (int) (scanX * width);
    this.top = (int) (this.adjustedScanY * height);
    this.scanWidth = (int) (scanAreaWidth * width);
    Float area = scanAreaHeight * cameraWidth;
    this.scanHeight = (int) ((area / aspectRatio) * height);
  }
 
  // TODO: Metaprogramming loop to remove getters/setters
  public void setLimitScanArea(Boolean value) { this.limitScanArea = value; }
  public void setScanAreaX(Float value) { this.scanX = value; }
  public void setScanAreaY(Float value) { this.scanY = value; }
  public void setScanAreaWidth(Float value) { this.scanAreaWidth = value; }
  public void setScanAreaHeight(Float value) { this.scanAreaHeight = value; }
  public void setCameraWidth(Integer value) { this.cameraWidth = value; }
  public void setCameraHeight(Integer value) { this.cameraHeight = value; }
  
  public Boolean getLimitScanArea() { return this.limitScanArea; }
  public Float getAdjustedScanY() { return this.adjustedScanY; }
  public Integer getLeft() { return this.left; }
  public Integer getTop() { return this.top; }
  public Float getScanAreaWidth() { return this.scanAreaWidth; }
  public Float getScanAreaHeight() { return this.scanAreaHeight; }
  public Integer getScanWidth() { return this.scanWidth; }
  public Integer getScanHeight() { return this.scanHeight; }
  public Integer getWidth() { return this.width; }
  public Integer getHeight() { return this.height; }
  public Float getScanX() { return this.scanX; }
  public Float getScanY() { return this.scanY; }
  public Integer getCameraWidth() { return this.cameraWidth; }
  public Integer getCameraHeight() { return this.cameraHeight; }
}
