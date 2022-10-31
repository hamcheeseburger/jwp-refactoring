package kitchenpos.dto.request;

import kitchenpos.domain.NumberOfGuests;
import kitchenpos.domain.OrderTable;

public class OrderTableRequest {

    private int numberOfGuests;
    private boolean empty;

    public OrderTableRequest() {
    }

    public OrderTableRequest(final int numberOfGuests, final boolean empty) {
        this.numberOfGuests = numberOfGuests;
        this.empty = empty;
    }

    public OrderTable toEntity() {
        return new OrderTable(null, new NumberOfGuests(this.numberOfGuests), this.empty);
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }
}
