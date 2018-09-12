package br.ufsc.lehmann.testexecution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import br.ufsc.lehmann.msm.artigo.clusterers.util.DistanceMatrix.Tuple;

public class GridSearchParams {
	
	private boolean firstExecution;
	private LinkedList<Config> configurations = new LinkedList<>();
	private List<Param> register = new ArrayList<>();

	public GridSearchParams() {
		this.firstExecution = true;
	}

	public boolean hasNextConfigurations() {
		if(firstExecution) {
			firstExecution = false;
			return true;
		}
		if(configurations.isEmpty()) {
			return false;
		}
		Optional<Config> hasConfig = configurations.stream().filter(c -> c.params.stream().allMatch(t -> !t.getLast().used)).findFirst();
		
		return hasConfig.isPresent();
	}

	public String getThreshold(Param param) {
		String threshold = param.getThreshold();
		if(threshold == null) {
			return null;
		}
		try {
			return String.valueOf(Double.parseDouble(threshold));
		} catch (NumberFormatException e) {
			//
		}
		
		Value ret = null;
		if(!configurations.isEmpty()) {
			if(register.contains(param)) {
				Optional<Config> hasConfig = configurations.stream().filter(c -> c.params.stream().filter(t -> t.getFirst() == param && !t.getLast().used).findFirst().isPresent()).findFirst();
				Tuple<Param, Value> tuple = hasConfig.get().params.stream().filter(t -> t.getFirst() == param).findFirst().get();
				ret = tuple.getLast();
				ret.used = true;
			} else {
				register.add(param);
				Gson gson = new Gson();
				Double[] multipleThresholds = gson.fromJson(threshold, Double[].class);
				LinkedList<Config> newConfigurations = new LinkedList<>();
				for (Double t : multipleThresholds) {
					for (Config config2 : configurations) {
						Config cp = config2.copy();
						cp.params.stream().forEach(tu -> tu.getLast().used = false);
						cp.params.add(new Tuple<Param, Value>(param, new Value(String.valueOf(t), false)));
						newConfigurations.add(cp);
					}
				}
				this.configurations = newConfigurations;
				Config config = configurations.getFirst();
				config.params.stream().forEach(t -> t.getLast().used = true);
				
				return String.valueOf(multipleThresholds[0]);
			}
		} else {
			register.add(param);
			Gson gson = new Gson();
			Double[] multipleThresholds = gson.fromJson(threshold, Double[].class);
			LinkedList<Config> newConfigurations = new LinkedList<>();
			for (Double t : multipleThresholds) {
				Config cp = new Config();
				cp.params.add(new Tuple<Param, Value>(param, new Value(String.valueOf(t), false)));
				newConfigurations.add(cp);
			}
			this.configurations = newConfigurations;
			Config config = configurations.getFirst();
			Optional<Tuple<Param, Value>> hasConfig = config.params.stream().filter(t -> t.getFirst() == param).findFirst();

			Tuple<Param, Value> tuple = hasConfig.get();
			ret = tuple.getLast();
			ret.used = true;
		}
		return ret.value;
	}

	private static class Config {
		List<Tuple<Param, Value>> params = new LinkedList<>();
		
		Config() {
		}
		
		public Config(List<Tuple<Param, Value>> collect) {
			params = collect;
		}

		Config copy() {
			return new Config(params.stream().map(t -> new Tuple<>(t.getFirst(), t.getLast().copy())).collect(Collectors.toList()));
		}
	}
	
	private static class Value {
		String value;
		boolean used;
		public Value(String value, boolean used) {
			this.value = value;
			this.used = used;
		}
		Value copy() {
			return new Value(value, used);
		}
	}
	
	public static void main(String[] args) {
		GridSearchParams params = new GridSearchParams();
		Param p = new Param();
		Param q = new Param();
		Param o = new Param();
		p.setThreshold("[1,2,3]");
		q.setThreshold("[10,20,30]");
		o.setThreshold("[100,200,300]");
		while(params.hasNextConfigurations()) {
			System.out.println(params.getThreshold(p));
			System.out.println(params.getThreshold(q));
			System.out.println(params.getThreshold(o));
			System.out.println("--------------------");
		}
	}
}