package br.gov.inmetro.beacon.generator;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Leitor decorador com buffer assíncrono em memória (Padrão Produtor-Consumidor / Entropy Pool).
 * Uma thread dedicada de segundo plano consome continuamente da fonte de entropia subjacente
 * e preenche uma fila bloqueante em memória RAM.
 * 
 * Isso desacopla o tempo de I/O do hardware do tempo de gravação/processamento em disco,
 * permitindo que ambas as operações trabalhem paralelamente em capacidade máxima (*pipelining*).
 */
public class BufferedDeviceEntropyReader implements IEntropyReader, AutoCloseable {

    public static final int DEFAULT_BUFFER_CAPACITY = 10_000;

    private final IEntropyReader delegate;
    private final BlockingQueue<String> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicReference<Throwable> backgroundError = new AtomicReference<>(null);
    private final Thread producerThread;

    public BufferedDeviceEntropyReader(IEntropyReader delegate) {
        this(delegate, DEFAULT_BUFFER_CAPACITY);
    }

    public BufferedDeviceEntropyReader(IEntropyReader delegate, int bufferCapacity) {
        if (delegate == null) {
            throw new IllegalArgumentException("O IEntropyReader decorado não pode ser nulo.");
        }
        int capacity = Math.max(16, bufferCapacity);
        this.delegate = delegate;
        this.queue = new ArrayBlockingQueue<>(capacity);

        this.producerThread = new Thread(this::produceLoop, "entropy-pool-producer");
        this.producerThread.setDaemon(true);
        this.producerThread.start();
    }

    private void produceLoop() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                String noise = delegate.getNoise512Bits();
                if (noise != null) {
                    while (running.get() && !Thread.currentThread().isInterrupted()) {
                        if (queue.offer(noise, 100, TimeUnit.MILLISECONDS)) {
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            if (running.get()) {
                backgroundError.compareAndSet(null, t);
            }
        } finally {
            if (running.get() && backgroundError.get() == null) {
                backgroundError.compareAndSet(null, new IOException("A fonte de entropia subjacente finalizou o fluxo de dados (EOF)."));
            }
        }
    }

    @Override
    public String getNoise512Bits() throws Exception {
        while (running.get()) {
            Throwable err = backgroundError.get();
            if (err != null && queue.isEmpty()) {
                if (err instanceof Exception) {
                    throw (Exception) err;
                }
                throw new RuntimeException("Falha na thread produtora de entropia: " + err.getMessage(), err);
            }

            String noise = queue.poll(100, TimeUnit.MILLISECONDS);
            if (noise != null) {
                return noise;
            }

            if (!producerThread.isAlive() && queue.isEmpty()) {
                if (err != null) {
                    if (err instanceof Exception) {
                        throw (Exception) err;
                    }
                    throw new RuntimeException("Thread produtora finalizada com erro: " + err.getMessage(), err);
                }
                throw new IllegalStateException("Thread produtora de entropia finalizada inesperadamente.");
            }
        }

        throw new IllegalStateException("Leitor de entropia com buffer foi encerrado.");
    }

    @Override
    public void close() throws Exception {
        running.set(false);
        if (producerThread != null) {
            producerThread.interrupt();
            try {
                producerThread.join(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        queue.clear();

        if (delegate instanceof AutoCloseable) {
            ((AutoCloseable) delegate).close();
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getCapacity() {
        return queue.remainingCapacity() + queue.size();
    }

    public boolean isRunning() {
        return running.get() && producerThread.isAlive();
    }

    public IEntropyReader getDelegate() {
        return delegate;
    }
}
