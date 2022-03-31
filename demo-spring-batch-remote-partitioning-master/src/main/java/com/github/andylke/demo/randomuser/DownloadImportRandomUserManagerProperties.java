package com.github.andylke.demo.randomuser;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "download-import-random-user-manager")
public class DownloadImportRandomUserManagerProperties {

  private int totalPage;

  private int gridSize;

  public int getTotalPage() {
    return totalPage;
  }

  public void setTotalPage(int totalPage) {
    this.totalPage = totalPage;
  }

  public int getGridSize() {
    return gridSize;
  }

  public void setGridSize(int gridSize) {
    this.gridSize = gridSize;
  }
}
