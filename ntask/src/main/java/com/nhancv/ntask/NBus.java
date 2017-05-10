package com.nhancv.ntask;

/**
 * Created by nhancao on 5/10/17.
 */

public interface NBus<D extends Object> {
    int getCode();

    D getData();
}
