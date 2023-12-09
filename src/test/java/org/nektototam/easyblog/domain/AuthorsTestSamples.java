package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AuthorsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Authors getAuthorsSample1() {
        return new Authors().id(1L).name("name1").email("email1").url("url1");
    }

    public static Authors getAuthorsSample2() {
        return new Authors().id(2L).name("name2").email("email2").url("url2");
    }

    public static Authors getAuthorsRandomSampleGenerator() {
        return new Authors()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString());
    }
}
