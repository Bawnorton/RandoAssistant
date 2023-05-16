package com.bawnorton.randoassistant.util.tuples;

import java.io.Serializable;

public record Quadruplet<A, B, C, D>(A a, B b, C c, D d) implements Serializable {
    public static <A, B, C, D> Quadruplet<A, B, C, D> of(A a, B b, C c, D d) {
        return new Quadruplet<>(a, b, c, d);
    }
}
