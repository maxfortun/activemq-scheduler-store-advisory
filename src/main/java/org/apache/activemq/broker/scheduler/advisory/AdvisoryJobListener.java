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

import java.util.Map;

import org.apache.activemq.advisory.AdvisorySupport;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;

import org.apache.activemq.broker.scheduler.JobListener;

import org.apache.activemq.util.ByteSequence;
import org.apache.activemq.util.IdGenerator;
import org.apache.activemq.util.LongSequenceGenerator;

import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessageId;
import org.apache.activemq.command.ProducerId;
import org.apache.activemq.command.ProducerInfo;

import org.apache.activemq.state.ProducerState;

import org.apache.activemq.broker.scheduler.SchedulerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an advisory JobListener interface.
 */
public class AdvisoryJobListener implements JobListener {
	private static final Logger LOG = LoggerFactory.getLogger(AdvisoryJobListener.class);

	/**
	 *  Used to specify undesired advisory properties
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_FWD_EXCLUDE = "AMQ_SCHEDULER_ADVISORY_FWD_EXCLUDE";

	/**
	 *  Used to specify desired advisory properties
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_FWD_INCLUDE = "AMQ_SCHEDULER_ADVISORY_FWD_INCLUDE";

	/**
	 *  Used to specify which scheduler advisory was performed on a Scheduled Message
	 */
	public static final String AMQ_SCHEDULER_ADVISORY = "AMQ_SCHEDULER_ADVISORY";

	/**
	 *  Message is scheduled
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_SCHEDULE = "SCHEDULE";
	public static final String AMQ_SCHEDULER_ADVISORY_SCHEDULED = "SCHEDULED";

	/**
	 *  Message is dispatched
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_DISPATCH = "DISPATCH";
	public static final String AMQ_SCHEDULER_ADVISORY_DISPATCHED = "DISPATCHED";

	/**
	 *  Message is removed
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_REMOVE = "REMOVE";
	public static final String AMQ_SCHEDULER_ADVISORY_REMOVED = "REMOVED";

	/**
	 *  Message is removed range
	 */
	public static final String AMQ_SCHEDULER_ADVISORY_REMOVE_RANGE = "REMOVE_RANGE";
	public static final String AMQ_SCHEDULER_ADVISORY_REMOVED_RANGE = "REMOVED_RANGE";

    private static final IdGenerator ID_GENERATOR = new IdGenerator();

	private final LongSequenceGenerator messageIdGenerator = new LongSequenceGenerator();
    private final ProducerId producerId = new ProducerId();

	private final SchedulerUtils schedulerUtils;
	private final JobListener delegateJobListener; 

	private ActiveMQDestination destination;

	public AdvisoryJobListener(String destinationName, SchedulerUtils schedulerUtils, JobListener delegateJobListener) {
		this.schedulerUtils = schedulerUtils;
		this.delegateJobListener = delegateJobListener;
		this.destination = ActiveMQDestination.createDestination(destinationName, ActiveMQDestination.TOPIC_TYPE);
		LOG.info("Destination: {}", this.destination);

		producerId.setConnectionId(ID_GENERATOR.generateId());
	}

	public void willScheduleJob(String id, ByteSequence job) throws Exception  {
		LOG.debug("Schedule job {}", id);
        Message message = schedulerUtils.toMessage(id, job);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_SCHEDULE);
        forwardMessage(message);
	}

	public void didScheduleJob(String id, ByteSequence job) throws Exception {
		LOG.debug("Scheduled job {}", id);
        Message message = schedulerUtils.toMessage(id, job);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_SCHEDULED);
        forwardMessage(message);
	}

	public void willDispatchJob(String id, ByteSequence job) throws Exception {
		LOG.debug("Dispatch job {}", id);
        Message message = schedulerUtils.toMessage(id, job);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_DISPATCH);
        forwardMessage(message);
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
		LOG.debug("Dispatched job {}", id);
        Message message = schedulerUtils.toMessage(id, job);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_DISPATCHED);
        forwardMessage(message);
	}

	public void willRemoveJob(String id, ByteSequence job) throws Exception {
		LOG.debug("Remove job {}", id);
        Message message = schedulerUtils.createMessage(id);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_REMOVE);
        forwardMessage(message);
	}

	public void didRemoveJob(String id, ByteSequence job) throws Exception {
		LOG.debug("Removed job {}", id);
        Message message = schedulerUtils.createMessage(id);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_REMOVED);
        forwardMessage(message);
	}

	public void willRemoveRange(long start, long end, Map<String,ByteSequence> jobs) throws Exception {
		LOG.debug("Remove range {} - {}", start, end);
        Message message = schedulerUtils.createMessage(start, end);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_REMOVE_RANGE);
        forwardMessage(message);
	}

	public void didRemoveRange(long start, long end, Map<String,ByteSequence> jobs) throws Exception {
		LOG.debug("Removed range {} - {}", start, end);
        Message message = schedulerUtils.createMessage(start, end);
        message.setProperty(AMQ_SCHEDULER_ADVISORY, AMQ_SCHEDULER_ADVISORY_REMOVED_RANGE);
        forwardMessage(message);
	}

	protected void forwardMessage(Message message) throws Exception {
		message.setOriginalTransactionId(null);
		message.setPersistent(false);
		message.setType(AdvisorySupport.ADIVSORY_MESSAGE_TYPE);
		message.setMessageId(new MessageId(producerId, messageIdGenerator.getNextSequenceId()));

		// Preserve original destination
		message.setOriginalDestination(message.getDestination());

		message.setDestination(destination);
		message.setResponseRequired(false);
		message.setProducerId(producerId);

		ConnectionContext context = schedulerUtils.getBrokerService().getAdminConnectionContext();
		final boolean originalFlowControl = context.isProducerFlowControl();
		final ProducerBrokerExchange producerExchange = new ProducerBrokerExchange();
		producerExchange.setConnectionContext(context);
		producerExchange.setMutable(true);
		producerExchange.setProducerState(new ProducerState(new ProducerInfo()));
		try {
			context.setProducerFlowControl(false);
			LOG.trace("{}", message);
			schedulerUtils.getBrokerService().getBroker().send(producerExchange, message);
		} finally {
			context.setProducerFlowControl(originalFlowControl);
		}
	}

}

