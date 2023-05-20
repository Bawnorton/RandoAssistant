package com.bawnorton.randoassistant.util.tuples;

import java.io.Serializable;

public record Quartet<A, B, C, D>(A a, B b, C c, D d) implements Serializable {
    public static <A, B, C, D> Quartet<A, B, C, D> of(A a, B b, C c, D d) {
        return new Quartet<>(a, b, c, d);
    }
}
