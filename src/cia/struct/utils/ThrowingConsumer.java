package cia.struct.utils;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
	void consume(T t) throws E;
}
