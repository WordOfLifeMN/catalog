package org.wolm.series;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeriesHelper {
	public static List<Series> withoutDuplicates(List<Series> list) {
		Set<Series> set = new HashSet<>(list);
		return new ArrayList<Series>(set);
	}
}
