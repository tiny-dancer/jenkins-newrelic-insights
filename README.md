# jenkins-newrelic-insights

This is a Jenkins Plugin which allows users to send custom events to new relic insights.

## Set up

### Credentials

Set up a "New Relic Insights Key" credential

![alt text](./docs/jenkins-creds.png "Logo Title Text 1")

## Usage

Add key value data points to submit with a custom event.

### Freestyle Job

> eventType key is required

![alt text](./docs/jenkins-freestyle.png "Logo Title Text 1")

### Scripted Pipeline 

Two options exist for sending data with a custom event in a scripted pipeline.

#### Key Value Data Points
```groovy
node {
    // assemble key value data points
    def secrets = [
      [$class: 'KeyValue', key: 'eventType', value: 'test_deployment'],
      [$class: 'KeyValue', key: 'appId', value: '888']
    ]
    
    // call build step with key value data points
    step([$class: 'NewRelicInsights', 
    credentialsId: '291ff5f2-a93f-4d5a-8e56-a43d61475fc7', 
    data: secrets])
}
```

#### Json String
```groovy
import groovy.json.*;

node {
    // a simpler option for pipelines
    def json = [eventType: 'test_deployment', appId: '888']
    
    // call build step with json string
    step([$class: 'NewRelicInsights', 
    credentialsId: '291ff5f2-a93f-4d5a-8e56-a43d61475fc7', 
    json: json])   
}
```
> Insights only accepts key-value pairs, not map/object or array values. Only floats and strings are
 supported data types.  See [Insights Docs](https://docs.newrelic.com/docs/insights/explore-data/custom-events/insert-custom-events-insights-api) for more information.

### Querying in Insights

All of the above examples would appear as below in insights:

![awesome pic](./docs/insights-query-results.png "Logo title text")

## Insights Dashboard Examples

Deployment Metrics

![alt text](./docs/nr-insights.png "Logo Title Text 1")


For more information, please see [new relic insights](https://newrelic.com/insights).

## Development

### Maven Tasks

Here is a list of maven tasks that I use on this project:

* **mvn verify**: runs all tests
* **mvn package**: creates the `hpi` plugin archive to be used with Jenkins
* **mvn hpi:run -Djetty.port=8090**: runs the Jenkins server (with the plugin pre-loaded) on port 8090

## License

This project is distributed under the MIT license.

## TODO

- [ ] Seperate out eventType from generic data object
- [ ] Support variables in freestyle
- [X] Support pipeline
- [X] Surface a cleaner way to pass the data via pipeline
- [ ] Implement proper build wrapper pipeline step
 ```groovy
 newrelicInsights credentialsId: '', data: data, json: json
 ```

## Reference credit

https://github.com/jenkinsci/newrelic-deployment-notifier-plugin
 