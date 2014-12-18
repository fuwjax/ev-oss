package org.echovantage.wonton.standard;

import org.echovantage.wonton.Wonton;

public class NullWonton extends AbstractWonton implements Wonton.WVoid {
    @Override
    public String toString() {
        return "null";
    }
}
