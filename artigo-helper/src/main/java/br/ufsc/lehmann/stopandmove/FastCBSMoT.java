package br.ufsc.lehmann.stopandmove;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.stopandmove.StopAndMoveExtractor.PropertiesCallback;

public class FastCBSMoT {
	
	private SpatialDistanceFunction distance;
	private PropertiesCallback propsCallback;

	public FastCBSMoT(SpatialDistanceFunction distance) {
		this(distance, StopAndMoveExtractor.EMPTY_PROPERTIES);
	}

	public FastCBSMoT(SpatialDistanceFunction distance, PropertiesCallback addProperties) {
		this.distance = distance;
		this.propsCallback = addProperties;
	}

	public StopAndMove findStops(SemanticTrajectory T, double maxDist, int minTime, int timeTolerance, int mergeTolerance, double ratio,
			AtomicInteger sid, AtomicInteger mid) {
		int size = T.length();
		int[] neighborhood = new int[size];
	
		for (int i = 0; i < neighborhood.length; i++) {
			neighborhood[i] = 0;
		}
	
		for (int i = 0; i < T.length(); i++) {
			int value = countNeighbors(i, T, ratio);
			neighborhood[i] = value;
			i += value;
		}
	
		StopAndMove ret = new StopAndMove(T);
		for (int i = 0; i < neighborhood.length; i++) {
			if (neighborhood[i] > 0) {
				Instant p1 = Semantic.TEMPORAL.getData(T, i).getStart();
				Instant p2 = Semantic.TEMPORAL.getData(T, i + neighborhood[i] - 1).getStart();
	
				long p1Milli = p1.toEpochMilli();
				long p2Milli = p2.toEpochMilli();
				long diff = p2Milli - p1Milli;
				if (diff >= timeTolerance) {
					List<Long> points = new ArrayList<>(neighborhood[i]);
					Stop s = new Stop(sid.incrementAndGet(), i, p1Milli, neighborhood[i] + 1, p2Milli);
					s.setCentroid(centroid(T, i, i + neighborhood[i] - 1));
	
					for (int x = 0; x <= neighborhood[i]; x++) {
						TPoint p = Semantic.SPATIAL.getData(T, i + x);
						s.addPoint(p);
						points.add(Semantic.GID.getData(T, i + x).longValue());
					}
					i += neighborhood[i];
					propsCallback.addProperties(T, s);
					ret.addStop(s, points);
				} else {
					List<Long> gids = new ArrayList<>();
					List<TPoint> points = new ArrayList<>();
					int init = i, j = i;
					for (; j < neighborhood.length && j < i + neighborhood[i] + 1; j++) {
						long gid = Semantic.GID.getData(T, j).longValue();
						TPoint p = Semantic.SPATIAL.getData(T, j);
						gids.add(gid);
						points.add(new TPoint(gid, p.getX(), p.getY(), p.getTimestamp()));
					}
					Move m = new Move(mid.incrementAndGet(), ret.lastStop(), null, p1Milli, p2Milli, init, j - init, points.toArray(new TPoint[points.size()]));
					propsCallback.addProperties(T, m);
					ret.addMove(m, gids);
					i += neighborhood[i];
				}
			} else {
				List<Long> gids = new ArrayList<>();
				List<TPoint> points = new ArrayList<>();
				int init = i, j = i;
				for (; j < neighborhood.length && neighborhood[j] == 0; j++) {
					long gid = Semantic.GID.getData(T, j).longValue();
					TPoint p = Semantic.SPATIAL.getData(T, j);
					gids.add(gid);
					points.add(new TPoint(gid, p.getX(), p.getY(), p.getTimestamp()));
				}
				i = j - 1;
				Instant p1 = Semantic.TEMPORAL.getData(T, init).getStart();
				Instant p2 = Semantic.TEMPORAL.getData(T, i).getEnd();
				Move m = new Move(mid.incrementAndGet(), ret.lastStop(), null, p1.toEpochMilli(), p2.toEpochMilli(), init, j - init, points.toArray(new TPoint[points.size()]));
				propsCallback.addProperties(T, m);
				ret.addMove(m, gids);
			}
		}
	
		ret = mergeStops(ret, maxDist, mergeTolerance);
		ret = cleanStops(ret, minTime, mid);
		return ret;
	}
	StopAndMove cleanStops(StopAndMove stopAndMove, int minTime, AtomicInteger mid) {
		List<Stop> S = new ArrayList<>(stopAndMove.getStops());
		for (int i = 0; i < S.size(); i++) {
			Stop s = S.get(i);

			if ((s.getEndTime() - s.getStartTime()) < minTime) {
				stopAndMove.remove(s, i == 0 ? null : S.get(i - 1), i + 1 == S.size() ? null : S.get(i + 1), mid);
			}
		}
		return stopAndMove;
	}

	// TODO: adicionar atividades na lista
	StopAndMove mergeStops(StopAndMove stopAndMove, double maxDist, int timeTolerance) {
		List<Stop> S = new ArrayList<>(stopAndMove.getStops());
		for (int i = 0; i < S.size(); i++) {
			if (i + 1 != S.size()) {
				Stop s1 = S.get(i);
				Stop s2 = S.get(i + 1);
				if (s2.getStartTime() - s1.getEndTime() <= timeTolerance) {

					TPoint c1 = S.get(i).getCentroid();
					TPoint c2 = S.get(i + 1).getCentroid();

					if (distance.distance(c1, c2) <= distance.convert(maxDist)) {
						stopAndMove.mergeStops(s1, s2);
						
						S.remove(s2);
						i--;
					}
				}
			}
		}
		return stopAndMove;
	}

	TPoint centroid(SemanticTrajectory T, int start, int end) {
		double x = 0;
		double y = 0;

		int i = start;
		int total = 0;
		while (i >= start && i <= end) {
			total++;
			TPoint point = Semantic.SPATIAL.getData(T, i);
			x += point.getX();
			y += point.getY();
			i++;
		}

		TPoint p = new TPoint(0, x / total, y / total, new Timestamp(0));
		return p;
	}

	int countNeighbors(int i, SemanticTrajectory T, double maxDist) {
		int neighbors = 0;
		boolean yet = true;
		int j = i + 1;
		while (j < T.length() && yet) {
			TPoint p = Semantic.SPATIAL.getData(T, i);
			TPoint d = Semantic.SPATIAL.getData(T, j);
			if (distance.distance(p, d) < distance.convert(maxDist)) {
				neighbors++;
			} else {
				yet = false;
			}
			j++;
		}
		return neighbors;
	}
}
