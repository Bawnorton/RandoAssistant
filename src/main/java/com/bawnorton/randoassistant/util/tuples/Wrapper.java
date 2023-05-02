package com.bawnorton.randoassistant.util.tuples;

public class Wrapper<T> {
    private T element;

    private Wrapper(T element) {
        this.element = element;
    }

    public static <T> Wrapper<T> of(T element) {
        return new Wrapper<>(element);
    }

    public static <T> Wrapper<T> ofNothing() {
        return new Wrapper<>(null);
    }

    public T get() {
        return element;
    }

    public void set(T element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return "Wrapper{" + element + "}";
    }
}
