/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.broker.scheduler.advisory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.scheduler.JobScheduler;
import org.apache.activemq.broker.scheduler.JobSchedulerStore;
import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.util.ServiceSupport;

import org.apache.activemq.broker.scheduler.SchedulerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advisory support for JobSchedulerStore
 */
public class AdvisoryJobSchedulerStore extends ServiceSupport implements JobSchedulerStore {

	private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobSchedulerStore.class);

	private final ReentrantLock lock = new ReentrantLock();
	private final Map<String, AdvisoryJobScheduler> schedulers = new HashMap<String, AdvisoryJobScheduler>();

	private final BrokerService brokerService;
	private final SchedulerUtils schedulerUtils;

	private JobSchedulerStore delegateJobSchedulerStore = null;
	
	public AdvisoryJobSchedulerStore(BrokerService brokerService, JobSchedulerStore delegateJobSchedulerStore) {
		this(brokerService);
		this.delegateJobSchedulerStore = delegateJobSchedulerStore;
	}

	public AdvisoryJobSchedulerStore(BrokerService brokerService) {
		this.brokerService = brokerService;
		this.schedulerUtils = new SchedulerUtils(brokerService);
	}

	@Override
	protected void doStop(ServiceStopper stopper) throws Exception {
		for (AdvisoryJobScheduler scheduler : schedulers.values()) {
			try {
				scheduler.stopDispatching();
			} catch (Exception e) {
				LOG.error("Failed to stop scheduler: {}", scheduler.getName(), e);
			}
		}

		if( null != delegateJobSchedulerStore) {
			delegateJobSchedulerStore.stop();
		}
	}

	@Override
	protected void doStart() throws Exception {
		if( null != delegateJobSchedulerStore) {
			delegateJobSchedulerStore.start();
		}

		for (AdvisoryJobScheduler scheduler : schedulers.values()) {
			try {
				scheduler.startDispatching();
			} catch (Exception e) {
				LOG.error("Failed to start scheduler: {}", scheduler.getName(), e);
			}
		}
	}

	@Override
	public JobScheduler getJobScheduler(String name) throws Exception {
		this.lock.lock();
		try {
			AdvisoryJobScheduler result = this.schedulers.get(name);
			if (result == null) {
				LOG.debug("Creating new advisory scheduler: {}", name);
				JobScheduler delegateJobScheduler = null;
				if(null != delegateJobSchedulerStore) {
					delegateJobScheduler = delegateJobSchedulerStore.getJobScheduler(name);
				}
				result = new AdvisoryJobScheduler(name, schedulerUtils, delegateJobScheduler);
				this.schedulers.put(name, result);
				if (isStarted()) {
					result.startDispatching();
				}
			}
			return result;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean removeJobScheduler(String name) throws Exception {
		boolean result = false;

		this.lock.lock();
		try {
			AdvisoryJobScheduler scheduler = this.schedulers.remove(name);
			result = scheduler != null;
			if (result) {
				LOG.debug("Removing advisory Job Scheduler: {}", name);
				scheduler.stopDispatching();
				this.schedulers.remove(name);
			}
		} finally {
			this.lock.unlock();
		}
		return result;
	}

	//---------- Methods that don't really apply to this implementation ------//

	@Override
	public long size() {
		return 0;
	}

	@Override
	public File getDirectory() {
		return null;
	}

	@Override
	public void setDirectory(File directory) {
	}
}
