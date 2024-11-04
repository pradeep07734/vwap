package com.utility.vwap;

import java.util.function.Function;

interface NamedFunction<T, R> extends Function<T, R> {
    String getName();
}