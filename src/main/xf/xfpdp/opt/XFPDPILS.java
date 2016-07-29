package xf.xfpdp.opt;

import java.util.Arrays;

import xf.xfpdp.XFPDPUtils;
import xf.xfvrp.base.Node;
import xf.xfvrp.base.Quality;
import xf.xfvrp.base.SiteType;
import xf.xfvrp.base.Util;
import xf.xfvrp.base.Vehicle;
import xf.xfvrp.base.monitor.StatusCode;
import xf.xfvrp.opt.XFVRPOptBase;

/** 
 * Copyright (c) 2012-present Holger Schneider
 * All rights reserved.
 *
 * This source code is licensed under the MIT License (MIT) found in the
 * LICENSE file in the root directory of this source tree.
 *
 *
 * Optimization procedure for iterative local search
 * 
 * Three local search procedures with adaptive randomized variable neighborhood selection.
 * 
 * @author hschneid
 *
 */
public class XFPDPILS extends XFVRPOptBase {

	private XFVRPOptBase[] optArr = new XFVRPOptBase[]{
			new XFPDPRelocate2()
	};

	private double[] optPropArr = new double[]{
			1
	};

	/*
	 * (non-Javadoc)
	 * @see de.fhg.iml.vlog.xftour.model.XFBase#execute(de.fhg.iml.vlog.xftour.model.XFNode[])
	 */
	@Override
	public Node[] execute(Node[] giantTour) {
		Node[] bestRoute = Arrays.copyOf(giantTour, giantTour.length);
		Node[] bestBestTour = Arrays.copyOf(giantTour, giantTour.length);
		Quality bestBestQ = check(giantTour);

		statusManager.fireMessage(StatusCode.RUNNING, this.getClass().getSimpleName()+" is starting with "+model.getParameter().getILSLoops()+" loops.");

		for (int i = 0; i < model.getParameter().getILSLoops(); i++) {
			Node[] gT = Arrays.copyOf(bestRoute, bestRoute.length);

			// Variation
			perturbPDP(gT, model.getVehicle());
			
			// Intensification
			gT = localSearch(gT, model.getVehicle());

			// Evaluation
			Quality q = check(gT);

			// Selection
			if(q.getFitness() < bestBestQ.getFitness()) {
				statusManager.fireMessage(StatusCode.RUNNING, this.getClass().getSimpleName()+" loop "+i+"\t last cost : "+bestBestQ.getCost()+"\t new cost : "+q.getCost());

				bestRoute = gT;
				bestBestQ = q;
				bestBestTour = gT;
			} else
				bestRoute = Util.normalizeRoute(bestRoute, model);
		}

		return Util.normalizeRoute(bestBestTour, model);
	}

	/**
	 * This perturb routine relocates single nodes iterativly. The nodes are
	 * selected randomly.
	 * 
	 * @param giantRoute
	 * @param vehicle
	 */
	private void perturbPDP(Node[] giantRoute, Vehicle vehicle) {
		int nbrOfVariations = 5;
		int[] param = new int[4];
		Node[] copy = new Node[giantRoute.length];
		
		for (int i = 0; i < nbrOfVariations; i++) {
			// Search nodes for source shipment
			// Restriction: no depot
			chooseSrcPickup(param, giantRoute);
			chooseSrcDelivery(param, giantRoute);

			// Search destination
			// Restriction: 
			//   Source is not destination
			//   Solution is not invalid
			int cnt = 0;
			boolean changed = false;
			while(true) {
				// Choose
				chooseDstPickup(param, giantRoute);
				chooseDstDelivery(param, giantRoute);
				
				// Move
				System.arraycopy(giantRoute, 0, copy, 0, giantRoute.length);
				XFPDPUtils.move(giantRoute, param[0], param[1], param[2], param[3]);
				
				// Eval
				Quality q = check(giantRoute);
				if(q.getPenalty() == 0) {
					changed = true;
					break;
				}

				// Re-Move through copy back
				System.arraycopy(copy, 0, giantRoute, 0, giantRoute.length);

				// Terminate for infinity
				if(cnt > 100)
					break;

				cnt++;
			}

			if(!changed)
				i--;
		}
	}

	/**
	 * 
	 * @param param
	 * @param giantRoute
	 */
	private void chooseSrcPickup(int[] param, Node[] giantRoute) {
		// Choose a random source node (customer or replenish)
		int src = -1;
		do {
			src = rand.nextInt(giantRoute.length - 2) + 1;
		} while(giantRoute[src].getSiteType() == SiteType.DEPOT || giantRoute[src].getDemand()[0] < 0);

		param[0] = src;
	}

	/**
	 * 
	 * @param route
	 * @return
	 */
	private void chooseSrcDelivery(int[] param, Node[] route) {
		Node srcPickup = route[param[0]];
		int shipIdx = srcPickup.getShipmentIdx();
		param[1] = -1;
		
		for (int i = 0; i < route.length; i++) {
			if(route[i].getShipmentIdx() == shipIdx && route[i].getDemand()[0] < 0) {
				param[1] = i;
				return;
			}
		}
	
		if(param[1] == -1)
			throw new IllegalStateException("Structural exception of giant route, where a pickup nopde of a shipment has no delivery node.");
	}

	/**
	 * 
	 * @param param
	 * @param giantRoute
	 */
	private void chooseDstPickup(int[] param, Node[] giantRoute) {
		param[2] = -1;
		do {
			param[2] = rand.nextInt(giantRoute.length - 1);
		} while(param[2] == param[0]);
	}
	
	/**
	 * 
	 * @param param
	 * @param giantRoute
	 */
	private void chooseDstDelivery(int[] param, Node[] giantRoute) {
		int[] routeIdxArr = new int[giantRoute.length];
		int id = 0;
		for (int i = 1; i < giantRoute.length; i++) {
			if(giantRoute[i].getSiteType() == SiteType.DEPOT)
				id++;
			routeIdxArr[i] = id;
		}
		
		int dstPickupRouteIdx = routeIdxArr[param[2]];
		
		param[3] = -1;
		do {
			param[3] = rand.nextInt(giantRoute.length - 1);
		} while(param[2] > param[3] || routeIdxArr[param[3]] != dstPickupRouteIdx);
	}

	/**
	 * 
	 * @param giantRoute
	 * @param vehicle
	 * @return
	 */
	private Node[] localSearch(Node[] giantTour, Vehicle vehicle) {
		boolean[] processedArr = new boolean[optArr.length];

		Quality q = null;
		int nbrOfProcessed = 0;
		while(nbrOfProcessed < processedArr.length) {
			// Choose
			int optIdx = choose(processedArr);

			// Process
			giantTour = optArr[optIdx].execute(giantTour, model, statusManager);

			// Check
			Quality qq = check(giantTour);
			if(q == null || qq.getFitness() < q.getFitness()) {
				q = qq;
				Arrays.fill(processedArr, false);
				nbrOfProcessed = 0;
			}

			// Mark
			processedArr[optIdx] = true;
			nbrOfProcessed++;
		}

		return giantTour;
	}

	/**
	 * 
	 * @param processedArr
	 * @return
	 */
	private int choose(boolean[] processedArr) {
		int idx = -1;
		do {
			double sum = 0;
			double r = rand.nextDouble();
			for (int j = 0; j < processedArr.length; j++) {
				sum += optPropArr[j];
				if(sum > r) {
					idx = j;
					break;
				}
			}
		} while(processedArr[idx]);

		return idx;
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	//	@SuppressWarnings("unused")
	//	private boolean checkDups(Node[] nodes) {
	//		Set<String> set = new HashSet<String>();
	//		for (Node n : nodes) {
	//			if(n.getSiteType() == SiteType.CUSTOMER) {
	//				if(set.contains(n.getExternID()))
	//					return false;
	//				set.add(n.getExternID());
	//			}
	//		}
	//
	//		return true;
	//	}
}
