# phone-number-frontend

## Overview

This frontend service enables users to validate and verify their
phone numbers. It generates a verification code, sends it via SMS,
and prompts the user to enter the code to complete verification.

### Unit testing
To run the unit tests for the application, use the following command:

```bash
sbt test
```


### Integration testing
To run the integration tests, use the following command:

```bash
sbt it/test
```

### Code coverage

```bash
sbt clean coverage test it/test coverageReport
```

### Running locally
To run the service locally, you can use the following command:

```bash
./run-local.sh
```

Once running, access the frontend at http://localhost:6080/phone-number-example-frontend.

### SBT Updates Plugin
This project uses the sbt-updates plugin to help manage dependency updates.
For more information on how to use the plugin, please refer to the documentation:

https://github.com/hmrc/platui/blob/main/docs/sbt-updates_plugin-usage.md#sbt-updates-plugin

To check all dependencies (libraries and plugins), the easiest way is to use the command below:

```bash
sbt ";dependencyUpdates; reload plugins; dependencyUpdates"
```

Keep in mind that the output will be split into two parts where the first one will have libraries and second plugins.


### Service manager profile
To run the service using the service manager, use the following command:

```bash
sm2 --start PHONE_NUMBER_ALL
```


### License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
