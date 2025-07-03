package org.omnione.did.base.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.*;

/**
 * Verify OpenFeign Client is registered properly
 *
 * @author birariro
 */
@SpringBootTest(classes = {OpenFeignConfig.class})
class OpenFeignConfigTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    void atLeastOneFeignClientBeanShouldExist() {
        String[] feignBeans = ctx.getBeanNamesForAnnotation(FeignClient.class);

        assertThat(feignBeans).isNotEmpty();
    }
}
