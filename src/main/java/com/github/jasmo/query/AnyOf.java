/*
 * Copyright © 2017 Caleb Whiting <caleb.andrew.whiting@gmail.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the COPYING file for more details.
 */

package com.github.jasmo.query;

/**
 * @author Caleb Whiting
 */
public class AnyOf {

	private final Object[] values;

	public AnyOf(Object... values) {
		this.values = values;
	}

	public Object[] values() {
		return values;
	}

}