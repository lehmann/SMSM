package br.ufsc.lehmann.method;

import org.apache.commons.lang3.mutable.MutableInt;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.SMSM;
import br.ufsc.lehmann.SlackTemporalSemantic;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface SMSMDistanceAndTemporalDurationTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		MoveSemantic ellipseMoveSemantic = null;
		MoveSemantic distanceMoveSemantic = null;
		MoveSemantic temporalDurationMoveSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.SPATIAL_LATLON;
		MutableInt geoThreshold = Thresholds.STOP_CENTROID_LATLON;
		if(problem instanceof NElementProblem) {
			return new SMSMClassifier(//
						new SMSM.SMSM_MoveSemanticParameters(NElementProblem.move_ellipses, new SMSM.SMSM_DimensionParameters[] {
								new SMSM.SMSM_DimensionParameters<>(NElementProblem.move_ellipses, AttributeType.MOVE, Thresholds.MOVE_INNER_POINTS_PERC, 1)
							}),
						new SMSM.SMSM_StopSemanticParameters(NElementProblem.stop, new SMSM.SMSM_DimensionParameters[] {
								new SMSM.SMSM_DimensionParameters<>(Semantic.SPATIAL, AttributeType.STOP_SPATIAL, 0.5, 1.0/2.0),
								new SMSM.SMSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/2.0)
							})
						);
//		} else if(problem instanceof NewYorkBusProblem) {
//			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
//			ellipseMoveSemantic = NewYorkBusDataReader.MOVE_ELLIPSES_SEMANTIC;
//			distanceMoveSemantic = NewYorkBusDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof DublinBusProblem) {
//			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
//			ellipseMoveSemantic = DublinBusDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof GeolifeUniversitySubProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeUniversitySubProblem) problem).stopSemantic();
			ellipseMoveSemantic = GeolifeUniversityDataReader.MOVE_ELLIPSES_SEMANTIC;
			distanceMoveSemantic = GeolifeUniversityDataReader.MOVE_DISTANCE_SEMANTIC;
			temporalDurationMoveSemantic = GeolifeUniversityDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof GeolifeProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
			ellipseMoveSemantic = GeolifeDataReader.MOVE_ELLIPSES_SEMANTIC;
			distanceMoveSemantic = GeolifeDataReader.MOVE_DISTANCE_SEMANTIC;
			temporalDurationMoveSemantic = GeolifeDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
//		} else if(problem instanceof PatelProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((PatelProblem) problem).stopSemantic();
//			ellipseMoveSemantic = PatelDataReader.MOVE_ELLIPSES_SEMANTIC;
//		} else if(problem instanceof VehicleProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((VehicleProblem) problem).stopSemantic();
//			ellipseMoveSemantic = VehicleDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
			ellipseMoveSemantic = SanFranciscoCabDataReader.MOVE_ELLIPSES_SEMANTIC;
			distanceMoveSemantic = SanFranciscoCabDataReader.MOVE_DISTANCE_SEMANTIC;
			temporalDurationMoveSemantic = SanFranciscoCabDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof InvolvesProblem) {
			stopSemantic = ((InvolvesProblem) problem).stopSemantic();
			ellipseMoveSemantic = InvolvesDatabaseReader.MOVE_ELLIPSES_SEMANTIC;
			distanceMoveSemantic = InvolvesDatabaseReader.MOVE_DISTANCE_SEMANTIC;
			temporalDurationMoveSemantic = InvolvesDatabaseReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
//		} else if(problem instanceof SergipeTracksProblem) {
//			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
//			ellipseMoveSemantic = SergipeTracksDataReader.MOVE_ELLIPSES_SEMANTIC;
//		} else if(problem instanceof PrototypeProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
//			ellipseMoveSemantic = PrototypeDataReader.MOVE_SEMANTIC;
//		} else if(problem instanceof PisaProblem) {
//			stopSemantic = ((PisaProblem) problem).stopSemantic();
//			ellipseMoveSemantic = PisaDataReader.MOVE_ELLIPSES_SEMANTIC;
//		} else if(problem instanceof HermoupolisProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((HermoupolisProblem) problem).stopSemantic();
//			ellipseMoveSemantic = HermoupolisDataReader.MOVE_ELLIPSES_SEMANTIC;
		}
		return new SMSMClassifier(//
				new SMSM.SMSM_MoveSemanticParameters(ellipseMoveSemantic, new SMSM.SMSM_DimensionParameters[] {
//						new SMSM.H_MSM_DimensionParameters<>(ellipseMoveSemantic, AttributeType.MOVE, Thresholds.MOVE_INNER_POINTS_PERC, 1.0/3.0),
						new SMSM.SMSM_DimensionParameters<>(distanceMoveSemantic, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1.0/2.0),
						new SMSM.SMSM_DimensionParameters<>(temporalDurationMoveSemantic, AttributeType.MOVE, Thresholds.MOVE_DURATION, 1.0/2.0)
					}),
				new SMSM.SMSM_StopSemanticParameters(stopSemantic, new SMSM.SMSM_DimensionParameters[] {
						new SMSM.SMSM_DimensionParameters<>(geoSemantic, AttributeType.STOP_SPATIAL, geoThreshold.intValue(), 1.0/2.0),
//						new SMSM.H_MSM_DimensionParameters<>(SlackTemporalSemantic.SLACK_TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
						new SMSM.SMSM_DimensionParameters<>(stopSemantic, AttributeType.STOP, Thresholds.calculateThreshold(stopSemantic), 1.0/2.0)
					})
				);
	}
}
