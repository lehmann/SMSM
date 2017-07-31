package br.ufsc.lehmann.classifier;

import br.ufsc.core.IMeasureDistance;
import br.ufsc.core.trajectory.SemanticTrajectory;
import br.ufsc.lehmann.EnumProblem;
import br.ufsc.lehmann.method.MTMTest;
import br.ufsc.lehmann.msm.artigo.Problem;

public class MTMClassifierTest extends AbstractClassifierTest implements MTMTest {

	public MTMClassifierTest(EnumProblem problemDescriptor) {
		super(problemDescriptor);
	}

	@Override
	public IMeasureDistance<SemanticTrajectory> measurer(Problem problem) {
		return MTMTest.super.measurer(problem);
	}

}
