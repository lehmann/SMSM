package br.ufsc.lehmann.msm.artigo;

import java.sql.Timestamp;

import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Stop;

public class ComparableStop extends Stop implements Comparable<ComparableStop> {
	
	private StopComparator comparator;

	public ComparableStop(Stop stop, StopComparator comparator) {
		super(stop.getStopId(), stop.getStopName(), stop.getStartTime(), stop.getEndTime(), stop.getStartPoint(), stop.getEndPoint(), stop.getCentroid());
		this.comparator = comparator;
	}

	public ComparableStop(int stopId, String stopName, Timestamp startTime, Timestamp endTime, TPoint startPoint, TPoint endPoint, TPoint centroid) {
		super(stopId, stopName, startTime, endTime, startPoint, endPoint, centroid);
	}

	@Override
	public int compareTo(ComparableStop o) {
		return comparator.compare(this, o);
	}

}