/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.broker.scheduler.Job;
import org.apache.activemq.broker.scheduler.JobListener;
import org.apache.activemq.broker.scheduler.JobScheduler;

import org.apache.activemq.util.ByteSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an advisory JobScheduler instance.
 */
public class AdvisoryJobScheduler implements JobScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobScheduler.class);

    private final String name;

	private JobScheduler delegateJobScheduler = null;

	private final Map<JobListener, AdvisoryJobListener> jobListeners = new ConcurrentHashMap<>();

    public AdvisoryJobScheduler(String name, JobScheduler delegateJobScheduler) {
		this(name);
		this.delegateJobScheduler = delegateJobScheduler;
    }

    public AdvisoryJobScheduler(String name) {
        this.name = name;
    }

    @Override
    public String getName() throws Exception {
        return name;
    }

    @Override
    public void startDispatching() throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.startDispatching();
    }

    @Override
    public void stopDispatching() throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.stopDispatching();
    }

    @Override
    public void addListener(JobListener listener) throws Exception {
		AdvisoryJobListener advisoryJobListener = new AdvisoryJobListener(listener);
		jobListeners.put(listener, advisoryJobListener);

		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.addListener(advisoryJobListener);
    }

    @Override
    public void removeListener(JobListener listener) throws Exception {
		AdvisoryJobListener advisoryJobListener = jobListeners.remove(listener);

		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.removeListener(advisoryJobListener);
    }

    @Override
    public void schedule(String jobId, ByteSequence payload, long delay) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.schedule(jobId, payload, delay);
    }

    @Override
    public void schedule(String jobId, ByteSequence payload, String cronEntry) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.schedule(jobId, payload, cronEntry);
    }

    @Override
    public void schedule(String jobId, ByteSequence payload, String cronEntry, long delay, long period, int repeat) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.schedule(jobId, payload, cronEntry, delay, period, repeat);
    }

    @Override
    public void remove(long time) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.remove(time);
    }

    @Override
    public void remove(String jobId) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.remove(jobId);
    }

    @Override
    public void removeAllJobs() throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.removeAllJobs();
    }

    @Override
    public void removeAllJobs(long start, long finish) throws Exception {
		if(null == delegateJobScheduler) {
			return;
		}
		delegateJobScheduler.removeAllJobs(start, finish);
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
