package xf.xfvrp.opt.improve.ils;

import xf.xfvrp.base.exception.XFVRPException;
import xf.xfvrp.opt.Solution;
import xf.xfvrp.opt.XFVRPOptBase;
import xf.xfvrp.opt.improve.XFPDPRelocate;

/** 
 * Copyright (c) 2012-2021 Holger Schneider
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
public class XFPDPILS extends XFILS {

	/*
	 * (non-Javadoc)
	 * @see xf.xfvrp.opt.improve.ils.XFILS#execute(xf.xfvrp.opt.Solution)
	 */
	@Override
	public Solution execute(Solution solution) throws XFVRPException {
		optArr = new XFVRPOptBase[]{
				new XFPDPRelocate()
		};
		optPropArr = new double[]{
				1
		};
		randomChangeService = new XFPDPRandomChangeService();
		
		return super.execute(solution);
	}
}
