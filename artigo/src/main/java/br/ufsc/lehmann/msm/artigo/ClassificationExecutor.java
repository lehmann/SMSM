package br.ufsc.lehmann.msm.artigo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.msm.artigo.NearestNeighbour.DataEntry;

public class ClassificationExecutor {

	public void classify(Problem problem, IMeasureDistance<SemanticTrajectory> measureDistance) {
		File to = new File("./src/main/resources/output_" + measureDistance.name() + "_" + problem.shortDescripton() + ".txt");
		try {
			boolean b = to.createNewFile();
			if (!b) {
				to.delete();
				b = to.createNewFile();
				if (!b) {
					throw new RuntimeException("Arquivo n�o criado");
				} else {
					System.out.println("Arquivo de sa�da: " + to.getAbsolutePath());
				}
			} else {
				System.out.println("Arquivo de sa�da: " + to.getAbsolutePath());
			}
		} catch (IOException e1) {
			throw new RuntimeException("Arquivo n�o criado", e1);
		}
		List<SemanticTrajectory> training = problem.trainingData();

		List<DataEntry<SemanticTrajectory>> entries = new ArrayList<DataEntry<SemanticTrajectory>>();
		for (SemanticTrajectory traj : training) {
			Object data = problem.discriminator().getData(traj, 0);
			entries.add(new DataEntry<SemanticTrajectory>(traj, data));
		}
		NearestNeighbour<SemanticTrajectory> nn = new NearestNeighbour<SemanticTrajectory>(entries, Math.min(training.size(), 3), measureDistance,
				false);
		ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() / 2,
				Runtime.getRuntime().availableProcessors(), 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		DelayQueue<DelayedClassification> classifications = new DelayQueue<>();
		for (SemanticTrajectory semanticTrajectory : problem.testingData()) {
			Future<Classification> submit = executorService.submit(new Callable<Classification>() {

				@Override
				public Classification call() throws Exception {
					return new Classification(semanticTrajectory, nn.classify(new DataEntry<SemanticTrajectory>(semanticTrajectory, "descubra")));
				}
			});
			classifications.add(new DelayedClassification(submit, 0));
		}
		executorService.shutdown();
		while (!classifications.isEmpty()) {
			DelayedClassification toProcess = classifications.poll();
			if (toProcess == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (!toProcess.futureClassification.isDone()) {
				classifications.add(new DelayedClassification(toProcess.futureClassification, 500/* ms */));
			} else {
				try {
					Classification classification = toProcess.futureClassification.get();
					SemanticTrajectory semanticTrajectory = classification.semanticTrajectory;
					Object classifiedAs = classification.classifiedAs;
					Object data = problem.discriminator().getData(semanticTrajectory, 0);
					if (data.equals(classifiedAs)) {
						Files.append("Traj " + semanticTrajectory.getTrajectoryId() + " correct classified\n", to, Charsets.UTF_8);
					} else {
						Files.append("Traj " + semanticTrajectory.getTrajectoryId() + " incorrect classified\n", to, Charsets.UTF_8);
					}
				} catch (ExecutionException | InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class Classification {
		SemanticTrajectory semanticTrajectory;
		Object classifiedAs;

		public Classification(SemanticTrajectory semanticTrajectory, Object classify) {
			this.semanticTrajectory = semanticTrajectory;
			classifiedAs = classify;
		}
	}

	static class DelayedClassification implements Delayed {

		private Future<Classification> futureClassification;
		private long delay;
		private long origin;

		DelayedClassification(Future<Classification> distance, int delay) {
			this.origin = System.currentTimeMillis();
			this.futureClassification = distance;
			this.delay = delay;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(delay - (System.currentTimeMillis() - origin), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed delayed) {
			if (delayed == this) {
				return 0;
			}

			if (delayed instanceof DelayedClassification) {
				long diff = delay - ((DelayedClassification) delayed).delay;
				return ((diff == 0) ? 0 : ((diff < 0) ? -1 : 1));
			}

			long d = (getDelay(TimeUnit.MILLISECONDS) - delayed.getDelay(TimeUnit.MILLISECONDS));
			return ((d == 0) ? 0 : ((d < 0) ? -1 : 1));
		}
	}
}
