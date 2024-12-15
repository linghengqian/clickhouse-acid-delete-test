package io.github.linghengqian;

public record Order(long orderId, int orderType, int userId, long addressId, String status) {
}
