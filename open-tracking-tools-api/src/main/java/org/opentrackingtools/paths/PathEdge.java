package org.opentrackingtools.paths;

import javax.annotation.Nonnull;

import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.util.AbstractCloneableSerializable;

import org.opentrackingtools.graph.InferenceGraphEdge;
import org.opentrackingtools.graph.InferenceGraphSegment;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

public class PathEdge extends
    AbstractCloneableSerializable implements Comparable<PathEdge> {

  private static final long serialVersionUID = 2615199504616160384L;

  protected Double distToStartOfEdge = null;
  protected InferenceGraphEdge edge = null;
  protected Boolean isBackward = null;
  protected InferenceGraphSegment segment = null;
  protected Double distToFromStartOfGraphEdge = null;
  protected LineSegment line = null;
  
  public final static PathEdge nullPathEdge = new PathEdge();

  protected PathEdge() {
    this.edge = InferenceGraphEdge.nullGraphEdge;
  }

  public Double getDistToFromStartOfGraphEdge() {
    return distToFromStartOfGraphEdge;
  }

  public PathEdge(InferenceGraphSegment segment, double startDistance, boolean isBackward) {
    Preconditions.checkArgument(!segment.getParentEdge().isNullEdge());
    Preconditions.checkState((isBackward != Boolean.TRUE)
        || startDistance <= 0d);
    this.segment = segment;
    this.line = new LineSegment(segment.getLine());
    if (isBackward)
      this.line.reverse();
    this.edge = segment.getParentEdge();
    this.distToFromStartOfGraphEdge = segment.getParentEdge().getLengthLocationMap().getLength(
        segment.getStartIndex());
    this.distToStartOfEdge = startDistance;
    this.isBackward = isBackward;
  }

  @Override
  public PathEdge clone() {
    final PathEdge clone = (PathEdge) super.clone();
    clone.distToStartOfEdge = this.distToStartOfEdge;
    clone.edge = this.edge;
    clone.isBackward = this.isBackward;
    clone.segment = this.segment;
    return clone;
  }

  @Override
  public int compareTo(PathEdge o) {
    return ComparisonChain
        .start()
        .compare(this.edge, o.edge)
        .compare(this.segment, o.segment)
        .compare(this.isBackward(), o.isBackward,
            Ordering.natural().nullsLast())
        .compare(this.distToStartOfEdge, o.distToStartOfEdge,
            Ordering.natural().nullsLast()).result();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final PathEdge other = (PathEdge) obj;
    if (this.distToStartOfEdge == null) {
      if (other.distToStartOfEdge != null) {
        return false;
      }
    } else if (!this.distToStartOfEdge
        .equals(other.distToStartOfEdge)) {
      return false;
    }
    if (this.segment == null) {
      if (other.segment != null) {
        return false;
      }
    } else if (!this.segment.equals(other.segment)) {
      return false;
    }
    if (this.edge == null) {
      if (other.edge != null) {
        return false;
      }
    } else if (!this.edge.equals(other.edge)) {
      return false;
    }
    if (this.isBackward == null) {
      if (other.isBackward != null) {
        return false;
      }
    } else if (!this.isBackward.equals(other.isBackward)) {
      return false;
    }
    return true;
  }
  
  public Vector clampToEdge(Vector state) {
    Preconditions.checkState(!this.isNullEdge());
    Preconditions.checkArgument(state.getDimensionality() == 2);
    
    final Vector newState = state.clone();
    final double direction = this.isBackward ? -1d : 1d;
    final double distance = direction * (newState.getElement(0)
        - this.distToStartOfEdge);
    
    if ( distance < 0d) {
      newState.setElement(0, this.distToStartOfEdge);
    } else if (distance > this.line.getLength()) {
      newState.setElement(0, this.distToStartOfEdge + direction * this.line.getLength());
    }
    return newState;
  }

  /**
   * Returns a state on the edge that's been truncated within the given
   * tolerance. The relative parameters set to true will return a state relative
   * to the edge (i.e. removing the distance to start).
   * 
   * @param state
   * @param tolerance
   * @param relative
   * @return the state on the edge or null if it's
   */
  public Vector getCheckedStateOnEdge(Vector state, double tolerance,
    boolean relative) {
    Preconditions.checkState(!this.isNullEdge());
    Preconditions.checkArgument(tolerance >= 0d);
    Preconditions.checkArgument(state.getDimensionality() == 2);

    final Vector newState = state.clone();
    final double distance = newState.getElement(0);
    final double direction = this.isBackward ? -1d : 1d;
    final double posDistAdj =
        direction * distance - Math.abs(this.distToStartOfEdge);
    final double overEndDist = posDistAdj - this.getLength();
    if (overEndDist > 0d) {
      if (overEndDist > tolerance) {
        return null;
      } else {
        newState.setElement(0, direction * this.getLength()
            + (relative ? 0d : this.distToStartOfEdge));
      }
    } else if (posDistAdj < 0d) {
      if (posDistAdj < -tolerance) {
        return null;
      } else {
        newState.setElement(0, (relative ? 0d
            : this.distToStartOfEdge));
      }
    }

    if (relative) {
      newState.setElement(0, direction * posDistAdj);
    }

    return newState;
  }

  public Double getDistToStartOfEdge() {
    return this.distToStartOfEdge;
  }

  public InferenceGraphSegment getSegment() {
    return this.segment;
  }
  
  public LineSegment getLine() {
    return this.line;
  }

  public InferenceGraphEdge getInferenceGraphEdge() {
    return this.edge;
  }

  public double getLength() {
    return this.segment.getLine().getLength();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((this.distToStartOfEdge == null) ? 0
                : this.distToStartOfEdge.hashCode());
    result =
        prime * result
            + ((this.edge == null) ? 0 : this.edge.hashCode());
    result =
        prime * result
            + ((this.segment == null) ? 0 : this.segment.hashCode());
    result =
        prime
            * result
            + ((this.isBackward == null) ? 0 : this.isBackward
                .hashCode());
    return result;
  }

  public Boolean isBackward() {
    return this.isBackward;
  }

  public boolean isNullEdge() {
    return this.equals(nullPathEdge);
  }

  public boolean isOnEdge(double distance) {
    final double direction = this.isBackward ? -1d : 1d;
    final double posDistToStart = Math.abs(this.distToStartOfEdge);
    final double posDistOffset =
        direction * distance - posDistToStart;

    if (posDistOffset - this.getLength() > 1e-7d) {
      return false;
    } else if (posDistOffset < 0d) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    if (this.isNullEdge()) {
      return "PathEdge [empty edge]";
    } else {
      final double distToStart =
          this.distToStartOfEdge == 0d && this.isBackward ? -0d
              : this.distToStartOfEdge.longValue();
      return "PathEdge [edge=" + this.edge.getEdgeId() + " ("
          + this.getLength() + "/" + this.edge.getLength() + ")"
          + ", distToStart=" + distToStart + "]";
    }
  }

}