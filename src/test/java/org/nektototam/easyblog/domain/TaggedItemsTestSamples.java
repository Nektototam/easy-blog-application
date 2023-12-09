package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TaggedItemsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static TaggedItems getTaggedItemsSample1() {
        return new TaggedItems().id(1L).itemType("itemType1");
    }

    public static TaggedItems getTaggedItemsSample2() {
        return new TaggedItems().id(2L).itemType("itemType2");
    }

    public static TaggedItems getTaggedItemsRandomSampleGenerator() {
        return new TaggedItems().id(longCount.incrementAndGet()).itemType(UUID.randomUUID().toString());
    }
}
