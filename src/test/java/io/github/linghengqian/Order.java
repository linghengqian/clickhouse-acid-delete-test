package io.github.linghengqian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 8306802022239174861L;

    private long orderId;

    private int orderType;

    private int userId;

    private long addressId;

    private String status;
}
