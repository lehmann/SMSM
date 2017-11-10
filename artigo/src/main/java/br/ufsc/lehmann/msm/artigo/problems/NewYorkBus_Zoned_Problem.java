package br.ufsc.lehmann.msm.artigo.problems;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufsc.core.trajectory.Semantic;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.core.trajectory.StopSemantic;
import cc.mallet.util.ArrayUtils;
import smile.math.Random;

public class NewYorkBus_Zoned_Problem extends NewYorkBusProblem {
	
	private List<SemanticTrajectory> data;
	private List<SemanticTrajectory> trainingData;
	private List<SemanticTrajectory> testingData;
	private List<SemanticTrajectory> validatingData;
	private boolean loaded;
	private String[] zones;
	private Random random = new Random();
	private StopSemantic stopSemantic;
	private boolean onlyStops;
	private boolean withDirection;
	private StopMoveStrategy strategy;
	
	public NewYorkBus_Zoned_Problem(String... lines) {
		this(NewYorkBus_Zoned_DatabaseReader.STOP_CENTROID_SEMANTIC, lines);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, String... zones) {
		this(stopSemantic, false, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, boolean onlyStops, String... zones) {
		this(stopSemantic, StopMoveStrategy.CBSMoT, onlyStops, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, String... zones) {
		this(stopSemantic, strategy, onlyStops, false, zones);
	}
	
	public NewYorkBus_Zoned_Problem(StopSemantic stopSemantic, StopMoveStrategy strategy, boolean onlyStops, boolean withDirection, String... zones) {
		this.stopSemantic = stopSemantic;
		this.strategy = strategy;
		this.onlyStops = onlyStops;
		this.withDirection = withDirection;
		this.zones = zones;
	}
	
	@Override
	public void initialize(Random r) {
		if(!random.equals(r)) {
			random = r;
			loaded = false;
			load();
		}
	}

	@Override
	public List<SemanticTrajectory> data() {
		if(!loaded) {
			load();
		}
		return data;
	}

	@Override
	public Semantic discriminator() {
		if(withDirection) {
			return NewYorkBusDataReader.ROUTE_WITH_DIRECTION;
		}
		return NewYorkBusDataReader.ROUTE;
	}
	
	public StopSemantic stopSemantic() {
		return stopSemantic;
	}

	@Override
	public List<SemanticTrajectory> trainingData() {
		if(!loaded) {
			load();
		}
		return trainingData;
	}

	@Override
	public List<SemanticTrajectory> testingData() {
		if(!loaded) {
			load();
		}
		return testingData;
	}

	@Override
	public List<SemanticTrajectory> validatingData() {
		if(!loaded) {
			load();
		}
		return validatingData;
	}

	@Override
	public String shortDescripton() {
		return "New York bus " + (withDirection ? "Directed " : "") + (!org.apache.commons.lang3.ArrayUtils.isEmpty(zones)? "(zones=" + ArrayUtils.toString(zones) + ")" : "") + "[" + stopSemantic.name() + "][onlyStops=" + onlyStops + "]";
	}
	
	private void load() {
		if(loaded) {
			return;
		}
		try {
			data = new ArrayList<>(new NewYorkBusDataReader(onlyStops, strategy).read(zones));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
//		try {
//			data = new ArrayList<>(new NewYorkBus_Zoned_DatabaseReader(onlyStops).read(zones));
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
//			throw new RuntimeException(e);
//		}
		Collections.shuffle(data, new java.util.Random() {
			@Override
			public int nextInt(int bound) {
				return random.nextInt(bound);
			}
			
			@Override
			public int nextInt() {
				return random.nextInt();
			}
		});
//		data = data.subList(0, data.size() / 80);
		this.trainingData = data.subList(0, (int) (data.size() * (1.0 / 3)));
		this.testingData = data.subList((int) (data.size() * (1.0 / 3) + 1), (int) (data.size() * (2.0 / 3)));
		this.validatingData = data.subList((int) (data.size() * (2.0 / 3) + 1), data.size() - 1);
		loaded = true;
	}

}