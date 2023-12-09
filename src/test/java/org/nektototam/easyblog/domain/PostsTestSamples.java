package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PostsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Posts getPostsSample1() {
        return new Posts().id(1L).title("title1").slug("slug1");
    }

    public static Posts getPostsSample2() {
        return new Posts().id(2L).title("title2").slug("slug2");
    }

    public static Posts getPostsRandomSampleGenerator() {
        return new Posts().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).slug(UUID.randomUUID().toString());
    }
}
