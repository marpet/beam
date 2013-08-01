This module contains a set or BEAM ProductReader acceptance tests.

To be able to run the tests, two VM properties need to be present:

-Drun.reader.tests=true

This property enables the acceptance test runner. If not set, all tests are skipped and a message is printed to the
console window.

-Dreader.tests.data.dir=<Path_To_Data>

This property defines the root directory for the test dataset. All test-product definitions are referenced relative to
this root directory. If the property is not set or does not denote a valid directory, the test setup fails.