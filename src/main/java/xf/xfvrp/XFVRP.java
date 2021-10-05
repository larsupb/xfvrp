package xf.xfvrp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import xf.xfvrp.base.*;
import xf.xfvrp.base.exception.XFVRPException;
import xf.xfvrp.base.exception.XFVRPExceptionType;
import xf.xfvrp.base.monitor.StatusCode;
import xf.xfvrp.base.xfvrp.XFVRPBase;
import xf.xfvrp.base.xfvrp.XFVRPData;
import xf.xfvrp.opt.*;
import xf.xfvrp.opt.fleetmix.IMixedFleetHeuristic;
import xf.xfvrp.opt.init.ModelBuilder;
import xf.xfvrp.opt.init.precheck.PreCheckService;
import xf.xfvrp.opt.init.solution.InitialSolutionBuilder;
import xf.xfvrp.report.Report;
import xf.xfvrp.report.build.ReportBuilder;

/** 
 * Copyright (c) 2012-2021 Holger Schneider
 * All rights reserved.
 *
 * This source code is licensed under the MIT License (MIT) found in the
 * LICENSE file in the root directory of this source tree.
 *
 *
 * XFVRP is central user interface for this suite. 
 * It combines all methods for data import, optimization execution, parameters and
 * retrieval of solutions.
 * 
 * The modeling of this class represents a state machine, where iteratively several 
 * methods must be called. The execution method take all inserted data and parameters
 * and start the optimizers.
 * 
 * 
 * @author hschneid
 * 
 */
public class XFVRP extends XFVRPBase {
	private final XFVRPParameter parameters = new XFVRPParameter();
	private final XFVRPData data = new XFVRPData();
	
	/* List of optimization procedures */
	private final List<XFVRPOptBase> optList = new ArrayList<>();

	/* Last model for the last created solution */
	private XFVRPModel lastModel;

	/* Solutions - List of generated solutions per each vehicle type*/
	private final List<XFVRPSolution> vehicleSolutionList = new ArrayList<>();

	/**
	 * Calculates the VRP with the before inserted data
	 * by addDepot(), addCustomer(), addMetric() and 
	 * addVehicle() or the parameters setCapacity() and setMaxRouteDuration()
	 */
	public void executeRoutePlanning() throws XFVRPException {
		statusManager.fireMessage(StatusCode.RUNNING, "XFVRP started");
		statusManager.setStartTime();

		// Flush import buffer
		data.getImporter().finishImport();
		vehicleSolutionList.clear();

		// Copy imported data to internal data structure
		Vehicle[] vehicles = data.getImporter().getVehicles();
		Node[] nodes = data.getImporter().getNodes(vehicles, statusManager);

		// Check of input parameter
		if(data.getImporter().getDepotList().size() == 0) {
			statusManager.fireMessage(StatusCode.ABORT, "No depot is given.");
			throw new XFVRPException(XFVRPExceptionType.ILLEGAL_INPUT, "No depot is given.");
		}
		if(vehicles.length == 0) {
			statusManager.fireMessage(StatusCode.ABORT, "No vehicle information are present.");
			throw new XFVRPException(XFVRPExceptionType.ILLEGAL_INPUT, "No vehicle information are present.");
		}
		
		IMixedFleetHeuristic heuristic = getParameters().getMixedFleetHeuristic();
		Collection<XFVRPSolution> solutions = heuristic.execute(nodes, vehicles, this::executeRoutePlanning,
				data.getMetric(), parameters, statusManager);
		
		vehicleSolutionList.addAll(solutions);

		statusManager.fireMessage(StatusCode.FINISHED, "XFVRP finished successfully.");
	}

	/**
	 * Calculates a single vehicle VRP for a given vehicle with all
	 * announced optimization procedures.
	 */
	private XFVRPSolution executeRoutePlanning(RoutingDataBag dataBag) throws XFVRPException {
		Node[] nodes = new PreCheckService().precheck(dataBag.nodes, dataBag.vehicle, parameters);
		XFVRPModel model = new ModelBuilder().build(nodes, dataBag.vehicle, data.getMetric(), parameters, statusManager);
		Solution solution = new InitialSolutionBuilder().build(model, parameters, statusManager);

		// VRP optimizations, if initiated solution has appropriate length
		if (solution.getGiantRoute().length > 0) {
			/*
			 * For each given optimization procedure the current
			 * solution plan is searched for optimizations. If solution
			 * splitting is allowed, big solution plans with a big
			 * number of routes is splitted into smaller solution plans.
			 * This is a speed up.
			 */
			XFVRPOptSplitter splitter = new XFVRPOptSplitter();
			for (XFVRPOptBase xfvrp : optList) {
				statusManager.fireMessage(StatusCode.RUNNING, "Optimization for algorithm " + xfvrp.getClass().getSimpleName() + " started.");

				try {
					if (parameters.isRouteSplittingAllowed() && xfvrp.isSplittable)
						solution = splitter.execute(solution, model, statusManager, xfvrp);
					else
						solution = xfvrp.execute(solution, model, statusManager);
				} catch (UnsupportedOperationException usoex) {
					statusManager.fireMessage(StatusCode.EXCEPTION, "Splitting encountert problem:\n" + usoex.getMessage());
				}
			}

			// Normalization of last result
			NormalizeSolutionService.normalizeRoute(solution, model);
		}

		lastModel = model;
		return new XFVRPSolution(solution, model);
	}

	/**
	 * Uses the last planned solution and turns it
	 * into a report representation.
	 * 
	 * All route plan informations can be akquired by this report.
	 * 
	 * @return A report data structure with detailed information about the route plan or null if no solution was calculated.
	 */
	public Report getReport() throws XFVRPException {
		if(vehicleSolutionList.size() > 0) {

			Report rep = new Report(lastModel);
			for (XFVRPSolution sol : vehicleSolutionList)
				rep.importReport(new ReportBuilder().getReport(sol));

			return rep;
		}
		return null;
	}

	/**
	 * Adds a certain optimization algorithm out
	 * of the spectrum of accessible methods in
	 * the enumeration XFVRPOptType.
	 * 
	 * @param type algorithm type. it can't be null.
	 */
	public void addOptType(XFVRPOptType type) throws XFVRPException {
		if(type != null)
			optList.add(type.createInstance());
	}

	/**
	 * Clears all added optimization algorithms
	 */
	public void clearOptTypes() {
		optList.clear();
	}
	
	public XFVRPParameter getParameters() {
		return parameters;
	}
	
	public XFVRPData getData() {
		return data;
	}
}
