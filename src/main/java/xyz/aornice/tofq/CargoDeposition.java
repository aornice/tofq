package xyz.aornice.tofq;

import xyz.aornice.tofq.impl.Cargo;

/**
 * Created by drfish on 09/04/2017.
 */
public interface CargoDeposition {
    void write(Cargo cargo);

    void write(Cargo[] cargo);
}
