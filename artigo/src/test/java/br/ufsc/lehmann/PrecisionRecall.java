package br.ufsc.lehmann;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.ITrainable;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.ftsm.base.TrajectorySimilarityCalculator;
import br.ufsc.lehmann.classifier.SMSMEllipsesClassifierTest;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.AUC;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.MAP;
import br.ufsc.lehmann.msm.artigo.classifiers.validation.Validation;
import br.ufsc.lehmann.msm.artigo.problems.BasicSemantic;
import br.ufsc.lehmann.msm.artigo.problems.IDataReader;
import br.ufsc.lehmann.testexecution.Dataset;
import br.ufsc.lehmann.testexecution.Datasets;
import br.ufsc.lehmann.testexecution.ExecutionPOJO;
import br.ufsc.lehmann.testexecution.Groundtruth;
import br.ufsc.lehmann.testexecution.Measure;
import br.ufsc.lehmann.testexecution.Measures;
import smile.math.Random;

public class PrecisionRecall {


	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
		Stream<java.nio.file.Path> files = java.nio.file.Files.walk(Paths.get("./src/test/resources/crawdad"));
		files.filter(path -> path.toFile().isFile() && path.toFile().toString().endsWith(".test")).forEach(path -> {
			String fileName = path.toString();
			System.out.printf("Executing file %s\n", fileName);
			
			executeDescriptor(fileName);
		});
		files.close();
	}

	private static void executeDescriptor(String fileName) {
		Random random = new Random(5);
		ExecutionPOJO execution;
		try {
			execution = new Gson().fromJson(new FileReader(fileName), ExecutionPOJO.class);
		} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Dataset dataset = execution.getDataset();
		Measure measure = execution.getMeasure();
		Groundtruth groundtruth = execution.getGroundtruth();
		IDataReader dataReader = Datasets.createDataset(dataset);
		List<SemanticTrajectory> data = dataReader.read();
		
		Collections.shuffle(data, new java.util.Random() {
			smile.math.Random rnd = new smile.math.Random();
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
		TrajectorySimilarityCalculator<SemanticTrajectory> similarityCalculator = Measures.createMeasure(measure);

		BasicSemantic<Object> groundtruthSemantic = new BasicSemantic<>(groundtruth.getIndex().intValue());
		SemanticTrajectory[] allData = data.toArray(new SemanticTrajectory[data.size()]);
		Validation validation = new Validation(groundtruthSemantic, (IMeasureDistance<SemanticTrajectory>) similarityCalculator);
		
		Stopwatch w = Stopwatch.createStarted();
		if(similarityCalculator instanceof ITrainable) {
			((ITrainable) similarityCalculator).train(Arrays.asList(allData));
		}

		double[] precisionAtRecall = validation.precisionAtRecall(similarityCalculator, allData, /*data.size() / problemDescriptor.numClasses()*/10);
		w = w.stop();
		System.out.printf("Elapsed time %d miliseconds\n", w.elapsed(TimeUnit.MILLISECONDS));
		System.out.printf("Precision@recall(%d): %s\n", /*data.size() / problemDescriptor.numClasses()*/10, ArrayUtils.toString(precisionAtRecall, "0.0"));
		double auc = AUC.precisionAtRecall(precisionAtRecall);
		double map = MAP.precisionAtRecall(precisionAtRecall);
		System.out.printf("AUC: %.2f\n", auc);
		System.out.printf("MAP: %.2f\n", map);
	}
}
