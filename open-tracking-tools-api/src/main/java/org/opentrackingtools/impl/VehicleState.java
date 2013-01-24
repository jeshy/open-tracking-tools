package org.opentrackingtools.impl;

import gov.sandia.cognition.math.matrix.AbstractVector;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.statistics.ComputableDistribution;
import gov.sandia.cognition.statistics.ProbabilityFunction;
import gov.sandia.cognition.util.AbstractCloneableSerializable;

import java.util.ArrayList;
import java.util.Random;

import jj2000.j2k.NotImplementedError;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opentrackingtools.GpsObservation;
import org.opentrackingtools.graph.InferenceGraph;
import org.opentrackingtools.graph.paths.impl.InferredPathPrediction;
import org.opentrackingtools.graph.paths.states.PathStateBelief;
import org.opentrackingtools.statistics.distributions.impl.OnOffEdgeTransDirMulti;
import org.opentrackingtools.statistics.filters.vehicles.road.impl.AbstractRoadTrackingFilter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;

/**
 * This class represents the state of a vehicle, which is made up of the
 * vehicles location, whether it is on an edge, which path it took from its
 * previous location on an edge, and the distributions that determine these.
 * 
 * @author bwillard
 * 
 */
public class VehicleState implements
    ComputableDistribution<GpsObservation>,
    Comparable<VehicleState> {

  public static class PDF extends VehicleState implements
      ProbabilityFunction<GpsObservation> {

    private static final long serialVersionUID =
        879217079360170446L;

    public PDF(VehicleState state) {
      super(state);
    }

    @Override
    public PDF clone() {
      return (PDF) super.clone();
    }

    @Override
    public Double evaluate(GpsObservation input) {
      return Math.exp(logEvaluate(input));
    }

    @Override
    public VehicleState.PDF getProbabilityFunction() {
      return this;
    }

    @Override
    public double logEvaluate(GpsObservation input) {
      double logLikelihood = 0d;

      /*
       * Movement.
       */
      logLikelihood +=
          this.belief.logLikelihood(
              input.getProjectedPoint(),
              this.getMovementFilter());
      //          this.getMovementFilter().logLikelihood(
      //              input.getProjectedPoint(), this.belief);

      return logLikelihood;
    }

    @Override
    public GpsObservation sample(Random random) {
      throw new NotImplementedError();
    }

    @Override
    public ArrayList<GpsObservation> sample(Random random,
      int numSamples) {
      throw new NotImplementedError();
    }

  }

  public static class VehicleStateInitialParameters extends
      AbstractCloneableSerializable implements Comparable<VehicleStateInitialParameters> {
    private static final long serialVersionUID =
        3613725475525876941L;
    private final Vector obsCov;
    private final Vector onRoadStateCov;
    private final Vector offRoadStateCov;
    private final Vector offTransitionProbs;
    private final Vector onTransitionProbs;
    private final long seed;
    private final int numParticles;
    private final String filterTypeName;
    private final int initialObsFreq;
    private final int obsCovDof;
    private final int onRoadCovDof;
    private final int offRoadCovDof;

    public VehicleStateInitialParameters(Vector obsCov,
      int obsCovDof, Vector onRoadStateCov,
      int onRoadCovDof, Vector offRoadStateCov,
      int offRoadCovDof, Vector offProbs, Vector onProbs,
      String filterTypeName, int numParticles,
      int initialObsFreq, long seed) {
      this.obsCovDof = obsCovDof;
      this.onRoadCovDof = onRoadCovDof;
      this.offRoadCovDof = offRoadCovDof;
      this.numParticles = numParticles;
      this.obsCov = obsCov;
      this.onRoadStateCov = onRoadStateCov;
      this.offRoadStateCov = offRoadStateCov;
      this.offTransitionProbs = offProbs;
      this.onTransitionProbs = onProbs;
      this.seed = seed;
      this.filterTypeName = filterTypeName;
      this.initialObsFreq = initialObsFreq;
    }

    @Override
    public VehicleStateInitialParameters clone() {
      final VehicleStateInitialParameters clone =
          (VehicleStateInitialParameters) super.clone();
      // TODO
      return clone;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final VehicleStateInitialParameters other =
          (VehicleStateInitialParameters) obj;
      if (filterTypeName == null) {
        if (other.filterTypeName != null) {
          return false;
        }
      } else if (!filterTypeName
          .equals(other.filterTypeName)) {
        return false;
      }
      if (initialObsFreq != other.initialObsFreq) {
        return false;
      }
      if (numParticles != other.numParticles) {
        return false;
      }
      if (obsCov == null) {
        if (other.obsCov != null) {
          return false;
        }
      } else if (!obsCov.equals(other.obsCov)) {
        return false;
      }
      if (obsCovDof != other.obsCovDof) {
        return false;
      }
      if (offRoadCovDof != other.offRoadCovDof) {
        return false;
      }
      if (offRoadStateCov == null) {
        if (other.offRoadStateCov != null) {
          return false;
        }
      } else if (!offRoadStateCov
          .equals(other.offRoadStateCov)) {
        return false;
      }
      if (offTransitionProbs == null) {
        if (other.offTransitionProbs != null) {
          return false;
        }
      } else if (!offTransitionProbs
          .equals(other.offTransitionProbs)) {
        return false;
      }
      if (onRoadCovDof != other.onRoadCovDof) {
        return false;
      }
      if (onRoadStateCov == null) {
        if (other.onRoadStateCov != null) {
          return false;
        }
      } else if (!onRoadStateCov
          .equals(other.onRoadStateCov)) {
        return false;
      }
      if (onTransitionProbs == null) {
        if (other.onTransitionProbs != null) {
          return false;
        }
      } else if (!onTransitionProbs
          .equals(other.onTransitionProbs)) {
        return false;
      }
      if (seed != other.seed) {
        return false;
      }
      return true;
    }

    public String getFilterTypeName() {
      return filterTypeName;
    }

    public int getInitialObsFreq() {
      return this.initialObsFreq;
    }

    public int getNumParticles() {
      return numParticles;
    }

    public Vector getObsCov() {
      return obsCov;
    }

    public int getObsCovDof() {
      return this.obsCovDof;
    }

    public int getOffRoadCovDof() {
      return offRoadCovDof;
    }

    public Vector getOffRoadStateCov() {
      return offRoadStateCov;
    }

    public Vector getOffTransitionProbs() {
      return offTransitionProbs;
    }

    public int getOnRoadCovDof() {
      return onRoadCovDof;
    }

    public Vector getOnRoadStateCov() {
      return onRoadStateCov;
    }

    public Vector getOnTransitionProbs() {
      return onTransitionProbs;
    }

    public long getSeed() {
      return seed;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result =
          prime
              * result
              + ((filterTypeName == null) ? 0
                  : filterTypeName.hashCode());
      result = prime * result + initialObsFreq;
      result = prime * result + numParticles;
      result =
          prime * result
              + ((obsCov == null) ? 0 : obsCov.hashCode());
      result = prime * result + obsCovDof;
      result = prime * result + offRoadCovDof;
      result =
          prime
              * result
              + ((offRoadStateCov == null) ? 0
                  : offRoadStateCov.hashCode());
      result =
          prime
              * result
              + ((offTransitionProbs == null) ? 0
                  : offTransitionProbs.hashCode());
      result = prime * result + onRoadCovDof;
      result =
          prime
              * result
              + ((onRoadStateCov == null) ? 0
                  : onRoadStateCov.hashCode());
      result =
          prime
              * result
              + ((onTransitionProbs == null) ? 0
                  : onTransitionProbs.hashCode());
      result =
          prime * result + (int) (seed ^ (seed >>> 32));
      return result;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder();
      builder
          .append("VehicleStateInitialParameters [obsCov=")
          .append(obsCov).append(", onRoadStateCov=")
          .append(onRoadStateCov)
          .append(", offRoadStateCov=")
          .append(offRoadStateCov)
          .append(", offTransitionProbs=")
          .append(offTransitionProbs)
          .append(", onTransitionProbs=")
          .append(onTransitionProbs).append(", seed=")
          .append(seed).append(", numParticles=")
          .append(numParticles).append(", filterTypeName=")
          .append(filterTypeName)
          .append(", initialObsFreq=")
          .append(initialObsFreq).append(", obsCovDof=")
          .append(obsCovDof).append(", onRoadCovDof=")
          .append(onRoadCovDof).append(", offRoadCovDof=")
          .append(offRoadCovDof).append("]");
      return builder.toString();
    }

    @Override
    public int compareTo(VehicleStateInitialParameters o) {
     return new CompareToBuilder()
       .append(this.filterTypeName, o.filterTypeName)
       .append(this.initialObsFreq, o.initialObsFreq)
       .append(this.numParticles, o.numParticles)
       .append(this.obsCov.toArray(), o.obsCov.toArray())
       .append(this.obsCovDof, o.obsCovDof)
       .append(this.offRoadStateCov.toArray(), o.offRoadStateCov.toArray())
       .append(this.offRoadCovDof, o.offRoadCovDof)
       .append(this.onRoadStateCov.toArray(), o.onRoadStateCov.toArray())
       .append(this.onRoadCovDof, o.onRoadCovDof)
       .append(this.initialObsFreq, o.initialObsFreq)
       .append(this.onTransitionProbs.toArray(), o.onTransitionProbs.toArray())
       .append(this.offTransitionProbs.toArray(), o.offTransitionProbs.toArray())
       .toComparison();
    }
  }

  private static final long serialVersionUID =
      3229140254421801273L;

  /*
   * These members represent the state/parameter samples/sufficient statistics.
   */
  private final AbstractRoadTrackingFilter<?> movementFilter;

  /**
   * This could be the 4D ground-coordinates dist. for free motion, or the 2D
   * road-coordinates, either way the tracking filter will check. Also, this
   * could be the prior or prior predictive distribution.
   */
  protected final PathStateBelief belief;

  /*-
   * Edge transition priors 
   * 1. edge off 
   * 2. edge on 
   * 3. edges transitions to others (one for all)
   * edges
   */
  protected final OnOffEdgeTransDirMulti edgeTransitionDist;
  private final GpsObservation observationFactory;
  private VehicleState parentState = null;

  private final Double distanceFromPreviousState;

  private final InferenceGraph graph;

  // private final int initialHashCode;
  // private final int edgeInitialHashCode;
  // private final int obsInitialHashCode;
  // private final int transInitialHashCode;
  // private final int beliefInitialHashCode;

  private int hash = 0;

  public VehicleState(InferenceGraph inferredGraph,
    GpsObservation observationFactory,
    AbstractRoadTrackingFilter<?> updatedFilter,
    PathStateBelief belief,
    OnOffEdgeTransDirMulti edgeTransitionDist,
    VehicleState parentState) {

    Preconditions.checkNotNull(inferredGraph);
    Preconditions.checkNotNull(observationFactory);
    Preconditions.checkNotNull(updatedFilter);
    Preconditions.checkNotNull(belief);

    this.observationFactory = observationFactory;
    this.movementFilter = updatedFilter;
    this.belief = belief.clone();
    this.graph = inferredGraph;

    /*
     * Check that the state's location corresponds
     * to the last edge.
     */
    Preconditions
        .checkState(!belief.isOnRoad()
            || belief.getEdge().equals(
                Iterables.getLast(belief.getPath()
                    .getEdges())));
    /*
     * This is the constructor used when creating transition states, so this is
     * where we'll need to reset the distance measures
     */
    if (parentState != null) {
      final Vector dist =
          this.belief.minus(parentState.belief);
      if (dist.getDimensionality() == 2) {
        this.distanceFromPreviousState =
            AbstractRoadTrackingFilter.getOr().times(dist)
                .norm2();
      } else {
        this.distanceFromPreviousState =
            AbstractRoadTrackingFilter.getOg().times(dist)
                .norm2();
      }
    } else {
      this.distanceFromPreviousState = null;
    }

    this.edgeTransitionDist = edgeTransitionDist;

    this.parentState = parentState;
    /*
     * Reset the parent's parent state so that we don't keep these objects
     * forever.
     */
    // state.parentState = null;

    final double timeDiff;
    if (observationFactory.getPreviousObservation() != null) {
      timeDiff =
          (observationFactory.getTimestamp().getTime() - observationFactory
              .getPreviousObservation().getTimestamp()
              .getTime()) / 1000d;
    } else {
      timeDiff = 30d;
    }
    this.movementFilter.setCurrentTimeDiff(timeDiff);

    // DEBUG
    // this.initialHashCode = this.hashCode();
    // this.edgeInitialHashCode = this.edge.hashCode();
    // this.transInitialHashCode = this.edgeTransitionDist.hashCode();
    // this.beliefInitialHashCode =
    // Arrays.hashCode(((DenseVector)this.initialBelief.convertToVector()).getArray());
    // this.obsInitialHashCode = this.observation.hashCode();
  }

  public VehicleState(VehicleState other) {
    this.graph = other.graph;
    this.movementFilter = other.movementFilter.clone();
    this.belief = other.belief.clone();
    this.edgeTransitionDist =
        other.edgeTransitionDist.clone();
    this.observationFactory = other.observationFactory;
    this.distanceFromPreviousState =
        other.distanceFromPreviousState;
    this.parentState = other.parentState;

    // DEBUG
    // this.initialHashCode = this.hashCode();
    // this.edgeInitialHashCode = this.edge.hashCode();
    // this.transInitialHashCode = this.edgeTransitionDist.hashCode();
    // this.beliefInitialHashCode =
    // Arrays.hashCode(((DenseVector)this.initialBelief.convertToVector()).getArray());
    // this.obsInitialHashCode = this.observation.hashCode();
  }

  @Override
  public VehicleState clone() {
    return new VehicleState(this);
  }

  @Override
  public int compareTo(VehicleState arg0) {
    return oneStateCompareTo(this, arg0);
  }

  @Override
  public boolean equals(Object obj) {
    /*
     * We do this to avoid evaluating every parent down the chain.
     */
    if (!oneStateEquals(this, obj))
      return false;

    final VehicleState other = (VehicleState) obj;
    if (parentState == null) {
      if (other.parentState != null) {
        return false;
      }
    } else if (!oneStateEquals(parentState,
        other.parentState)) {
      return false;
    }

    return true;
  }

  public PathStateBelief getBelief() {
    return belief;
  }

  public Double getDistanceFromPreviousState() {
    return distanceFromPreviousState;
  }

  public OnOffEdgeTransDirMulti getEdgeTransitionDist() {
    return edgeTransitionDist;
  }

  public InferenceGraph getGraph() {
    return graph;
  }

  /**
   * Returns ground-coordinate mean location
   * 
   * @return
   */
  public Vector getMeanLocation() {
    final Vector v = belief.getGroundState();
    return AbstractRoadTrackingFilter.getOg().times(v);
  }

  public AbstractRoadTrackingFilter<?> getMovementFilter() {
    return movementFilter;
  }

  public GpsObservation getObservation() {
    return observationFactory;
  }

  public VehicleState getParentState() {
    return parentState;
  }

  @Override
  public VehicleState.PDF getProbabilityFunction() {
    return new VehicleState.PDF(this);
  }

  @Override
  public int hashCode() {
    /*
     * We do this to avoid evaluating every parent down the chain.
     */
    if (hash != 0) {
      return hash;
    } else {
      final int prime = 31;
      int result = 1;
      result = prime * result + oneStateHashCode(this);
      if (this.parentState != null)
        result =
            prime * result
                + oneStateHashCode(this.parentState);
      hash = result;
      return result;
    }
  }

  @Override
  public GpsObservation sample(Random random) {
    throw new NotImplementedError();
  }

  @Override
  public ArrayList<GpsObservation> sample(Random random,
    int numSamples) {
    throw new NotImplementedError();
  }

  public void setParentState(VehicleState parentState) {
    this.parentState = parentState;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("VehicleState [belief=").append(belief)
        .append(", observation=")
        .append(observationFactory.getTimestamp()).append("]");
    return builder.toString();
  }

  public static Vector getNonVelocityVector(Vector vector) {
    final Vector res;
    if (vector.getDimensionality() == 4)
      res =
          AbstractRoadTrackingFilter.getOg().times(vector);
    else
      res =
          AbstractRoadTrackingFilter.getOr().times(vector);
    return res;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  private static int oneStateCompareTo(VehicleState t,
    VehicleState o) {
    if (t == o)
      return 0;

    if (t == null) {
      if (o != null)
        return -1;
      else
        return 0;
    } else if (o == null) {
      return 1;
    }

    final CompareToBuilder comparator =
        new CompareToBuilder();
    comparator.append(t.belief, o.belief);
    comparator.append(t.getObservation(),
        o.getObservation());
    comparator.append(t.edgeTransitionDist,
        o.edgeTransitionDist);

    return comparator.toComparison();
  }

  protected static boolean oneStateEquals(Object thisObj,
    Object obj) {
    if (thisObj == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (thisObj.getClass() != obj.getClass()) {
      return false;
    }
    final VehicleState thisState = (VehicleState) thisObj;
    final VehicleState other = (VehicleState) obj;
    if (thisState.belief == null) {
      if (other.belief != null) {
        return false;
      }
    } else if (!thisState.belief.equals(other.belief)) {
      return false;
    }
    if (thisState.edgeTransitionDist == null) {
      if (other.edgeTransitionDist != null) {
        return false;
      }
    } else if (!thisState.edgeTransitionDist
        .equals(other.edgeTransitionDist)) {
      return false;
    }
    if (thisState.movementFilter == null) {
      if (other.movementFilter != null) {
        return false;
      }
    } else if (!thisState.movementFilter
        .equals(other.movementFilter)) {
      return false;
    }
    if (thisState.observationFactory == null) {
      if (other.observationFactory != null) {
        return false;
      }
    } else if (!thisState.observationFactory
        .equals(other.observationFactory)) {
      return false;
    }
    return true;
  }

  protected static int oneStateHashCode(VehicleState state) {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((state.belief == null) ? 0 : state.belief
                .hashCode());
    result =
        prime
            * result
            + ((state.edgeTransitionDist == null) ? 0
                : state.edgeTransitionDist.hashCode());
    result =
        prime
            * result
            + ((state.movementFilter == null) ? 0
                : state.movementFilter.hashCode());
    result =
        prime
            * result
            + ((state.observationFactory == null) ? 0
                : state.observationFactory.hashCode());
    return result;
  }

}
