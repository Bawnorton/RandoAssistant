package com.bawnorton.randoassistant.util;

import java.io.Serializable;

public record Triplet<A, B, C>(A a, B b, C c) implements Serializable {
    public static <A, B, C> Triplet<A, B, C> of(A a, B b, C c) {
        return new Triplet<>(a, b, c);
    }
}
