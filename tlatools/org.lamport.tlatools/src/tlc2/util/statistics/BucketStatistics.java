/*******************************************************************************
 * Copyright (c) 2017 Microsoft Research. All rights reserved. 
 *
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package tlc2.util.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class BucketStatistics extends AbstractBucketStatistics implements IBucketStatistics {

	/**
	 * The amount of samples seen by this statistics. It's identical
	 * to the sum of the value of all buckets.
	 */
	private long observations;
	
	/**
	 * Instead of using an ever-growing list of samples, identical
	 * samples are counted in a bucket. E.g. the sample 5 is stored
	 * in a bucket with key 5 and a value of 2 if the sample has been
	 * seen two times.
	 * The map is thread safe, so are the values.
	 */
	private final Map<Integer, Long> buckets;

	public BucketStatistics(String aTitle) {
		super(aTitle);
		this.buckets = new HashMap<Integer, Long>();
	}

	public BucketStatistics(String aTitle, final String pkg, final String name) {
		super(aTitle, pkg, name);
		this.buckets = new HashMap<Integer, Long>();
	}

	/* (non-Javadoc)
	 * @see tlc2.util.statistics.IBucketStatistics#addSample(int)
	 */
	public void addSample(int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("Negative amount invalid");
		}
		
		Long l = buckets.get(amount);
		if(l == null) {
			buckets.put(amount, 1L);
		} else {
			buckets.replace(amount, ++l);
		}
		observations++;
	}

	/* (non-Javadoc)
	 * @see tlc2.util.statistics.AbstractBucketStatistics#getObservations()
	 */
	public long getObservations() {
		return observations;
	}

	/* (non-Javadoc)
	 * @see tlc2.util.statistics.IBucketStatistics#getSamples()
	 */
	public NavigableMap<Integer, Long> getSamples() {
		final NavigableMap<Integer, Long> res = new TreeMap<Integer, Long>();
		for (Entry<Integer, Long> entry : this.buckets.entrySet()) {
			res.put(entry.getKey(), entry.getValue());
		}
		return res;
	}

	/**
	 * Adds the observations of <code>stat</code> to this {@link BucketStatistics}.
	 */
	public void add(final IBucketStatistics stat) {
		this.observations += stat.getObservations();
		
		for (Entry<Integer, Long> entry : stat.getSamples().entrySet()) {
			final Long l = this.buckets.get(entry.getKey());
			if (l == null) {
				this.buckets.put(entry.getKey(), entry.getValue());
			} else {
				this.buckets.replace(entry.getKey(), l + entry.getValue());
			}
		}
	}
}
