package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class UsersTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Users getUsersSample1() {
        return new Users().id(1L).name("name1").email("email1");
    }

    public static Users getUsersSample2() {
        return new Users().id(2L).name("name2").email("email2");
    }

    public static Users getUsersRandomSampleGenerator() {
        return new Users().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString()).email(UUID.randomUUID().toString());
    }
}
