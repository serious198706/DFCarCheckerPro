package com.df.app.entries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsg on 14-1-6.
 *
 * 测量结果
 */

public class Measurement {

	private int blockId;

	private List<Integer> values = new ArrayList<Integer>();

	public Measurement(int blockId) {
		this.blockId = blockId;
	}

	public int getBlockId() {
		return blockId;
	}

	public Measurement setBlockId(int blockId) {
		this.blockId = blockId;
		return this;
	}

	public Measurement addValue(int value) {
		values.add(value);
		return this;
	}

	public Measurement setValue(int[] values) {
		for (int value : values) {
			this.values.add(value);
		}
		return this;
	}

	public List<Integer> getValues() {
		return values;
	}
	
	public String toValueString() {
		StringBuffer sb = new StringBuffer();

		for (int value : values) {
			sb.append(value).append(",");
		}
		
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}

		return sb.toString();
	}
}