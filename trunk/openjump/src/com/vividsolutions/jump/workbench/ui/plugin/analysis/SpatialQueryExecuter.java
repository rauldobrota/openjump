package com.vividsolutions.jump.workbench.ui.plugin.analysis;

import java.util.*;
import com.vividsolutions.jump.task.*;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jump.feature.*;

/**
 * Exceutes a spatial query with a given mask FeatureCollection, source FeatureCollection,
 * and predicate.
 * Ensures result does not contain duplicates.
 *
 * @author Martin Davis
 * @version 1.0
 */
public class SpatialQueryExecuter
{
  private FeatureCollection maskFC;
  private FeatureCollection sourceFC;
  private GeometryPredicate predicate;

  private boolean complementResult = false;

  private boolean isExceptionThrown = false;

  private Geometry geoms[] = new Geometry[2];
  private Set resultSet = new HashSet();

  public SpatialQueryExecuter(FeatureCollection maskFC, FeatureCollection sourceFC)
  {
    this.maskFC = maskFC;
    this.sourceFC = sourceFC;
  }

  private FeatureCollection getQueryFeatureCollection()
  {
    boolean buildIndex = false;
    if (maskFC.size() > 10) buildIndex = true;
    if (sourceFC.size() > 100) buildIndex = true;
    if (buildIndex) {
      return new IndexedFeatureCollection(sourceFC);
    }
    return sourceFC;
  }

  public boolean isExceptionThrown() { return isExceptionThrown; }

  public FeatureCollection getResultFC()
  {
    return new FeatureDataset(sourceFC.getFeatureSchema());
  }

  private boolean isInResult(Feature f)
  {
    return resultSet.contains(f);
  }

  public void setComplementResult(boolean complementResult)
  {
    this.complementResult = complementResult;
  }

  /**
   * Computes geomSrc.func(geomMask)
   *
   * @param monitor
   * @param func
   * @param params
   * @param resultFC
   */
  public void execute(TaskMonitor monitor,
                                     GeometryPredicate func,
                                     double[] params,
                                     FeatureCollection resultFC
                                     )
  {
    FeatureCollection queryFC = getQueryFeatureCollection();

    int total = maskFC.size();
    int count = 0;
    for (Iterator iMask = maskFC.iterator(); iMask.hasNext(); ) {

      monitor.report(count++, total, "features");
      if (monitor.isCancelRequested()) return;

      Feature fMask = (Feature) iMask.next();
      Geometry gMask = fMask.getGeometry();
      Envelope queryEnv = gMask.getEnvelopeInternal();

      // special hack for withinDistance
      if (func instanceof GeometryPredicate.WithinDistancePredicate) {
        queryEnv.expandBy(params[0]);
      }

      Collection queryResult = queryFC.query(queryEnv);
      for (Iterator iSrc = queryResult.iterator(); iSrc.hasNext(); ) {
        Feature fSrc = (Feature) iSrc.next();

        if (isInResult(fSrc))
          continue;

        Geometry gSrc = fSrc.getGeometry();
        geoms[0] = gSrc;
        geoms[1] = gMask;
        boolean isInResult = isTrue(func, gSrc, gMask, params);

        if (isInResult) {
          resultSet.add(fSrc);
        }
      }
    }

    if (complementResult) {
      loadComplement(resultFC);
    }
    else {
      loadResult(resultFC);
    }
  }

  private void loadComplement(FeatureCollection resultFC)
  {
    for (Iterator i = sourceFC.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      if (! resultSet.contains(f)) {
        resultFC.add(f);
      }
    }
  }

  private void loadResult(FeatureCollection resultFC)
  {
    for (Iterator i = resultSet.iterator(); i.hasNext(); ) {
      Feature f = (Feature) i.next();
      resultFC.add(f);
    }
  }

  private boolean isTrue(GeometryPredicate func, Geometry geom0, Geometry geom1, double[] params)
  {
    try {
      return func.isTrue(geom0, geom1, params);
    }
    catch (RuntimeException ex) {
      // simply eat exceptions and report them by returning null
      isExceptionThrown = true;
    }
    return false;

  }
}