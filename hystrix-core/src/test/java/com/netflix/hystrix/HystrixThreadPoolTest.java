/*
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.hystrix;

import com.netflix.hystrix.HystrixThreadPool.Factory;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler;
import org.junit.Before;
import org.junit.Test;
import rx.Scheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HystrixThreadPoolTest {
    @Before
    public void setup() {
        Hystrix.reset();
    }

    @Test
    public void testShutdown() {
        // other unit tests will probably have run before this so get the count
        int count = Factory.threadPools.size();

        HystrixThreadPool pool = Factory.getInstance(HystrixThreadPoolKey.Factory.asKey("threadPoolFactoryTestShutdown"),
                HystrixThreadPoolPropertiesTest.getUnitTestPropertiesBuilder());

        assertEquals(count + 1, Factory.threadPools.size());
        assertFalse(pool.getExecutor().isShutdown());

        Factory.shutdown();

        // ensure all pools were removed from the cache
        assertEquals(0, Factory.threadPools.size());
        assertTrue(pool.getExecutor().isShutdown());
    }

    @Test
    public void testShutdownWithWait() {
        // other unit tests will probably have run before this so get the count
        int count = Factory.threadPools.size();

        HystrixThreadPool pool = Factory.getInstance(HystrixThreadPoolKey.Factory.asKey("threadPoolFactoryTestShutdownWithWait"),
                HystrixThreadPoolPropertiesTest.getUnitTestPropertiesBuilder());

        assertEquals(count + 1, Factory.threadPools.size());
        assertFalse(pool.getExecutor().isShutdown());

        Factory.shutdown(1, TimeUnit.SECONDS);

        // ensure all pools were removed from the cache
        assertEquals(0, Factory.threadPools.size());
        assertTrue(pool.getExecutor().isShutdown());
    }

    @Test
    public void ensureThreadPoolInstanceIsTheOneRegisteredWithMetricsPublisherAndThreadPoolCache() {
        HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory.asKey("threadPoolFactoryConcurrencyTest");
        HystrixThreadPool poolOne = new HystrixThreadPool.HystrixThreadPoolDefault(
                threadPoolKey, HystrixThreadPoolPropertiesTest.getUnitTestPropertiesBuilder());
        HystrixThreadPool poolTwo = new HystrixThreadPool.HystrixThreadPoolDefault(
                threadPoolKey, HystrixThreadPoolPropertiesTest.getUnitTestPropertiesBuilder());

        assertThat(poolOne.getExecutor(), is(poolTwo.getExecutor())); //Now that we get the threadPool from the metrics object, this will always be equal

        //Now the HystrixThreadPool ALWAYS has the same reference to the ThreadPoolExecutor so that it no longer matters which
        //wins to be inserted into the HystrixThreadPool.Factory.threadPools cache.
    }

    @Test(timeout = 2500)
    public void testUnsubscribeHystrixThreadPool() throws InterruptedException {
        // methods are package-private so can't test it somewhere else
        HystrixThreadPool pool = Factory.getInstance(HystrixThreadPoolKey.Factory.asKey("threadPoolFactoryTest"),
                HystrixThreadPoolPropertiesTest.getUnitTestPropertiesBuilder());
        
        final AtomicBoolean interrupted = new AtomicBoolean();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(1);

        HystrixContextScheduler hcs = new HystrixContextScheduler(HystrixPlugins.getInstance().getConcurrencyStrategy(), pool);

        Scheduler.Worker w = hcs.createWorker();

        try {
            w.schedule(() -> {
                start.countDown();
                try {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        interrupted.set(true);
                    }
                } finally {
                    end.countDown();
                }
            });
            
            start.await();
            
            w.unsubscribe();
            
            end.await();
            
            Factory.shutdown();
            
            assertTrue(interrupted.get());
        } finally {
            w.unsubscribe();
        }
    }

}
