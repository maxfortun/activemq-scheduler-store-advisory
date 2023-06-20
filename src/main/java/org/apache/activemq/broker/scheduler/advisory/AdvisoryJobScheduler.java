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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.scheduler.Job;
import org.apache.activemq.broker.scheduler.JobListener;
import org.apache.activemq.broker.scheduler.JobScheduler;

import org.apache.activemq.util.ByteSequence;

import org.apache.activemq.broker.scheduler.SchedulerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an advisory JobScheduler instance.
 */
public class AdvisoryJobScheduler implements JobScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobScheduler.class);

	private final String name;
	private final SchedulerUtils schedulerUtils;

	private JobScheduler delegateJobScheduler = null;

	private final AtomicBoolean dispatchEnabled = new AtomicBoolean(false);
	private final Map<JobListener, AdvisoryJobListener> jobListeners = new ConcurrentHashMap<>();

	public AdvisoryJobScheduler(String name, SchedulerUtils schedulerUtils, JobScheduler delegateJobScheduler) {
		this(name, schedulerUtils);
		this.delegateJobScheduler = delegateJobScheduler;
		LOG.trace("AdvisoryJobScheduler[{}] created with delegate {}", name, delegateJobScheduler);
	}

	public AdvisoryJobScheduler(String name, SchedulerUtils schedulerUtils) {
		this.name = name;
		this.schedulerUtils = schedulerUtils;
		LOG.trace("AdvisoryJobScheduler[{}] created", name);
	}

	@Override
	public String getName() throws Exception {
		return name;
	}

	@Override
	public void startDispatching() throws Exception {
		if(null != delegateJobScheduler) {
			LOG.trace("AdvisoryJobScheduler[{}] delegating startDispatching to {}", name, delegateJobScheduler);
			delegateJobScheduler.startDispatching();
		}

		dispatchEnabled.set(true);
		LOG.trace("AdvisoryJobScheduler[{}] dispatching: ", name, dispatchEnabled.get());
	}

	@Override
	public void stopDispatching() throws Exception {
		dispatchEnabled.set(false);
		LOG.trace("AdvisoryJobScheduler[{}] dispatching: ", name, dispatchEnabled.get());

		if(null != delegateJobScheduler) {
			LOG.trace("AdvisoryJobScheduler[{}] delegating stopDispatching to {}", name, delegateJobScheduler);
			delegateJobScheduler.stopDispatching();
		}
	}

	@Override
	public void addListener(JobListener listener) throws Exception {
		LOG.trace("AdvisoryJobScheduler[{}] add listener: {}", name, listener);

		AdvisoryJobListener advisoryJobListener = new AdvisoryJobListener(schedulerUtils, listener);
		jobListeners.put(listener, advisoryJobListener);

		if(null != delegateJobScheduler) {
			LOG.trace("AdvisoryJobScheduler[{}] delegating addListener[{}] to {}", name, listener, delegateJobScheduler);
			delegateJobScheduler.addListener(advisoryJobListener);
		}
	}

	@Override
	public void removeListener(JobListener listener) throws Exception {
		LOG.trace("AdvisoryJobScheduler[{}] remove listener: {}", name, listener);

		AdvisoryJobListener advisoryJobListener = jobListeners.remove(listener);

		if(null != delegateJobScheduler) {
			LOG.trace("AdvisoryJobScheduler[{}] delegating removeListener[{}] to {}", name, listener, delegateJobScheduler);
			delegateJobScheduler.removeListener(advisoryJobListener);
		}
	}

	@Override
	public void schedule(String jobId, ByteSequence payload, long delay) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willScheduleJob(jobId, payload);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.schedule(jobId, payload, delay);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didScheduleJob(jobId, payload);
		}
	}

	@Override
	public void schedule(String jobId, ByteSequence payload, String cronEntry) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willScheduleJob(jobId, payload);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.schedule(jobId, payload, cronEntry);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didScheduleJob(jobId, payload);
		}
	}

	@Override
	public void schedule(String jobId, ByteSequence payload, String cronEntry, long delay, long period, int repeat) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willScheduleJob(jobId, payload);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.schedule(jobId, payload, cronEntry, delay, period, repeat);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didScheduleJob(jobId, payload);
		}
	}

	@Override
	public void remove(long time) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willRemoveRange(time, time);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.remove(time);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didRemoveRange(time, time);
		}
	}

	@Override
	public void remove(String jobId) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willRemoveJob(jobId);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.remove(jobId);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didRemoveJob(jobId);
		}
	}

	@Override
	public void removeAllJobs() throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willRemoveRange(0, Long.MAX_VALUE);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.removeAllJobs();
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didRemoveRange(0, Long.MAX_VALUE);
		}
	}

	@Override
	public void removeAllJobs(long start, long finish) throws Exception {
		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.willRemoveRange(start, finish);
		}

		if(null != delegateJobScheduler) {
			delegateJobScheduler.removeAllJobs(start, finish);
		}

		for(AdvisoryJobListener advisoryJobListener : jobListeners.values()) {
			advisoryJobListener.didRemoveRange(start, finish);
		}
	}

	@Override
	public long getNextScheduleTime() throws Exception {
		if(null == delegateJobScheduler) {
			return -1L;
		}
		return delegateJobScheduler.getNextScheduleTime();
	}

	@Override
	public List<Job> getNextScheduleJobs() throws Exception {
		if(null == delegateJobScheduler) {
			return new ArrayList<Job>();
		}
		return delegateJobScheduler.getNextScheduleJobs();
	}

	@Override
	public List<Job> getAllJobs() throws Exception {
		if(null == delegateJobScheduler) {
			return new ArrayList<Job>();
		}
		return delegateJobScheduler.getAllJobs();
	}

	@Override
	public List<Job> getAllJobs(long start, long finish) throws Exception {
		if(null == delegateJobScheduler) {
			return new ArrayList<Job>();
		}
		return delegateJobScheduler.getAllJobs(start, finish);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "JobScheduler: " + name;
	}
}
