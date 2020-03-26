We need a Java API SDK for an existing REST API.

The API docs are available at: https://cloudconvert.com/api/v2

There are already PHP, node.js and Python SDKs available:
https://github.com/cloudconvert/cloudconvert-php
https://github.com/cloudconvert/cloudconvert-node
https://github.com/cloudconvert/cloudconvert-python

So the task is basically just "translating" one of these SDKs into Java.

The SDK should have the following features:
- Create jobs, get jobs, delete jobs, wait for job completion
- Create tasks, get tasks, delete tasks, wait for task completion
- Method to upload input files
- Method to download output files
- Method to validate webhook signatures
- Unit tests
- Integration tests
- Working Travis configuration for both unit and integration tests
- README file (similar to the existing SDKs)
- Prepare Maven package (pom.xml)


1. The SDK should work with Java 8+
2. There is no strict deadline, but I would be thankful if you could get it done in ~2 weeks.
3. Yes, there is a sandbox API available with unlimited requests.
4. Socket.io is not required for the Java SDK
5. Both sync and async support
