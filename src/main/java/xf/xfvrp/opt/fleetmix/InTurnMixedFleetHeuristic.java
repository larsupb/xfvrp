package xf.xfvrp.opt.fleetmix;

import java.util.*;
import java.util.stream.Collectors;

import xf.xfvrp.RoutingDataBag;
import xf.xfvrp.base.Node;
import xf.xfvrp.base.SiteType;
import xf.xfvrp.base.Vehicle;
import xf.xfvrp.base.XFVRPParameter;
import xf.xfvrp.base.exception.XFVRPException;
import xf.xfvrp.base.metric.Metric;
import xf.xfvrp.base.monitor.StatusCode;
import xf.xfvrp.base.monitor.StatusManager;
import xf.xfvrp.base.preset.VehiclePriorityInitialiser;
import xf.xfvrp.opt.XFVRPSolution;
import xf.xfvrp.report.Report;
import xf.xfvrp.report.RouteReport;

/**
 * InTurnMixedFleetHeuristic aims to distribute the set of generated routes fairly to the set of different vehicles
 *
 *
 */
public class InTurnMixedFleetHeuristic extends MixedFleetHeuristicBase implements IMixedFleetHeuristic{
	List<XFVRPSolution> vehicleSolutions;
	String fallbackVehicleName;
	Vehicle fallbackVehicle;
	
	@Override
	public List<XFVRPSolution> execute(Node[] nodes, Vehicle[] vehicles, RoutePlanningFunction routePlanningFunction,
									   Metric metric, XFVRPParameter parameter, StatusManager statusManager)
	throws XFVRPException {
		List<Node> unplannedNodes = Arrays.asList(nodes);
		
		if (fallbackVehicleName != null) {
			fallbackVehicle = Arrays.stream(vehicles).filter(v -> Objects.equals(v.getName(), fallbackVehicleName)).findFirst().get();
			vehicles = Arrays.stream(vehicles).filter(v -> !Objects.equals(v.getName(), fallbackVehicleName)).toArray(Vehicle[]::new);
		}
		vehicles = VehiclePriorityInitialiser.execute(vehicles);
		
		vehicleSolutions = new ArrayList<>();
		
		int[] vehCounts = Arrays.stream(vehicles).mapToInt(Vehicle::getNbrOfAvailableVehicles).toArray();
		int i = 0;
		while (countCustomersLeft(unplannedNodes) > 0 && countVehiclesLeft(vehCounts) > 0) {
			if (vehCounts[i] == 0) continue;
			
			Vehicle veh = vehicles[i];
			statusManager.fireMessage(StatusCode.RUNNING, "Run with vehicle " + veh.getName() + " started.");
			
			Vehicle instance = new Vehicle(veh);
			instance.setNbrOfAvailableVehicles(1);
			
			unplannedNodes = route(routePlanningFunction, unplannedNodes, instance);
			
			vehCounts[i] = vehCounts[i] - 1;
			i = (i+1) % vehicles.length;
		}
		
		if (countCustomersLeft(unplannedNodes) > 0 && fallbackVehicleName != null) {
			statusManager.fireMessage(StatusCode.RUNNING, "No vehicle left and not every customer assigned -> using fallback vehicle");
			unplannedNodes = route(routePlanningFunction, unplannedNodes, fallbackVehicle);
		}
		
		// Insert invalid and unplanned nodes into solution
		XFVRPSolution unplannedNodesSolution = insertUnplannedNodes(unplannedNodes, metric, parameter, statusManager);
		if(unplannedNodesSolution != null)
			vehicleSolutions.add(unplannedNodesSolution);
		
		return vehicleSolutions;
	}
	
	private List<Node> route(RoutePlanningFunction routePlanningFunction, List<Node> unplannedNodes, Vehicle instance) {
		// Optimize all nodes with current vehicle type
		XFVRPSolution solution = routePlanningFunction.apply(new RoutingDataBag(unplannedNodes.toArray(new Node[0]), instance));
		
		// Point out best routes for this vehicle type
		InTurnMixedFleetSelector selector = getSelector();
		selector.setNumberOfRoutes(instance.getNbrOfAvailableVehicles());
		
		List<RouteReport> bestRoutes = selector.getBestRoutes(instance, getReportBuilder().getReport(solution));
		
		if (bestRoutes.size() > 0) {
			// Add selected routes to overall best solution
			vehicleSolutions.add(reconstructGiantRoute(bestRoutes, solution.getModel()));
			
			// Remove customers from best routes for next planning stage
			unplannedNodes = getUnusedNodes(bestRoutes, unplannedNodes);
		}
		return unplannedNodes;
	}
	
	public void setFallbackVehicleName(String vehicleName) {
		this.fallbackVehicleName = vehicleName;
	}
	
	private int countVehiclesLeft(int[] vehCounts) {
		return Arrays.stream(vehCounts).sum();
	}
	
	private long countCustomersLeft(List<Node> unplannedNodes) {
		return unplannedNodes.stream().filter(f -> f.getSiteType().equals(SiteType.CUSTOMER)).count();
	}
	
	/**
	 * Custom implementation of a "best" route selector
	 * @return the route with the highest density of stops
	 */
	@Override
	public InTurnMixedFleetSelector getSelector() {
		return new InTurnMixedFleetSelector();
	}
	
	private static class InTurnMixedFleetSelector implements IMixedFleetSelector {
		int numberOfRoutes;
		
		@Override
		public List<RouteReport> getBestRoutes(Vehicle veh, Report rep) {
			List<RouteReport> routes = rep.getRoutes()
					.stream()
					.sorted(Comparator.comparingDouble(this::getStopDensity))
					.collect(Collectors.toList());
			List<RouteReport> bestRoutes = new ArrayList<>();
			for (int i = 0; i < routes.size(); i++) {
				if (i < numberOfRoutes)
					bestRoutes.add(routes.get(i));
			}
			return bestRoutes;
		}
		
		private float getStopDensity(RouteReport r) {
			return r.getSummary().getDistance() / r.getSummary().getNbrOfStops();
		}
		
		public void setNumberOfRoutes(int numberOfRoutes) {
			this.numberOfRoutes = numberOfRoutes;
		}
	}
}