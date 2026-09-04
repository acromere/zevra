package com.acromere.util;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadInfoNameComparatorTest {

	@Test
	void testCompareWithNulls() {
		ThreadInfoNameComparator comparator = new ThreadInfoNameComparator();

		assertThat( comparator.compare( null, null ) ).isZero();

		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadInfo currentThreadInfo = bean.getThreadInfo( Thread.currentThread().getId() );

		assertThat( comparator.compare( null, currentThreadInfo ) ).isNegative();
		assertThat( comparator.compare( currentThreadInfo, null ) ).isPositive();
	}

	@Test
	void testCompareThreadInfos() throws InterruptedException {
		ThreadInfoNameComparator comparator = new ThreadInfoNameComparator();
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();

		CountDownLatch latch = new CountDownLatch( 1 );
		AtomicReference<ThreadInfo> infoA = new AtomicReference<>();
		AtomicReference<ThreadInfo> infoB = new AtomicReference<>();

		Thread threadA = new Thread( () -> {
			infoA.set( bean.getThreadInfo( Thread.currentThread().getId() ) );
			try {
				latch.await( 2, TimeUnit.SECONDS );
			} catch( InterruptedException ignored ) {}
		}, "Thread-Alpha" );

		Thread threadB = new Thread( () -> {
			infoB.set( bean.getThreadInfo( Thread.currentThread().getId() ) );
			try {
				latch.await( 2, TimeUnit.SECONDS );
			} catch( InterruptedException ignored ) {}
		}, "thread-beta" );

		threadA.start();
		threadB.start();

		threadA.join( 1000 );
		threadB.join( 1000 );
		latch.countDown();

		assertThat( infoA.get() ).isNotNull();
		assertThat( infoB.get() ).isNotNull();

		assertThat( comparator.compare( infoA.get(), infoB.get() ) ).isNegative();
		assertThat( comparator.compare( infoB.get(), infoA.get() ) ).isPositive();
		assertThat( comparator.compare( infoA.get(), infoA.get() ) ).isZero();
	}

}
