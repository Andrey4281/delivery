package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Address;
import microarch.delivery.core.domain.model.Location;

public interface GeoClient {
    Result<Location, Error> getLocation(Address address);
}
