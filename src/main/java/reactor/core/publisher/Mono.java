package reactor.core.publisher;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Mono<T> implements CorePublisher<T> {

    /**
     * 延迟构建
     *
     * @param supplier
     * @return
     * @param <T>
     */
    public static <T> Mono<T> defer(Supplier<? extends Mono<? extends T>> supplier) {
        return onAssembly(new MonoDefer<>(supplier));
    }

    public abstract void subscribe(CoreSubscriber<? super T> actual);

    @Override
    @SuppressWarnings("unchecked")
    public final void subscribe(Subscriber<? super T> actual) {
        CorePublisher publisher = Operators.onLastAssembly(this);
        CoreSubscriber subscriber = Operators.toCoreSubscriber(actual);

        try {
            if (publisher instanceof OptimizableOperator) {
                OptimizableOperator operator = (OptimizableOperator) publisher;
                while (true) {
                    subscriber = operator.subscribeOrReturn(subscriber);
                    if (subscriber == null) {
                        // null means "I will subscribe myself", returning...
                        return;
                    }

                    OptimizableOperator newSource = operator.nextOptimizableSource();
                    if (newSource == null) {
                        publisher = operator.source();
                        break;
                    }
                    operator = newSource;
                }
            }

            publisher.subscribe(subscriber);
        }
        catch (Throwable e) {
            Operators.reportThrowInSubscribe(subscriber, e);
            return;
        }
    }

    /**
     * 通用的装配处理
     * @param source
     * @return
     * @param <T>
     */
    protected static <T> Mono<T> onAssembly(Mono<T> source) {
        return source;
    }
}
