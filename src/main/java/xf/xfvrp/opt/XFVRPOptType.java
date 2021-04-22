package xf.xfvrp.opt;

import xf.xfvrp.base.exception.XFVRPException;
import xf.xfvrp.base.exception.XFVRPExceptionType;
import xf.xfvrp.opt.construct.XFVRPConst;
import xf.xfvrp.opt.construct.XFVRPSavings;
import xf.xfvrp.opt.construct.insert.XFPDPFirstBestInsert;
import xf.xfvrp.opt.construct.insert.XFVRPFirstBestInsert;
import xf.xfvrp.opt.improve.*;
import xf.xfvrp.opt.improve.ils.XFPDPILS;
import xf.xfvrp.opt.improve.ils.XFVRPILS;
import xf.xfvrp.opt.improve.routebased.move.XFVRPSegmentMove;
import xf.xfvrp.opt.improve.routebased.move.XFVRPSingleMove;

/** 
 * Copyright (c) 2012-present Holger Schneider
 * All rights reserved.
 *
 * This source code is licensed under the MIT License (MIT) found in the
 * LICENSE file in the root directory of this source tree.
 *
 *
 * This enumeration holds all for the user accessible optimization
 * methods.
 * 
 * @author hschneid
 *
 */
public enum XFVRPOptType {
	
	SAVINGS(XFVRPSavings.class),
	CONST(XFVRPConst.class),
	FIRST_BEST(XFVRPFirstBestInsert.class),
	OPT2(XFVRP2Opt.class),
	OPT2_INTRA(XFVRP2OptIntra.class),
	SWAP(XFVRPSwap.class),
	SWAPSEGMENT(XFVRPSwapSegment.class),
	SWAPSEGMENT_WITH_INVERT(XFVRPSwapSegmentWithInvert.class),
	SWAPSEGMENT_EQ(XFVRPSwapSegmentEqual.class),
	RELOCATE(XFVRPSingleMove.class),
	PATH_RELOCATE(XFVRPSegmentMove.class),
	OPT3(XFVRP3Opt.class),
	OPT3_POINTMOVE(XFVRP3PointMove.class),
	ILS(XFVRPILS.class),
	PATH_EXCHANGE(XFVRPPathExchange.class),
	
	PDP_CHEAPEST_INSERT(XFPDPFirstBestInsert.class),
	PDP_RELOCATE(XFPDPRelocate.class),
	PDP_ILS(XFPDPILS.class);
	
	private Class<? extends XFVRPOptBase> clazz; 
	
	/**
	 * 
	 * @param clazz
	 */
	private XFVRPOptType(Class<? extends XFVRPOptBase> clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * Creates an instance of the chosen opt type class in clazz.
	 * 
	 * @return An object instance
	 */
	public XFVRPOptBase createInstance() throws XFVRPException {
		try {
			return (XFVRPOptBase) Class.forName(clazz.getName()).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new XFVRPException(XFVRPExceptionType.ILLEGAL_STATE, "no copy of optimization procedure possible", e);
		}
	}
}
