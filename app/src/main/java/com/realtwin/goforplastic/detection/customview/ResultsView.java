
package com.realtwin.goforplastic.detection.customview;

import com.realtwin.goforplastic.detection.tflite.Classifier.*;

import java.util.*;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
