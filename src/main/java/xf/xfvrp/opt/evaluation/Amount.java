package xf.xfvrp.opt.evaluation;

/**
 * Copyright (c) 2012-2021 Holger Schneider
 * All rights reserved.
 *
 * This source code is licensed under the MIT License (MIT) found in the
 * LICENSE file in the root directory of this source tree.
 **/
public class Amount {

	private float[] amounts;
	
	public Amount() {
	}

	public void add(float[] otherAmount) {
		init(otherAmount);
		
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] += otherAmount[i];
		}
	}
	
	public float getAmount(int idx) {
		return (amounts != null && idx < amounts.length) ? amounts[idx] : 0;
	}

	public float[] getAmounts() {
		return amounts;
	}

	public void setAmounts(float[] amounts) {
		this.amounts = amounts;
	}
	
	private void init(float[] otherAmount) {
		if(amounts == null)
			amounts = new float[otherAmount.length];
	}

	public boolean hasAmount() {
		return (amounts != null);
	}
}
