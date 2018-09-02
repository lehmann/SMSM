package br.ufsc.lehmann.testexecution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

import br.ufsc.core.trajectory.EqualsDistanceFunction;
import br.ufsc.core.trajectory.IDistanceFunction;
import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.SpatialDistanceFunction;
import br.ufsc.core.trajectory.StopSemantic;
import br.ufsc.core.trajectory.semantic.AttributeDescriptor;
import br.ufsc.core.trajectory.semantic.AttributeType;
import br.ufsc.core.trajectory.semantic.Move;
import br.ufsc.core.trajectory.semantic.Stop;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.ftsm.related.LCSS.LCSSSemanticParameter;
import br.ufsc.ftsm.related.MSM.MSMSemanticParameter;
import br.ufsc.ftsm.related.UMS;
import br.ufsc.lehmann.AngleDistance;
import br.ufsc.lehmann.DTWDistance;
import br.ufsc.lehmann.EllipsesDistance;
import br.ufsc.lehmann.MoveSemantic;
import br.ufsc.lehmann.NumberDistance;
import br.ufsc.lehmann.ProportionDistance;
import br.ufsc.lehmann.SMSM;
import br.ufsc.lehmann.SMSM.SMSM_DimensionParameters;
import br.ufsc.lehmann.SlackTemporalSemantic;
import br.ufsc.lehmann.Thresholds;
import br.ufsc.lehmann.TimestampDistance;
import br.ufsc.lehmann.method.CVTI;
import br.ufsc.lehmann.method.CVTI.CVTISemanticParameter;
import br.ufsc.lehmann.method.EDR.EDRSemanticParameter;
import br.ufsc.lehmann.method.wDF;
import br.ufsc.lehmann.msm.artigo.ComparableStopSemantic;
import br.ufsc.lehmann.msm.artigo.classifiers.DTWaClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.EDRClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.LCSSClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.MSMClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.MSTPClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMExtendedClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMExtendedPartialClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMPartialClassifier;
import br.ufsc.lehmann.msm.artigo.classifiers.SMSMartClassifier;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.utils.EuclideanDistanceFunction;
import br.ufsc.utils.LatLongDistanceFunction;

public class Measures {

	public static TrajectorySimilarityCalculator<SemanticTrajectory> createMeasure(Measure measure) {
		return createMeasures(measure).get(0);
	}
	
	public static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createMeasures(Measure measure) {
		if(measure.getName().equalsIgnoreCase("SMSM")) {
			return createSMSM(measure);
		}
		if(measure.getName().equalsIgnoreCase("SMSMExtended")) {
			return createSMSM(measure, true);
		}
		if(measure.getName().equalsIgnoreCase("SMSMart")) {
			return createSMSM(measure, true, false, true);
		}
		if(measure.getName().equalsIgnoreCase("SMSMPartial")) {
			return createSMSM(measure, false, true);
		}
		if(measure.getName().equalsIgnoreCase("SMSMExtendedPartial")) {
			return createSMSM(measure, true, true);
		}
		if(measure.getName().equalsIgnoreCase("LCSS")) {
			return createLCSS(measure);
		}
		if(measure.getName().equalsIgnoreCase("EDR")) {
			return createEDR(measure);
		}
		if(measure.getName().equalsIgnoreCase("MSTP")) {
			return createMSTP(measure);
		}
		if(measure.getName().equalsIgnoreCase("CVTI")) {
			return createCVTI(measure);
		}
		if(measure.getName().equalsIgnoreCase("MSM")) {
			return createMSM(measure);
		}
		if(measure.getName().equalsIgnoreCase("UMS")) {
			return createUMS(measure);
		}
		if(measure.getName().equalsIgnoreCase("DTWa")) {
			return createDTWa(measure);
		}
		if(measure.getName().equalsIgnoreCase("wDF")) {
			return createWDF(measure);
		}
		return null;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createUMS(Measure measure) {
		return Arrays.asList(new UMS());
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createSMSM(Measure measure) {
		return createSMSM(measure, false);
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createSMSM(Measure measure, boolean extended) {
		return createSMSM(measure, extended, false);
	}
	
	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createSMSM(Measure measure, boolean extended, boolean partial) {
		return createSMSM(measure, extended, partial, false);
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createSMSM(Measure measure, boolean extended, boolean partial, boolean smart) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while(grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			Double stopWeight = 0.0, moveWeight = 0.0;
			StopSemantic stop = null;
			MoveSemantic move = null;
			List<SMSM.SMSM_DimensionParameters<Stop>> stopDimensions = new ArrayList<>();
			List<SMSM.SMSM_DimensionParameters<Move>> moveDimensions = new ArrayList<>();
			for (Param param : params) {
				if(param.getType().equalsIgnoreCase("stop")) {
					stopWeight = param.getWeight();
					stop = new StopSemantic(param.getIndex().intValue(), null);
					List<Param> stopParams = param.getParams();
					for (Param stopParam : stopParams) {
						AttributeType attr = null;
						boolean isSpatial = false;
						String d = stopParam.getDistance();
						String stopThreshold = grid.getThreshold(stopParam);
						Number threshold = Strings.isNullOrEmpty(stopThreshold) ?  null : Double.parseDouble(stopThreshold);
						double weight = stopParam.getWeight().doubleValue();
						
						IDistanceFunction distance = null;
						if(!Strings.isNullOrEmpty(d)) {
							distance = createDistance(stopParam, d);
						}
							
						String type = stopParam.getType().toUpperCase();
						Long index = stopParam.getIndex();
						Semantic semantic = null;
						AttributeType mainAttribute = AttributeType.STOP;
						switch(type) {
							case "SPATIAL":
								attr = AttributeType.STOP_SPATIAL;
								mainAttribute = AttributeType.STOP_SPATIAL;
								isSpatial = true;
								switch(stopParam.getDistance().toUpperCase()) {
								case "LATLON":
									semantic = Semantic.SPATIAL_LATLON;
									break;
								case "EUCLIDEAN":
									semantic = Semantic.SPATIAL_EUCLIDEAN;
									break;
								}
								break;
							case "TEMPORAL":
								attr = AttributeType.STOP_TEMPORAL;
								mainAttribute = AttributeType.STOP_TEMPORAL;
								semantic = Semantic.TEMPORAL;
								break;
							case "SEMANTIC":
								List<Param> semanticParams = stopParam.getParams();
								Param semParam = semanticParams.get(0);
								String semType = semParam.getType().toUpperCase();
								switch(semType) {
									case "NAME":
										attr = AttributeType.STOP_NAME;
										break;
									case "REGION":
										attr = AttributeType.STOP_REGION;
										break;
									case "CENTROID":
										attr = AttributeType.STOP_CENTROID;
										break;
									case "DURATION":
										attr = AttributeType.STOP_DURATION;
										break;
									default:
										attr = AttributeType.STOP;
								}
								String semDistance = semParam.getDistance().toUpperCase();
								distance = createDistance(semParam, semDistance);
								AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
								semantic = new StopSemantic(index.intValue(), desc);
								try {
									String t = grid.getThreshold(semParam);
									threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
								} catch (NumberFormatException e) {
									switch (semParam.getThreshold().toUpperCase()) {
										case "SUMMED-DISTANCES":
											threshold = Thresholds.MOVE_INNERPOINTS_DTW_DISTANCE;
											break;
										default:
											throw e;
									}
								}
								break;
							}
						SMSM_DimensionParameters dimension = new SMSM.SMSM_DimensionParameters(semantic, mainAttribute, threshold, weight, isSpatial);
						stopDimensions.add(dimension);
					}
				} else if(param.getType().equalsIgnoreCase("move")) {
					moveWeight = param.getWeight();
					move = new MoveSemantic(param.getIndex().intValue(), null);
					List<Param> moveParams = param.getParams();
					for (Param moveParam : moveParams) {
						AttributeType attr = null;
						boolean isSpatial = false;
						String d = moveParam.getDistance();
						Number threshold = null;

						String t = grid.getThreshold(moveParam);
						try {
							threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
						} catch (NumberFormatException e) {
							switch (t.toUpperCase()) {
								case "SUMMED-DISTANCES":
									threshold = Thresholds.MOVE_INNERPOINTS_DTW_DISTANCE;
									break;
								default:
									throw e;
							}
						}
						double weight = moveParam.getWeight().doubleValue();
						IDistanceFunction distance = null;
						if(!Strings.isNullOrEmpty(d)) {
							distance = createDistance(moveParam, d);
						}
						String type = moveParam.getType().toUpperCase();
						switch(type) {
							case "POINTS":
								attr = AttributeType.MOVE_POINTS;
								isSpatial = true;
								break;
							case "DURATION":
								attr = AttributeType.MOVE_DURATION;
								break;
							case "SEMANTIC":
								List<Param> semanticParams = moveParam.getParams();
								Param semParam = semanticParams.get(0);
								String semType = semParam.getType().toUpperCase();
								switch(semType) {
									case "ACTIVITY":
										attr = AttributeType.MOVE_ACTIVITY;
										break;
									case "STREET_NAME":
										attr = AttributeType.MOVE_STREET_NAME;
										break;
									case "TRAVELLED_DISTANCE":
										attr = AttributeType.MOVE_TRAVELLED_DISTANCE;
										break;
									case "TRANSPORTATION_MODE":
										attr = AttributeType.MOVE_TRANSPORTATION_MODE;
										break;
									case "USER":
										attr = AttributeType.MOVE_USER;
										break;
									default:
										attr = AttributeType.MOVE;
								}
								String semDistance = semParam.getDistance().toUpperCase();
								distance = createDistance(semParam, semDistance);
								t = semParam.getThreshold();
							threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
							break;
						}
						Long index = param.getIndex();
						AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
						MoveSemantic moveSemantic = new MoveSemantic(index.intValue(), desc);
						SMSM_DimensionParameters<Move> dimension = new SMSM.SMSM_DimensionParameters<>(moveSemantic, AttributeType.MOVE, threshold, weight, isSpatial);
						moveDimensions.add(dimension);
					}
				}
			}

			if(extended && smart) {
				ret.add(new SMSMartClassifier(//
						new SMSM.SMSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.SMSM_DimensionParameters[moveDimensions.size()]), moveWeight),
						new SMSM.SMSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.SMSM_DimensionParameters[stopDimensions.size()]), stopWeight)
						));
			}
			if(extended && partial) {
				ret.add(new SMSMExtendedPartialClassifier(//
						new SMSM.SMSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.SMSM_DimensionParameters[moveDimensions.size()]), moveWeight),
						new SMSM.SMSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.SMSM_DimensionParameters[stopDimensions.size()]), stopWeight)
						));
			}
			if(partial) {
				ret.add(new SMSMPartialClassifier(//
						new SMSM.SMSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.SMSM_DimensionParameters[moveDimensions.size()]), moveWeight),
						new SMSM.SMSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.SMSM_DimensionParameters[stopDimensions.size()]), stopWeight)
						));
			}
			if(extended) {
				ret.add(new SMSMExtendedClassifier(//
						new SMSM.SMSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.SMSM_DimensionParameters[moveDimensions.size()]), moveWeight),
						new SMSM.SMSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.SMSM_DimensionParameters[stopDimensions.size()]), stopWeight)
						));
			}
			ret.add(new SMSMClassifier(//
					new SMSM.SMSM_MoveSemanticParameters(move, moveDimensions.toArray(new SMSM.SMSM_DimensionParameters[moveDimensions.size()]), moveWeight),
					new SMSM.SMSM_StopSemanticParameters(stop, stopDimensions.toArray(new SMSM.SMSM_DimensionParameters[stopDimensions.size()]), stopWeight)
					));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createMSM(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			List<MSMSemanticParameter> stopDimensions = new ArrayList<>();
			for (Param param : params) {
				if (param.getType().equalsIgnoreCase("stop")) {
					List<Param> stopParams = param.getParams();
					for (Param stopParam : stopParams) {
						AttributeType attr = null;
						String d = stopParam.getDistance();
						String t = grid.getThreshold(stopParam);
						Double threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
						double weight = stopParam.getWeight().doubleValue();

						IDistanceFunction distance = null;
						if (!Strings.isNullOrEmpty(d)) {
							distance = createDistance(stopParam, d);
						}

						String type = stopParam.getType().toUpperCase();
						Long index = stopParam.getIndex();
						Semantic semantic = null;
						switch (type) {
						case "SPATIAL":
							attr = AttributeType.STOP_SPATIAL;
							switch (stopParam.getDistance().toUpperCase()) {
							case "LATLON":
								semantic = Semantic.SPATIAL_LATLON;
								break;
							case "EUCLIDEAN":
								semantic = Semantic.SPATIAL_EUCLIDEAN;
								break;
							}
							break;
						case "TEMPORAL":
							attr = AttributeType.STOP_TEMPORAL;
							semantic = Semantic.TEMPORAL;
							break;
						case "SEMANTIC":
							List<Param> semanticParams = stopParam.getParams();
							Param semParam = semanticParams.get(0);
							String semType = semParam.getType().toUpperCase();
							switch (semType) {
							case "NAME":
								attr = AttributeType.STOP_NAME;
								break;
							case "REGION":
								attr = AttributeType.STOP_REGION;
								break;
							case "CENTROID":
								attr = AttributeType.STOP_CENTROID;
								break;
							case "DURATION":
								attr = AttributeType.STOP_DURATION;
								break;
							default:
								attr = AttributeType.STOP;
							}
							String semDistance = semParam.getDistance().toUpperCase();
							distance = createDistance(semParam, semDistance);
							AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
							semantic = new StopSemantic(index.intValue(), desc);
							t = grid.getThreshold(semParam);
							threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
							break;
						}
						MSMSemanticParameter dimension = new MSMSemanticParameter(semantic, threshold, weight);
						stopDimensions.add(dimension);
					}
				}
			}
			ret.add(new MSMClassifier(//
					stopDimensions.toArray(new MSMSemanticParameter[stopDimensions.size()])
					));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createMSTP(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			List<Semantic> stopDimensions = new ArrayList<>();
			for (Param param : params) {
				AttributeType attr = null;
				String d = param.getDistance();

				IDistanceFunction distance = null;
				if (!Strings.isNullOrEmpty(d)) {
					distance = createDistance(param, d);
				}

				String type = param.getType().toUpperCase();
				Long index = param.getIndex();
				Semantic semantic = null;
				switch (type) {
				case "SPATIAL":
					attr = AttributeType.STOP_SPATIAL;
					switch (param.getDistance().toUpperCase()) {
					case "LATLON":
						semantic = Semantic.SPATIAL_LATLON;
						break;
					case "EUCLIDEAN":
						semantic = Semantic.SPATIAL_EUCLIDEAN;
						break;
					}
					break;
				case "TEMPORAL":
					attr = AttributeType.STOP_TEMPORAL;

					switch (param.getDistance().toUpperCase()) {
					case "PROPORTION":
						semantic = Semantic.TEMPORAL;
						break;
					case "SLACK-PROPORTION":
						Param slackParams = param.getParams().get(0);
						Double slack = Double.parseDouble(grid.getThreshold(slackParams));
						semantic = new SlackTemporalSemantic(index.intValue(), slack);
						break;
					}
					break;
				case "SEMANTIC":
					List<Param> semanticParams = param.getParams();
					Param semParam = semanticParams.get(0);
					String semType = semParam.getType().toUpperCase();
					switch (semType) {
					case "NAME":
						attr = AttributeType.STOP_NAME;
						break;
					case "REGION":
						attr = AttributeType.STOP_REGION;
						break;
					case "CENTROID":
						attr = AttributeType.STOP_CENTROID;
						break;
					case "DURATION":
						attr = AttributeType.STOP_DURATION;
						break;
					default:
						attr = AttributeType.STOP;
					}
					String semDistance = semParam.getDistance().toUpperCase();
					distance = createDistance(semParam, semDistance);
					AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
					semantic = new ComparableStopSemantic(new StopSemantic(index.intValue(), desc));
					break;
				}
				stopDimensions.add(semantic);
			}
			ret.add(new MSTPClassifier(//
					stopDimensions.toArray(new Semantic[stopDimensions.size()])));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createLCSS(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			List<LCSSSemanticParameter> stopDimensions = new ArrayList<>();
			for (Param param : params) {
				AttributeType attr = null;
				String d = param.getDistance();
				String t = grid.getThreshold(param);
				Double threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
				
				IDistanceFunction distance = null;
				if(!Strings.isNullOrEmpty(d)) {
					distance = createDistance(param, d);
				}
					
				String type = param.getType().toUpperCase();
				Long index = param.getIndex();
				Semantic semantic = null;
				switch(type) {
					case "SPATIAL":
						attr = AttributeType.STOP_SPATIAL;
						switch(param.getDistance().toUpperCase()) {
						case "LATLON":
							semantic = Semantic.SPATIAL_LATLON;
							break;
						case "EUCLIDEAN":
							semantic = Semantic.SPATIAL_EUCLIDEAN;
							break;
						}
						break;
					case "TEMPORAL":
						attr = AttributeType.STOP_TEMPORAL;

						switch(param.getDistance().toUpperCase()) {
							case "PROPORTION":
								semantic = Semantic.TEMPORAL;
								break;
							case "SLACK-PROPORTION":
								Param slackParams = param.getParams().get(0);
								Double slack = Double.parseDouble(grid.getThreshold(slackParams));
								semantic = new SlackTemporalSemantic(index.intValue(), slack);
								break;
						}
						break;
					case "SEMANTIC":
						List<Param> semanticParams = param.getParams();
						Param semParam = semanticParams.get(0);
						String semType = semParam.getType().toUpperCase();
						switch(semType) {
							case "NAME":
								attr = AttributeType.STOP_NAME;
								break;
							case "REGION":
								attr = AttributeType.STOP_REGION;
								break;
							case "CENTROID":
								attr = AttributeType.STOP_CENTROID;
								break;
							case "DURATION":
								attr = AttributeType.STOP_DURATION;
								break;
							default:
								attr = AttributeType.STOP;
						}
						String semDistance = semParam.getDistance().toUpperCase();
						distance = createDistance(semParam, semDistance);
						AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
						semantic = new StopSemantic(index.intValue(), desc);
						t = grid.getThreshold(semParam);
						threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
						break;
					}
				LCSSSemanticParameter dimension = new LCSSSemanticParameter(semantic, threshold);
				stopDimensions.add(dimension);
			}
			ret.add(new LCSSClassifier(//
					stopDimensions.toArray(new LCSSSemanticParameter[stopDimensions.size()])
					));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createCVTI(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			CVTISemanticParameter stopDimension = null;
			Semantic temporalSemantic = Semantic.TEMPORAL;
			for (Param param : params) {
				AttributeType attr = null;
				String d = param.getDistance();
				String t = grid.getThreshold(param);
				Double threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
				
				IDistanceFunction distance = null;
				if(!Strings.isNullOrEmpty(d)) {
					distance = createDistance(param, d);
				}
					
				String type = param.getType().toUpperCase();
				Long index = param.getIndex();
				Semantic semantic = null;
				switch(type) {
					case "TEMPORAL":
						attr = AttributeType.STOP_TEMPORAL;

						switch(param.getDistance().toUpperCase()) {
							case "PROPORTION":
								temporalSemantic = Semantic.TEMPORAL;
								break;
							case "SLACK-PROPORTION":
								Param slackParams = param.getParams().get(0);
								Double slack = Double.parseDouble(grid.getThreshold(slackParams));
								temporalSemantic = new SlackTemporalSemantic(index.intValue(), slack);
								break;
						}
						break;
					case "SEMANTIC":
						List<Param> semanticParams = param.getParams();
						Param semParam = semanticParams.get(0);
						String semType = semParam.getType().toUpperCase();
						switch(semType) {
							case "NAME":
								attr = AttributeType.STOP_NAME;
								break;
							case "REGION":
								attr = AttributeType.STOP_REGION;
								break;
							case "CENTROID":
								attr = AttributeType.STOP_CENTROID;
								break;
							case "DURATION":
								attr = AttributeType.STOP_DURATION;
								break;
							default:
								attr = AttributeType.STOP;
						}
						String semDistance = semParam.getDistance().toUpperCase();
						distance = createDistance(semParam, semDistance);
						AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
						semantic = new StopSemantic(index.intValue(), desc);
						t = grid.getThreshold(semParam);
						threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
						CVTISemanticParameter dimension = new CVTISemanticParameter(semantic, threshold);
						stopDimension = (dimension);
						break;
					}
			}
			ret.add(new CVTI(//
					stopDimension, temporalSemantic
					));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createEDR(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {

			List<Param> params = measure.getParams();
			List<EDRSemanticParameter> stopDimensions = new ArrayList<>();
			for (Param param : params) {
				AttributeType attr = null;
				String d = param.getDistance();
				String t = grid.getThreshold(param);
				Double threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
				
				IDistanceFunction distance = null;
				if(!Strings.isNullOrEmpty(d)) {
					distance = createDistance(param, d);
				}
					
				String type = param.getType().toUpperCase();
				Long index = param.getIndex();
				Semantic semantic = null;
				switch(type) {
					case "SPATIAL":
						attr = AttributeType.STOP_SPATIAL;
						switch(param.getDistance().toUpperCase()) {
						case "LATLON":
							semantic = Semantic.SPATIAL_LATLON;
							break;
						case "EUCLIDEAN":
							semantic = Semantic.SPATIAL_EUCLIDEAN;
							break;
						}
						break;
					case "TEMPORAL":
						attr = AttributeType.STOP_TEMPORAL;

						switch(param.getDistance().toUpperCase()) {
							case "PROPORTION":
								semantic = Semantic.TEMPORAL;
								break;
							case "SLACK-PROPORTION":
								Param slackParams = param.getParams().get(0);
								Double slack = Double.parseDouble(grid.getThreshold(slackParams));
								semantic = new SlackTemporalSemantic(index.intValue(), slack);
								break;
						}
						break;
					case "SEMANTIC":
						List<Param> semanticParams = param.getParams();
						Param semParam = semanticParams.get(0);
						String semType = semParam.getType().toUpperCase();
						switch(semType) {
							case "NAME":
								attr = AttributeType.STOP_NAME;
								break;
							case "REGION":
								attr = AttributeType.STOP_REGION;
								break;
							case "CENTROID":
								attr = AttributeType.STOP_CENTROID;
								break;
							case "DURATION":
								attr = AttributeType.STOP_DURATION;
								break;
							default:
								attr = AttributeType.STOP;
						}
						String semDistance = semParam.getDistance().toUpperCase();
						distance = createDistance(semParam, semDistance);
						AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
						semantic = new StopSemantic(index.intValue(), desc);
						t = grid.getThreshold(semParam);
						threshold = Strings.isNullOrEmpty(t) ? null : Double.parseDouble(t);
						break;
					}
				EDRSemanticParameter dimension = new EDRSemanticParameter(semantic, threshold);
				stopDimensions.add(dimension);
			}
			ret.add(new EDRClassifier(//
					stopDimensions.toArray(new EDRSemanticParameter[stopDimensions.size()])
					));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createWDF(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			IDistanceFunction distance = null;
			String d = measure.getConfig().get("distance");
			
			if(!Strings.isNullOrEmpty(d)) {
				distance = createDistance(null, d);
			}
			String w = measure.getConfig().get("w");
			Double window = -1d;
			if(w != null) {
				window = Double.parseDouble(w);
			}
			ret.add(new wDF(window, (SpatialDistanceFunction) distance));
		}
		return ret;
	}

	private static List<TrajectorySimilarityCalculator<SemanticTrajectory>> createDTWa(Measure measure) {
		GridSearchParams grid = new GridSearchParams();
		List<TrajectorySimilarityCalculator<SemanticTrajectory>> ret = new ArrayList<>();
		while (grid.hasNextConfigurations()) {
			List<Param> params = measure.getParams();
			List<Semantic> stopDimensions = new ArrayList<>();
			Semantic discriminatorSemantic = null;
			for (Param param : params) {
				AttributeType attr = null;
				String d = param.getDistance();
				
				IDistanceFunction distance = null;
				if(!Strings.isNullOrEmpty(d)) {
					distance = createDistance(param, d);
				}
				String type = param.getType().toUpperCase();
				Long index = param.getIndex();
				Semantic semantic = null;
				switch(type) {
					case "DISCRIMINATOR":
						discriminatorSemantic = new BasicSemantic<>(index.intValue());
						continue;
					case "SPATIAL":
						attr = AttributeType.STOP_SPATIAL;
						switch(param.getDistance().toUpperCase()) {
						case "LATLON":
							semantic = Semantic.SPATIAL_LATLON;
							break;
						case "EUCLIDEAN":
							semantic = Semantic.SPATIAL_EUCLIDEAN;
							break;
						}
						break;
					case "TEMPORAL":
						attr = AttributeType.STOP_TEMPORAL;

						switch(param.getDistance().toUpperCase()) {
							case "TIMESTAMP":
							case "PROPORTION":
								semantic = Semantic.TEMPORAL;
								break;
							case "SLACK-PROPORTION":
								Param slackParams = param.getParams().get(0);
								Double slack = Double.parseDouble(grid.getThreshold(slackParams));
								semantic = new SlackTemporalSemantic(index.intValue(), slack);
								break;
						}
						break;
					case "SEMANTIC":
						List<Param> semanticParams = param.getParams();
						Param semParam = semanticParams.get(0);
						String semType = semParam.getType().toUpperCase();
						switch(semType) {
							case "NAME":
								attr = AttributeType.STOP_NAME;
								break;
							case "REGION":
								attr = AttributeType.STOP_REGION;
								break;
							case "CENTROID":
								attr = AttributeType.STOP_CENTROID;
								break;
							case "DURATION":
								attr = AttributeType.STOP_DURATION;
								break;
							default:
								attr = AttributeType.STOP;
						}
						String semDistance = semParam.getDistance().toUpperCase();
						distance = createDistance(semParam, semDistance);
						AttributeDescriptor desc = new AttributeDescriptor<>(attr, distance);
						semantic = new ComparableStopSemantic(new StopSemantic(index.intValue(), desc));
						break;
				}
				
				stopDimensions.add(semantic);
			}
			ret.add(new DTWaClassifier<>(discriminatorSemantic, stopDimensions.toArray(new Semantic[stopDimensions.size()])));
			
		}
		return ret;
	}

	private static IDistanceFunction createDistance(Param stopParam, String d) {
		IDistanceFunction distance;
		String paramDistance = d.toUpperCase();
		switch(paramDistance) {
			case "LATLON":
				distance = new LatLongDistanceFunction();
				break;
			case "EUCLIDEAN":
				distance = new EuclideanDistanceFunction();
				break;
			case "EQUAL":
				distance = new EqualsDistanceFunction<>();
				break;
			case "ANGLE":
				distance = new AngleDistance();
				break;
			case "NUMBER":
				distance = new NumberDistance();
				break;
			case "TIMESTAMP":
				distance = new TimestampDistance();
				break;
			case "PROPORTION":
				distance = new ProportionDistance();
				break;
			case "DTW":
			case "UMS":
				List<Param> dtwParams = stopParam.getParams();
				Param dtwParam = dtwParams.get(0);
				String dtwDistance = dtwParam.getDistance().toUpperCase();
				SpatialDistanceFunction spatialDistance = null;
				switch(dtwDistance) {
				case "LATLON":
					spatialDistance = new LatLongDistanceFunction();
					break;
				case "EUCLIDEAN":
					spatialDistance = new EuclideanDistanceFunction();
					break;
				}
				if(paramDistance.equals("UMS")) {
					distance = new EllipsesDistance(spatialDistance);
				} else {
					distance = new DTWDistance(spatialDistance);
				}
				break;
			default:
				distance = new EqualsDistanceFunction<>();
		}
		return distance;
	}

}
