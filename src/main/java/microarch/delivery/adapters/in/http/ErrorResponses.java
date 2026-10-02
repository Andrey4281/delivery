package microarch.delivery.adapters.in.http;

import microarch.delivery.adapters.in.http.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ErrorResponses {
    private ErrorResponses() {
        // Utility class, no instantiation
    }

    @SuppressWarnings("unchecked")
    public static <T> ResponseEntity<T> badRequest(libs.errs.Error error) {
        var body = new Error(HttpStatus.BAD_REQUEST.value(), error.getMessage());
        return (ResponseEntity<T>) ResponseEntity.badRequest().body(body);
    }
}
