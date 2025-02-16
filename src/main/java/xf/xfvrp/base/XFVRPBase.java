package xf.xfvrp.base;

import xf.xfvrp.base.exception.XFVRPException;
import xf.xfvrp.base.monitor.StatusManager;
import xf.xfvrp.opt.Solution;

/** 
 * Copyright (c) 2012-2021 Holger Schneider
 * All rights reserved.
 *
 * This source code is licensed under the MIT License (MIT) found in the
 * LICENSE file in the root directory of this source tree.
 *
 *
 * XFVRPBase is the root class of all optimization methods in this
 * framework. All these methods inherit the specified model,
 * the methods for accessing distances and travel times. 
 * 
 **/
public abstract class XFVRPBase<M extends XFVRPModel> {

	protected M model;
	protected StatusManager statusManager;

	/**
	 * Executes the planning or optimization method
	 */
	protected abstract Solution execute(Solution giantTour) throws XFVRPException;

	/**
	 * Sets necessary variables and 
	 * Executes afterwards the planning or optimization method
	 */
	public Solution execute(Solution giantRoute, M model, StatusManager statusManager) throws XFVRPException {
		this.model = model;
		this.statusManager = statusManager;
		
		return execute(giantRoute);
	}

	/**
	 * Returns the traveltime between two given XFNodes
	 */
	public float getTime(Node n1, Node n2) {
		return model.getTime(n1, n2);
	}

	/**
	 * Returns the distance between two given XFNodes
	 */
	public float getDistance(Node n1, Node n2) {
		return model.getDistance(n1, n2);
	}
	
	/**
	 * Sets the model for next optimization directly. 
	 * 
	 * For standard usage use method 'execute'
	 */
	public void setModel(M model) {
		this.model = model;
	}

	/**
	 * Sets the status manager for next optimization directly. 
	 * 
	 * For standard usage use method 'execute'
	 */
	public void setStatusManager(StatusManager statusManager) {
		this.statusManager = statusManager;
	}

}
