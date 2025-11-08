# phone-number-frontend

## Overview

This frontend service enables users to validate and verify their
phone numbers. It generates a verification code, sends it via SMS,
and prompts the user to enter the code to complete verification.

### Unit testing
To run the unit tests for the application, use the following command:

```sbt test ```


### Integration testing
To run the integration tests, use the following command:

```sbt it/test```

### Code coverage

```sbt clean coverage test it/test coverageReport```

### Running locally
To run the service locally, you can use the following command:

```./run_local.sh```

### SBT Updates Plugin
This project uses the sbt-updates plugin to help manage dependency updates.
For more information on how to use the plugin, please refer to the documentation:

https://github.com/hmrc/platui/blob/main/docs/sbt-updates_plugin-usage.md#sbt-updates-plugin

To check all dependencies (libraries and plugins) the easiest way is to use below command:

```sbt ";dependencyUpdates; reload plugins; dependencyUpdates"```

Keep in mind that the output will be split into two parts where the first one will have libraries and second plugins.
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
