package Proconnect.backProconnect.it;

import javax.sql.DataSource;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Cuenta las sentencias SQL que llegan al driver (datasource-proxy). Equivale al conteo DML de
 * {@code perf/bench/count-queries.mjs} (performance_schema): SELECT + INSERT + UPDATE + DELETE, sin
 * los {@code SET}/{@code COMMIT} de control de transacción. El contador es por hilo, y MockMvc
 * atiende la petición en el hilo del test.
 */
public final class QueryCounter {

    static final String DATASOURCE_NAME = "it-counted";

    private QueryCounter() {}

    public static void reset() {
        QueryCountHolder.clear();
    }

    /** Sentencias DML desde el último {@link #reset()}. */
    public static long dml() {
        QueryCount count = QueryCountHolder.get(DATASOURCE_NAME);
        if (count == null) {
            return 0;
        }
        return count.getSelect() + count.getInsert() + count.getUpdate() + count.getDelete();
    }

    public static long selects() {
        QueryCount count = QueryCountHolder.get(DATASOURCE_NAME);
        return count == null ? 0 : count.getSelect();
    }

    /** Envuelve el DataSource de la app con el proxy que cuenta. */
    @TestConfiguration(proxyBeanMethods = false)
    public static class Config {
        @Bean
        static BeanPostProcessor queryCountingDataSource() {
            return new BeanPostProcessor() {
                @Override
                public Object postProcessAfterInitialization(Object bean, String beanName) {
                    if (bean instanceof DataSource ds
                            && !(ds instanceof net.ttddyy.dsproxy.support.ProxyDataSource)) {
                        return ProxyDataSourceBuilder.create(ds)
                                .name(DATASOURCE_NAME)
                                .countQuery()
                                .build();
                    }
                    return bean;
                }
            };
        }
    }
}
