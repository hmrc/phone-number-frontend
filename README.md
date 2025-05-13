
## phone-number-frontend

### Summary

Frontend server for cip phone number services

The default port for phone-number-frontend is 6080
The default port for phone-number is port 6081
The default port for phone-number-verification is port 6083
The default port for phone-number-stubs is port 6099

### Testing

#### Unit tests

    sbt clean test

## Start the local services

If you don't have mongodb installed locally you can run it in docker using the following command

    docker run -d --rm --name mongodb -p 27017-27019:27017-27019 mongo:4

To start services locally, run the following:

    sm2 --start CIP_PHONE_NUMBER_ALL

#### And then run Integration tests

    sbt clean it:test

### Running app

sm2 --start CIP_PHONE_NUMBER_ALL

Run the services against the current versions in dev, stop the CIP_PHONE_NUMBER_FRONTEND service and start manually

    sm2 --start CIP_PHONE_NUMBER_ALL -r
    sm2 --stop CIP_PHONE_NUMBER_FRONTEND
    cd phone-number-frontend
    sbt run

For reference here are the details for running each of the services individually

    cd phone-number-frontend
    sbt run
 
    cd phone-number
    sbt run

    cd phone-number-verification
    sbt run

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
