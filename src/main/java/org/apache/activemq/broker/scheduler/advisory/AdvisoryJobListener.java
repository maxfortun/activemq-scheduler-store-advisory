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

import org.apache.activemq.broker.BrokerService;

import org.apache.activemq.broker.scheduler.JobListener;

import org.apache.activemq.util.ByteSequence;

import org.apache.activemq.command.ActiveMQDestination;

import org.apache.activemq.broker.scheduler.SchedulerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an advisory JobListener interface.
 */
public class AdvisoryJobListener implements JobListener {
	private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobListener.class);

	/**
	 *  Used to specify which scheduler activity was performed on a Scheduled Message
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_DESTINATION = "ActiveMQ.Scheduler.Activity";

	/**
	 *  Used to specify undesired activity properties
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_FWD_EXCLUDE = "AMQ_SCHEDULER_ACTIVITY_FWD_EXCLUDE";

	/**
	 *  Used to specify desired activity properties
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_FWD_INCLUDE = "AMQ_SCHEDULER_ACTIVITY_FWD_INCLUDE";

	/**
	 *  Used to specify which scheduler activity was performed on a Scheduled Message
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY = "AMQ_SCHEDULER_ACTIVITY";

	/**
	 *  Message is scheduled
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_SCHEDULED = "SCHEDULED";

	/**
	 *  Message is dispatched
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_DISPATCHED = "DISPATCHED";

	/**
	 *  Message is removed
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_REMOVED = "REMOVED";

	/**
	 *  Message is removed range
	 */
	public static final String AMQ_SCHEDULER_ACTIVITY_REMOVED_RANGE = "REMOVED_RANGE";

	private final SchedulerUtils schedulerUtils;
	private final JobListener delegateJobListener; 

	private ActiveMQDestination destination;

	public AdvisoryJobListener(SchedulerUtils schedulerUtils, JobListener delegateJobListener) {
		this.schedulerUtils = schedulerUtils;
		this.delegateJobListener = delegateJobListener;
		this.destination = ActiveMQDestination.createDestination(AMQ_SCHEDULER_ACTIVITY_DESTINATION, ActiveMQDestination.TOPIC_TYPE);
        LOG.info("Destination: {}", this.destination);
	}

	public void willScheduleJob(String id, ByteSequence job) throws Exception  {
	}

	public void didScheduleJob(String id, ByteSequence job) throws Exception {
	}

	public void willDispatchJob(String id, ByteSequence job) throws Exception {
	}

	// Actual dispatching of the job
	@Override
	public void scheduledJob(String id, ByteSequence job) {
		if(null == delegateJobListener) {
			return;
		}
		delegateJobListener.scheduledJob(id, job);
	}

	public void didDispatchJob(String id, ByteSequence job) throws Exception {
	}

	public void willRemoveJob(String id) throws Exception {
	}

	public void didRemoveJob(String id) throws Exception {
	}

	public void willRemoveRange(long start, long end) throws Exception {
	}

	public void didRemoveRange(long start, long end) throws Exception {
	}
}

