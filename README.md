# Boomi Application Performance Management Connector

I wanted to share a solution I recently developed to simplify Observability with the Dell Boomi AtomSphere Platform.
Boomi APM Connector allows:
- Distributed Tracing
- Trigger of Events
- Creation of Custom Metrics

The Boomi APM Connector will enable Boomi Runtime for Observability:

![Alt text](resources/observability-pyramid.png?raw=true "BoomiAPM")

Taking the following Components topology:

<!---![Alt text](resources/use-case.png?raw=true "BoomiAPM"))-->
<img src="./resources/use-case.png?raw=true" alt="Alt text" width="500px"></img>

## List of Actions

| Connection Actions | Operation         | Traces   | Events | Metrics |
| ------------------ |-------------------|----------|--------| ------- |
| Execute            | APM Start         | Start    | N/A    | N/A     |
| Execute            | APM Save/Continue | Continue | N/A    | N/A     |
| Execute            | APM Stop          | End      | Yes    | N/A     |
| Execute            | APM Error         | Start    | Yes    | N/A     |
| Update             | Publish Metrics   | N/A      | N/A    | Yes     |

## Observability Platforms supported

| Platform Name        | Trace | Events | Metrics |
|----------------------|-------|--------|---------|
| AppDynamics          | ✅     | ✅      | ✅      |
| AWS X-Ray Otel       | ✅     | No     | No      |
| Datadog              | ✅     | ✅      | ✅      |
| Dynatrace            | ✅     | No     | ✅      |
| ElasticAPM           | ✅     | No     | No      |
| HoneyComb            | ✅     | No     | No      |
| NewRelic             | ✅     | No     | No      |
| Manage Engine        | ✅ -   | No     | No      |
| OpenTelemetry        | ✅     | No     | ✅       |
| OpenTracing          | ✅     | No     | No      |
| OpenTracing Resolver | ✅     | No     | No      |
| VMWare Wavefront     | ✅     | No     | No      |

*-: Tracing on a Process (not distributed)*

**No: Not implemented yet or Not available*

## Getting Started

Please download the library [connector-archive](target/boomiapm-1.90--car.zip?raw=true) and the connector descriptor [connector-descriptor](target/classes/connector-descriptor.xml?raw=true).

### Prerequisites

#### Setup of the Observability Platform Agent and Java Agent

Depending on the Observability Stack used, installation of one of multiple Agents will be required.

#### Setup of the Boomi Connector

Please go to Setup>Account>Publisher and fill out the information.

And then, go to Setup>Development Resources>Developer and create a new Group. The two files to upload are the files you previous downloaded. For the Vendor Product Version, please mentioned the version of the Zip Archive.

#### Use of the Boomi Connector

Will will instrument the following Boomi Process:

![Alt text](resources/boomi-process.png?raw=true "BoomiAPM")

First, we have to configure the APM Connector:

![Alt text](resources/connector.png?raw=true "BoomiAPM")

Then, we will create three operations:

The first one with a "Start Trace" Action

![Alt text](resources/op-start-trace.png?raw=true "BoomiAPM")

The second one with a "Stop Trace' Action and the "Send Event" enabled

![Alt text](resources/op-stop-trace.png?raw=true "BoomiAPM")

A third one with a "Error' Action and the "Send Event" enabled

![Alt text](resources/op-error-trace.png?raw=true "BoomiAPM")

Then, we are the three shapes to the process:

- The APM Start shape at the beginning
- (Optional) The APM Save/Continue shape in a sub-process (will add the current process name) or in the main process to add tags
- The APM Stop shape before the last End, please note that we created a branch here as the Disk shape (Get) might not returned a Document thus an APM Stop shape after the Disk might not be called
- The APM Error in the try catch

*Please note that the operations created previously don't need to be created for another Process to instrument*

![Alt text](resources/boomi-process-instrumented.png?raw=true "BoomiAPM")

That's it! You can deploy the process and see Traces and Events.

### Additional configurations

#### Custom tags / metadata

You can add custom tags to the trace using the "Set Property" shape with APM Document:

![Alt text](resources/boomi-process-setprops.png?raw=true "BoomiAPM")

For instance you can add atomId and atomName:

![Alt text](resources/boomi-process-setprops-dialog.png?raw=true "BoomiAPM")

#### Getting exception

In your original Process, if you were using the "Document Property - Base - Try/Catch Message" value in a Shape (Throw, Notify, etc), you can use the "Document Property - APM - Try/Catch Message" to get the same error message.

So in your example,

![Alt text](resources/rethrow-before.png?raw=true "BoomiAPM")

will become:

![Alt text](resources/rethrow-after.png?raw=true "BoomiAPM")

# Overview of Deployments with Observability Stacks

## Overview of Deployment with AppDynamics
<!--![Alt text](resources/appdynamics.png?raw=true "BoomiAPM")-->
<img src="./resources/appdynamics.png?raw=true" alt="Alt text" width="500px"></img>

More details of the configuration of AppDynamics here [here](https://blog.antsoftware.org/boomi-observability-appdynamics/)

### Metrics Types
The following Metrics are supported for AppDynamics:
- average 
- sum
- observed

## Overview of Deployment with Datadog 
<!--![Alt text](resources/datadog.png?raw=true "BoomiAPM")-->
<img src="./resources/datadog.png?raw=true" alt="Alt text" width="500px"></img>

### Metrics Types
The following Metrics type are supported for Datadog:
- count
- rate
- gauge
- distribution

More details of the configuration of Datadog here [here](https://blog.antsoftware.org/boomi-observability-intro-datadog/)

## Overview of Deployment with ElasticAPM
<!--![Alt text](resources/elasticapm.png?raw=true "BoomiAPM")-->
<img src="./resources/elasticapm.png?raw=true" alt="Alt text" width="500px"></img>

More details of the configuration of Elastic APM here [here](https://blog.antsoftware.org/boomi-observability-elastic/)

## Overview of Deployment with New Relic One
<!--![Alt text](resources/newrelic.png?raw=true "BoomiAPM")-->
<img src="./resources/newrelic.png?raw=true" alt="Alt text" width="500px"></img>

More details of the configuration of New Relic One here [here](https://blog.antsoftware.org/boomi-observability-newrelic/)

## Overview of Deployment with Grafana Cloud
<!--![Alt text](resources/grafanacloud.png?raw=true "BoomiAPM")-->
<img src="./resources/grafanacloud.png?raw=true" alt="Alt text" width="500px"></img>

More details of the configuration of Grafana Cloud here [here](https://blog.antsoftware.org/boomi-observability-grafanacloud/)