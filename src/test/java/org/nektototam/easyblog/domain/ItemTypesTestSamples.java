package org.nektototam.easyblog.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ItemTypesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ItemTypes getItemTypesSample1() {
        return new ItemTypes().id(1L).name("name1");
    }

    public static ItemTypes getItemTypesSample2() {
        return new ItemTypes().id(2L).name("name2");
    }

    public static ItemTypes getItemTypesRandomSampleGenerator() {
        return new ItemTypes().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString());
    }
}
