package com.github.mrhjs225.LetsPatch.patch;

import java.util.Random;

import com.github.mrhjs225.LetsPatch.coverage.CoverageManager;
import com.github.mrhjs225.LetsPatch.coverage.CoveredLine;
import com.github.mrhjs225.LetsPatch.pool.Change;
import com.github.mrhjs225.LetsPatch.pool.ChangePool;
import com.github.mrhjs225.LetsPatch.pool.ContextIdentifier;

public class NoContextPatchStrategy extends PatchStrategy {

	public NoContextPatchStrategy(CoverageManager manager, ChangePool pool, ContextIdentifier collector, Random r,
			String flMetric, String cStrategyKey, String sourceDir, String[] compileClassPathEntries, int maxContextNum, int maxChangeNum, boolean changePrior) {
		super(manager, pool, collector, r, flMetric, cStrategyKey, sourceDir, compileClassPathEntries, maxContextNum, maxChangeNum, changePrior);
	}

	@Override
	public boolean isTarget(TargetLocation loc) {
		return true;
	}

	@Override
	public TargetLocation selectLocation(){
		if(currLocIndex < locations.size()){
			LocEntry e = locations.get(currLocIndex);
			if(e.changeIds == null) {
				e.changeIds = findCandidateChanges(e.loc);
				appendLoc(e);
			}
			return e.loc;
		} else {
			if(++currLineIndex < coveredLines.size()) {
				locations.clear();
				CoveredLine cl = coveredLines.get(currLineIndex);
				if(!patcherMap.containsKey(cl.className)) {
					System.out.println("Loading Class - "+cl.className);
					String source = PatchUtils.loadSource(sourceDir, cl.className);
					ConcretizationStrategy cStrategy = StrategyFactory.getConcretizationStrategy(cStrategyKey, manager, cl.className, sourceDir, r);
					Patcher patcher = new Patcher(cl.className, source, compileClassPathEntries, new String[] { sourceDir }, this, cStrategy);
					patcherMap.put(cl.className, patcher);
				}
				if(lineLocMap.containsKey(currLineIndex)) {
					locations.addAll(lineLocMap.get(currLineIndex));
				}
				currLocIndex = 0;
				return selectLocation();
			}
			return null;
		}
	}

	@Override
	public Change selectChange(){
		if(currLocIndex < locations.size()) {
			LocEntry e = locations.get(currLocIndex);
			Change c = e.changeIds != null && e.changeIds.size() > 0 ? pool.getChange(e.changeIds.remove(0)) : null;
			if(e.changeIds.size() == 0)
				nextLoc();
			return c;
		}
		return null;
	}
}
