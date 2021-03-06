package org.jbourdon.thinkgear.producer;

import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QueueSpliterator<T> implements Spliterator<T> {
    private final BlockingQueue<T> queue;
    private final long timeoutMs;

    public QueueSpliterator(final BlockingQueue<T> queue, final long timeoutMs) {
        this.queue = queue;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public int characteristics() {
        return Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        try {
            final T next = this.queue.poll(this.timeoutMs, TimeUnit.MILLISECONDS);
            if (next == null) {
                return false;
            }
            action.accept(next);
            return true;
        } catch (final InterruptedException e) {
            throw new SupplierErrorException("interrupted", e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

}