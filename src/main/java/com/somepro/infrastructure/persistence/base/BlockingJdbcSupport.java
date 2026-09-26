package com.somepro.infrastructure.persistence.base;

import com.somepro.infrastructure.config.ReactiveOperatorContext;
import com.somepro.infrastructure.persistence.audit.AuditContextHolder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

/**
 * 阻塞 JDBC → 响应式链路的桥接基类（基础设施层）。
 *
 * 各仓储适配器继承它，所有 Mapper 调用都必须包在 {@link #blocking} 里：
 * 1. 先在响应式线程上用 deferContextual 从 Reactor Context 取操作人（切线程后就取不到了）；
 * 2. 再 subscribeOn(boundedElastic) 切到阻塞线程池执行 JDBC（绝不能在 Netty event-loop 上阻塞）；
 * 3. 把操作人放进 AuditContextHolder，供 MetaObjectHandler 填充 createBy / updateBy，用完即清。
 *
 * 顺序不能颠倒：先取 Context 再 subscribeOn。
 */
public abstract class BlockingJdbcSupport {

    protected <T> Mono<T> blocking(Supplier<T> supplier) {
        return Mono.deferContextual(ctx -> {
            String operator = ReactiveOperatorContext.getOperator(ctx);
            return Mono.fromCallable(() -> {
                AuditContextHolder.setOperator(operator);
                try {
                    return supplier.get();
                } finally {
                    AuditContextHolder.clear();
                }
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }
}
