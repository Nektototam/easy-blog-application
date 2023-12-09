package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CommentsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Comments getCommentsSample1() {
        return new Comments().id(1L);
    }

    public static Comments getCommentsSample2() {
        return new Comments().id(2L);
    }

    public static Comments getCommentsRandomSampleGenerator() {
        return new Comments().id(longCount.incrementAndGet());
    }
}
