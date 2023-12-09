package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PagesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Pages getPagesSample1() {
        return new Pages().id(1L).title("title1").slug("slug1");
    }

    public static Pages getPagesSample2() {
        return new Pages().id(2L).title("title2").slug("slug2");
    }

    public static Pages getPagesRandomSampleGenerator() {
        return new Pages().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).slug(UUID.randomUUID().toString());
    }
}
