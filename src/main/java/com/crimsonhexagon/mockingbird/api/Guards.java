package com.crimsonhexagon.mockingbird.api;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * {@link Guards} provides value checking guards. Normally these guards will throw
 * {@link RuntimeException runtime exceptions}.
 */
class Guards {

	/**
	 * Requires an array and all of its elements to be non-null.
	 *
	 * @param array {@code T[]} array
	 * @param <T> array element type
	 * @throws NullPointerException when {@code array} is {@code null}
	 * @throws IllegalArgumentException when any element of the array is {@code null}
	 */
	static <T> void requireAllNonNull(T[] array) throws NullPointerException, IllegalArgumentException {
		requireNonNull(array, "array is null");
		for (int i = 0; i < array.length; i++)
			if (array[i] == null)
				throw new IllegalArgumentException(format("array[%d] is null", i));
	}
}
