package microarch.delivery.core.domain.services;

import java.util.Comparator;
import java.util.List;
import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public Result<Courier, Error> assignOrder(Order order, List<Courier> couriers) {
        if (!order.isCreated()) {
            return Result.failure(Errors.cantAssignIfOrderStatusIsNotCreated());
        }
        Location orderLocation = order.getLocation();
        List<Courier> availableCouriers = couriers.stream().filter(courier -> courier.canTakeOrder(order.getVolume())).toList();
        if (availableCouriers.isEmpty()) {
            return Result.failure(Errors.allCouriersAreFullyBookedOrUnavailable());
        }

        Courier closestCourier = availableCouriers.stream().min(Comparator.comparingInt(courier -> courier.getLocation().distance(orderLocation))).orElseThrow();

        UnitResult<Error> takingOrderResult = closestCourier.takeOrder(order.getId(), orderLocation, order.getVolume());
        if (takingOrderResult.isFailure()) {
            return Result.failure(takingOrderResult.getError());
        }
        UnitResult<Error> orderAssignResult = order.assign();
        if (orderAssignResult.isFailure()) {
            return Result.failure(orderAssignResult.getError());
        }
        return Result.success(closestCourier);
    }

    public static class Errors {
        public static Error cantAssignIfOrderStatusIsNotCreated() {
            return Error.of("cant.assign.if.order.status.is.not.created", "The order must be in CREATED status");
        }

        public static Error allCouriersAreFullyBookedOrUnavailable() {
            return Error.of("all.couriers.are.fully.booked.or.unavailable", "All couriers are fully booked or unavailable");
        }
    }
}
