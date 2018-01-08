
package br.ufsc.lehmann.method;

import java.time.Instant;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.TPoint;
import br.ufsc.core.trajectory.TemporalDuration;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.lehmann.H_MSM_StopMove;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NElementProblem;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.msm.artigo.Problem;
import br.ufsc.lehmann.msm.artigo.classifiers.H_MSM_StopMove_Classifier;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusDataReader;
import br.ufsc.lehmann.msm.artigo.problems.DublinBusProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeProblem;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversityDataReader;
import br.ufsc.lehmann.msm.artigo.problems.GeolifeUniversitySubProblem;
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

public interface H_MSM_StopMove_TemporalDurationTest {

	default IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		StopSemantic stopSemantic = null;
		MoveSemantic moveSemantic = null;
		Semantic<TPoint, Number> geoSemantic = Semantic.GEOGRAPHIC_LATLON;
		double geoThreshold = Thresholds.STOP_CENTROID_LATLON;
		if(problem instanceof NElementProblem) {
			return new H_MSM_StopMove_Classifier(//
					new H_MSM_StopMove.H_MSM_MoveSemanticParameters(NElementProblem.move_duration, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(NElementProblem.move_duration, AttributeType.MOVE, Thresholds.MOVE_DURATION, 1)
						}),
					new H_MSM_StopMove.H_MSM_StopSemanticParameters(NElementProblem.stop, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.GEOGRAPHIC, AttributeType.STOP_GEOGRAPHIC, 0.5, 1.0/2.0),
							new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, AttributeType.STOP_TEMPORAL, Thresholds.TEMPORAL, 1.0/2.0)
						})
					);
		} else if(problem instanceof NewYorkBusProblem) {
			stopSemantic = ((NewYorkBusProblem) problem).stopSemantic();
			moveSemantic = NewYorkBusDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof DublinBusProblem) {
			stopSemantic = ((DublinBusProblem) problem).stopSemantic();
			moveSemantic = DublinBusDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof GeolifeUniversitySubProblem) {
			stopSemantic = ((GeolifeUniversitySubProblem) problem).stopSemantic();
			moveSemantic = GeolifeUniversityDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof GeolifeProblem) {
			stopSemantic = ((GeolifeProblem) problem).stopSemantic();
			moveSemantic = GeolifeUniversityDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof PatelProblem) {
			geoThreshold = Thresholds.GEOGRAPHIC_EUCLIDEAN;
			geoSemantic = Semantic.GEOGRAPHIC_EUCLIDEAN;
			stopSemantic = ((PatelProblem) problem).stopSemantic();
			moveSemantic = PatelDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof VehicleProblem) {
			geoThreshold = Thresholds.GEOGRAPHIC_EUCLIDEAN;
			geoSemantic = Semantic.GEOGRAPHIC_EUCLIDEAN;
			stopSemantic = ((VehicleProblem) problem).stopSemantic();
			moveSemantic = VehicleDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof SanFranciscoCabProblem) {
			stopSemantic = ((SanFranciscoCabProblem) problem).stopSemantic();
			moveSemantic = SanFranciscoCabDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof SergipeTracksProblem) {
			stopSemantic = SergipeTracksDataReader.STOP_CENTROID_SEMANTIC;
			moveSemantic = SergipeTracksDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		} else if(problem instanceof PrototypeProblem) {
			geoThreshold = Thresholds.GEOGRAPHIC_EUCLIDEAN;
			geoSemantic = Semantic.GEOGRAPHIC_EUCLIDEAN;
			stopSemantic = PrototypeDataReader.STOP_SEMANTIC;
			moveSemantic = PrototypeDataReader.MOVE_SEMANTIC;
		} else if(problem instanceof PisaProblem) {
			stopSemantic = ((PisaProblem) problem).stopSemantic();
			moveSemantic = PisaDataReader.MOVE_TEMPORAL_DURATION_SEMANTIC;
		}
		return new H_MSM_StopMove_Classifier(//
				new H_MSM_StopMove.H_MSM_MoveSemanticParameters(moveSemantic, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
						new H_MSM_StopMove.H_MSM_DimensionParameters<>(moveSemantic, AttributeType.MOVE, Thresholds.MOVE_DURATION, 1)
				}),
				new H_MSM_StopMove.H_MSM_StopSemanticParameters(stopSemantic, new H_MSM_StopMove.H_MSM_DimensionParameters[] {
						new H_MSM_StopMove.H_MSM_DimensionParameters<>(geoSemantic, AttributeType.STOP_GEOGRAPHIC, geoThreshold, 1.0/3.0),
						new H_MSM_StopMove.H_MSM_DimensionParameters<>(Semantic.TEMPORAL, new AttributeType(
								(Stop s) -> new TemporalDuration(Instant.ofEpochMilli(s.getStartTime() - Thresholds.SLACK_TEMPORAL/2), Instant.ofEpochMilli(s.getEndTime() +  Thresholds.SLACK_TEMPORAL/2)), "SLACK_TEMPORAL"
								), Thresholds.TEMPORAL, 1.0/3.0),
						new H_MSM_StopMove.H_MSM_DimensionParameters<>(stopSemantic, AttributeType.STOP, Thresholds.calculateThreshold(stopSemantic), 1.0/3.0)
				})
				);
	}
}
