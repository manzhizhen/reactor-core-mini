package reactor.core.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;

import java.util.Objects;
import java.util.function.Supplier;

public class MonoDefer<T> extends Mono<T> implements Publisher<T> {

    final Supplier<? extends Mono<? extends T>> supplier;

    MonoDefer(Supplier<? extends Mono<? extends T>> supplier) {
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        Mono<? extends T> p;

        try {
            p = Objects.requireNonNull(supplier.get(), "The Mono returned by the supplier is null");
        } catch (Throwable e) {
            actual.onError(e);
            return;
        }

        p.subscribe(actual);
    }
}
