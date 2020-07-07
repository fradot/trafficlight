# TrafficLight
A simple Traffic light simulator app based on Spring Boot.

#### Features:

- Accepts multiple configurations that can be scheduled through the database.
- A different duration interval can be scheduled for each light.
- New configurations can be scheduled without restarting the application.
- Each configuration can be defined with a priority to determine which configuration to 
execute in case of overlapping configurations.
- H2 file persisted database available.

---

#### Quick Overview:

**StateMachine:** The states transitions are based on the spring state machine module. The state machine automatically start on application
startup with an initial state RED and wait for transition events to be triggered. A listener is registered which logs
the current states after each transition.

**Scheduler:** A custom implementation of the __SchedulingConfigurer__ is registering a custom __Trigger__ which defines the 
next execution time calculated based on the current configuration.

**How it works:** At startup the application loads the active configurations from the database and register two 
cron tasks (__ScheduledFuture__) for each configuration, an enabling task and a disabling task. The tasks
are scheduled based on the cron expressions defined. Each task has a reference to the corresponding configuration
and as soon as it gets triggered it adds (if enabling task) or removes (if disabling) the configuration
to/from a __PriorityBlockingQueue<TrafficLightConfiguration>__ which maintains all the active configurations.
The active configuration is retrieved by the the custom __Trigger__ which uses __peek()__ on the priority
queue to get the configuration with highest priority. Based on this configuration, the next execution time
is calculated.

**An example:** To have a traffic light configuration which runs every Monday, Tuesday and Friday, from 8:00am
to 8:00pm it will be enough to define a configuration like this:
- start cron: 0 0 8 ? * MON,TUE,FRI
- end cron: 0 0 20 ? * MON,TUE,FRI

If an end cron is not defined the configuration will run continuously.
If a start configuration is not defined then the configuration will never be activated.
Having the possibility to define a configuration priority, we can have multiple overlapping configurations 
achieving a high degree of customization.

**Enabling/Disabling a task at runtime:** During startup a task is registered to periodically synch with the database
and check for all the configuration to be enabled (__toBeEnabled__) and to be disabled(__toBeDisabled__) and
which are not currently active. All the configurations to be enabled found are added to the queue and set to active.
All the configurations to be disabled are removed from the queue and set to not active.

**Improvements:** 
- A proxy to handle the configuration queue and hide its implementation.
- A maximum number of configurations that can be defined based on available resources.
- Optimisation of configuration retrieval and update from/to database (batch).
- A controller listener to push the state of the traffic light to all the listening clients, using 
websocket.
- Increase code coverage, now at about 75%.

---

#### Setup:

##### Database
Option 1 - use H2 in memory database (may conflict with IT database):
    - Set spring.jpa.hibernate.ddl-auto=create-drop
    - Set spring.datasource.url=jdbc:h2:mem:testdb in application.properties
Option 2 - use H2 file database:
    - Set spring.jpa.hibernate.ddl-auto=update
    - Set spring.datasource.url=jdbc:h2:file:./data/h2_file_db;DB_CLOSE_ON_EXIT=FALSE in application.properties
    - Download and execute the H2 database engine available [here](https://www.h2database.com/html/main.html)
    - Use the browser interface to access the following database
        - Driver: org.h2.Driver
        - jdbc:h2:./data/h2_file_db (make sure to point to the correct file location)
        - user: sa
        - password: password
    - There are 2 configurations already stored in the database (table TL_CONFIGURATIONS), a default configuration
    which is mandatory to have, and a basic configuration which execute every 2 seconds.


##### Run tests
- Use IntelliJ to run unit tests and integration tests


##### Run the application
-  ./mvnw spring-boot:run


##### Create a new Configuration
- To create a new configuration (both before startup or when the app is already running), populate a 
database entry, for example:
    - active - must be set to false if the application is already running, set to true otherwise.
    - default_configuration - false
    - end_cron_expression - when the configuration should be disabled (periodically), 
    can be null, in this case the configuration is never disable.
    - start_cron_expression - when the configuration should be enabled (periodically), 
    can be null, in this case the configuration is never activate.
    - to_be_enabled - must be set to true if the application is already running, set to false otherwise.
    - to_be_disabled - false
    - the wanted duration in seconds for each phase (green_duration, red_duration, orange_duration)
    - the priority, in case we have overlapping configuration, the one with the highest priority
    will be executed.
    
    For example we can have a configuration which is enabled every day at 13:55:13 (cron expression: 23 55 13 ? * * )
    and can be disabled every day at 19:54:00 (cron expression: 0 54 19 ? * * ). Considering that we can have overlapping
    configurations, this configuration can be very flexible.
    
    **Note:** database synchronization happens every 3 minutes and can be configured in the
    application properties: trafficlight.database.synch.cron
    
    
     


