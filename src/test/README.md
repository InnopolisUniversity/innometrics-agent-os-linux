# Innometrics linux datacollector tests

Innometrics data collector testing implementation for Linux OS

#### -- Project Status: [Active]

## Types of test covered

### Functional tests
1. Unit testing
1. Integration
1. User Interface Testing
1. Ad-hoc testing
1. Black box testing

### Non-Functional tests
1. Security testing
1. Failover testing
1. Compatibility testing
1. Recovery testing

## Tests status
| Test type     | Total cases| Passed        | Failed      | Score   |
|-----------    |-------     |----------     |---------    |-------- |
| Black box     |    -       |     -         |   --        |   --    |
| white box     |    -       |     -         |   --        |   --    |
|  Unit         |    -       |     -         |   --        |   --    |
| Ad-hoc        |    -       |     -         |   --        |   --    |
| Integration   |    -       |     -         |   --        |   --    |
| Recovery      |    -       |     -         |   --        |   --    |
| Interface     |    -       |     -         |   --        |   --    |
| Exploratory   |    -       |     -         |   --        |   --    |

Overall Score: 1 out of 2  (50.00%)

## How to run test cases

To run test cases & generate a test report for specific tests types, execute the test script associated with the desired test. At the end of the test execution a test report will be generated depending to test type. For mode infomation about test cases and specifications see the tests outline [document](https://innometrics.ru). The Structure and files related to types of test is show bellow :

    .
    ├── modelTests.java             # ....
    ├── testingUtils                # ....
    ├── UIComponentsPresenceTests   # ....  
    ├── module-info.java          
    ├── ...      
    ├── ...   
    └── README.md

## Testing tools used

1. [TestFX](https://github.com/TestFX/TestFX)
