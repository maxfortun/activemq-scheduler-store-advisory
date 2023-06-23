### Premise

[ActiveMQ Scheduler](https://activemq.apache.org/delay-and-schedule-message-delivery) is a powerful tool for delaying and scheduling asynchronous messages. However, the broker does not currently have the appropriate built in facilities to monitor and manipulate the internal state of the scheduler. I wouldn't want to bloat the broker with ux code anyway, and would prefer to do all of the fancy user stuff outside of the broker process. As long as the broker can forward the scheduled messages in real time to an advisory topic - we can keep a synched state in an external tool. To achieve that we need to create a shim between the broker and the scheduler store that will issue advisory messages. This project is such a shim and forwards all scheduler advisory messages to `ActiveMQ.Scheduler.Advisory`.

### Advisories
Advisories have `AMQ_SCHEDULER_ADVISORY` header set with the type of advisory.
| Type | Description |
|---|---|
| SCHEDULE | Scheduler is about to schedule a message |
| SCHEDULED | Scheduler has scheduled a message |
| DISPATCH | Scheduler is about to dispatch a message |
| DISPATCHED | Scheduler has dispatched a message |
| REMOVE | Scheduler is about to remove a previously scheduled message |
| REMOVED | Scheduler has removed a previously scheduled message |
| REMOVE_RANGE | Scheduler is about to remove a range previously scheduled messages |
| REMOVED_RANGE | Scheduler has removed a range of previously scheduled messages |

### Building
```bash
mvn clean package install
```

### Installing artifact into ~/.m2/repository
```bash
cp target/activemq-scheduler-store-advisory-*.jar /opt/activemq/lib/optional/
```

### Deploying artifact to a local maven repo
Make sure your [settings.xml](https://maven.apache.org/settings.html) is properly configured.
```bash
repositoryId=$(xmllint --xpath '/*[local-name() = "settings"]/*[local = "activeProfile"]/text()' ~/.m2/settings.xml)
repositoryUrl=$(xmllint --xpath '/*[local-name() = "settings"]/*[local-name() = "profiles"]/*[local-name() = "profile" and *[local-name() = "id"]/text() = "$artifactoryId"]' ~/.m2/settings.xml)
version=$(xmllint --xpath '/*[local-name() = "project"]/*[local-name() = "version"]/text()')
mvn deploy:deploy-file -DrepositoryId=$repositoryId -Durl=$repositoryUrl -Dpackaging=jar -DgroupId=org.apache.activemq -DartifactId=activemq-scheduler-store-advisory -Dversion=$version -Dfile=target/activemq-scheduler-store-advisory-$version.jar
```

### Configuring broker
While this is not a well documented feature, broker does allow to specify a custom jobSchedulerStore. By default broker comes with a KahaDb store and a memory store. This example creates a shim between the broker and the KahaDb store.

```xml
<broker xmlns="http://activemq.apache.org/schema/core"
            id="broker"
            brokerName="${BROKER_NAME}"
            persistent="true"
            deleteAllMessagesOnStartup="false"
            dataDirectory="${activemq.data}"
            schedulePeriodForDestinationPurge="10000"
            schedulerSupport="true">
	
	<!-- ... -->

	<jobSchedulerStore>
  		<bean xmlns="http://www.springframework.org/schema/beans" 
			id="jobSchedulerStore" 
		  	class="org.apache.activemq.broker.scheduler.advisory.AdvisoryJobSchedulerStore">
	  
    			<constructor-arg ref="broker" />
	  
    			<constructor-arg>
      				<bean xmlns="http://www.springframework.org/schema/beans" 
			      		id="jobSchedulerStoreDelegate" 
		      			class="org.apache.activemq.store.kahadb.scheduler.JobSchedulerStoreImpl">
					<property name="directory" value="${activemq.data}/scheduler" />
      				</bean>
    			</constructor-arg>
	  
  		</bean>
	</jobSchedulerStore>

	<!-- ... -->
	
</broker>
```

