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
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisDataReader;
import br.ufsc.lehmann.msm.artigo.problems.HermoupolisProblem;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.NewYorkBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.PatelDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PatelProblem;
import br.ufsc.lehmann.msm.artigo.problems.PisaDataReader;
import br.ufsc.lehmann.msm.artigo.problems.PisaProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SergipeTracksProblem;
import br.ufsc.lehmann.msm.artigo.problems.VehicleDataReader;
import br.ufsc.lehmann.msm.artigo.problems.VehicleProblem;
import br.ufsc.lehmann.prototype.PrototypeDataReader;
import br.ufsc.lehmann.prototype.PrototypeProblem;

public interface SMSMEllipsesWithTemporalDurationTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		MoveSemantic ellipseMoveSemantic = null;
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
		} else if(problem instanceof NewYorkBusProblem) {
			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
			ellipseMoveSemantic = NewYorkBusDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof DublinBusProblem) {
			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
			ellipseMoveSemantic = DublinBusDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof GeolifeUniversitySubProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeUniversitySubProblem) problem).stopSemantic();
			ellipseMoveSemantic = GeolifeUniversityDataReader.MOVE_ELLIPSES_SEMANTIC;
			temporalDurationMoveSemantic = GeolifeUniversityDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof GeolifeProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
			ellipseMoveSemantic = GeolifeDataReader.MOVE_ELLIPSES_SEMANTIC;
			temporalDurationMoveSemantic = GeolifeDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof PatelProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((PatelProblem) problem).stopSemantic();
			ellipseMoveSemantic = PatelDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof VehicleProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((VehicleProblem) problem).stopSemantic();
			ellipseMoveSemantic = VehicleDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
			ellipseMoveSemantic = SanFranciscoCabDataReader.MOVE_ELLIPSES_SEMANTIC;
			temporalDurationMoveSemantic = SanFranciscoCabDataReader.MOVE_DISTANCE_SEMANTIC;
		} else if(problem instanceof SergipeTracksProblem) {
			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
			ellipseMoveSemantic = SergipeTracksDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof PrototypeProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
			ellipseMoveSemantic = PrototypeDataReader.MOVE_SEMANTIC;
		} else if(problem instanceof PisaProblem) {
			stopSemantic = ((PisaProblem) problem).stopSemantic();
			ellipseMoveSemantic = PisaDataReader.MOVE_ELLIPSES_SEMANTIC;
		} else if(problem instanceof HermoupolisProblem) {
			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((HermoupolisProblem) problem).stopSemantic();
			ellipseMoveSemantic = HermoupolisDataReader.MOVE_ELLIPSES_SEMANTIC;
		}
		return new SMSMClassifier(//
				new SMSM.SMSM_MoveSemanticParameters(ellipseMoveSemantic, new SMSM.SMSM_DimensionParameters[] {
						new SMSM.SMSM_DimensionParameters<>(ellipseMoveSemantic, AttributeType.MOVE, Thresholds.MOVE_INNER_POINTS_PERC, 1.0/2.0),
						new SMSM.SMSM_DimensionParameters<>(temporalDurationMoveSemantic, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1.0/2.0)
					}),
				new SMSM.SMSM_StopSemanticParameters(stopSemantic, new SMSM.SMSM_DimensionParameters[] {
						new SMSM.SMSM_DimensionParameters<>(geoSemantic, AttributeType.STOP_SPATIAL, geoThreshold.intValue(), 1.0/3.0),
						new SMSM.SMSM_DimensionParameters<>(SlackTemporalSemantic.SLACK_TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
						new SMSM.SMSM_DimensionParameters<>(stopSemantic, AttributeType.STOP, Thresholds.calculateThreshold(stopSemantic), 1.0/3.0)
					})
				);
	}
}
