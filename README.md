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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
