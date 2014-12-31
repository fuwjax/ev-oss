package org.echovantage.util.function;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Created by fuwjax on 12/30/14.
 */
public class Deferred<T> implements Supplier<T> {
    private class Block implements Supplier<T> {
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        @Override
        public T get() {
            lock.lock();
            try {
                if (value.get() == this) {
                    condition.await();
                }
                return value.get().get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UnsafeException(e, "Interrupted while initializing a deferred object");
            } finally {
                lock.unlock();
            }
        }

        public void unblock() {
            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    private final Supplier<T> init;
    private final AtomicReference<Supplier<T>> value = new AtomicReference<>();

    public Deferred(final Supplier<T> init) {
        this.init = init;
    }

    @Override
    public T get() {
        Supplier<T> p = value.get();
        if (p != null) {
            return p.get();
        }
        Block block = new Block();
        if (value.compareAndSet(null, block)) {
            try {
                final T v = init.get();
                if(value.compareAndSet(block, () -> v)){
                    return v;
                }
            } catch (final RuntimeException e) {
                value.compareAndSet(block, () -> {
                    throw e;
                });
                throw e;
            } finally {
                block.unblock();
            }
        }
        return value.get().get();
    }
}
