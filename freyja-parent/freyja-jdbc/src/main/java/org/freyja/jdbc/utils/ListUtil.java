package org.freyja.jdbc.utils;

import java.util.List;

public class ListUtil {
	public static int[] toPrimitive(List<Integer> list) {
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}
}
