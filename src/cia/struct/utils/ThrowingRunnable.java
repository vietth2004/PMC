package cia.struct.utils;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
	void run() throws E;
}
