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
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMClassifier;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesDatabaseReader;
import br.ufsc.lehmann.msm.artigo.problems.InvolvesProblem;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabDataReader;
import br.ufsc.lehmann.msm.artigo.problems.SanFranciscoCabProblem;

public interface SMSMDistanceTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		MoveSemantic moveSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.SPATIAL_LATLON;
		MutableInt geoThreshold = Thresholds.STOP_CENTROID_LATLON;
		if(problem instanceof NElementProblem) {
			return new SMSMClassifier(//
					new SMSM.SMSM_MoveSemanticParameters(NElementProblem.move, new SMSM.SMSM_DimensionParameters[] {
							new SMSM.SMSM_DimensionParameters<>(NElementProblem.move, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
						}),
					new SMSM.SMSM_StopSemanticParameters(NElementProblem.stop, new SMSM.SMSM_DimensionParameters[] {
							new SMSM.SMSM_DimensionParameters<>(Semantic.SPATIAL, AttributeType.STOP_SPATIAL, 0.5, 1.0/2.0),
							new SMSM.SMSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/2.0)
						})
					);
//		} else if(problem instanceof NewYorkBusProblem) {
//			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
//			moveSemantic = NewYorkBusDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof DublinBusProblem) {
//			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
//			moveSemantic = DublinBusDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof PatelProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((PatelProblem) problem).stopSemantic();
//			moveSemantic = PatelDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof VehicleProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = ((VehicleProblem) problem).stopSemantic();
//			moveSemantic = VehicleDataReader.MOVE_DISTANCE_SEMANTIC;
		} else if(problem instanceof InvolvesProblem) {
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((InvolvesProblem) problem).stopSemantic();
			moveSemantic = InvolvesDatabaseReader.MOVE_DISTANCE_SEMANTIC;
		} else if(problem instanceof GeolifeUniversitySubProblem) {
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeUniversitySubProblem) problem).stopSemantic();
			moveSemantic = GeolifeUniversityDataReader.MOVE_DISTANCE_SEMANTIC;
		} else if(problem instanceof GeolifeProblem) {
			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
			moveSemantic = GeolifeUniversityDataReader.MOVE_DISTANCE_SEMANTIC;
		} else if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
			moveSemantic = SanFranciscoCabDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof SergipeTracksProblem) {
//			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
//			moveSemantic = SergipeTracksDataReader.MOVE_DISTANCE_SEMANTIC;
//		} else if(problem instanceof PrototypeProblem) {
//			geoThreshold = Thresholds.SPATIAL_EUCLIDEAN;
//			geoSemantic = Semantic.SPATIAL_EUCLIDEAN;
//			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
//			moveSemantic = PrototypeDataReader.MOVE_SEMANTIC;
//		} else if(problem instanceof PisaProblem) {
//			stopSemantic = ((PisaProblem) problem).stopSemantic();
//			moveSemantic = PisaDataReader.MOVE_DISTANCE_SEMANTIC;
		}
		return new SMSMClassifier(//
				new SMSM.SMSM_MoveSemanticParameters(moveSemantic, new SMSM.SMSM_DimensionParameters[] {
						new SMSM.SMSM_DimensionParameters<>(moveSemantic, AttributeType.MOVE, Thresholds.MOVE_DISTANCE, 1)
					}),
				new SMSM.SMSM_StopSemanticParameters(stopSemantic, new SMSM.SMSM_DimensionParameters[] {
						new SMSM.SMSM_DimensionParameters<>(geoSemantic, AttributeType.STOP_SPATIAL, geoThreshold.intValue(), 1.0/2.0),
						//new SMSM.H_MSM_DimensionParameters<>(SlackTemporalSemantic.SLACK_TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/3.0),
						new SMSM.SMSM_DimensionParameters<>(stopSemantic, AttributeType.STOP, Thresholds.calculateThreshold(stopSemantic), 1.0/2.0)
					})
				);
	}
}
