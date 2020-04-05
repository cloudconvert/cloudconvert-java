We need a Java API SDK for an existing REST API.

The API docs are available at: https://cloudconvert.com/api/v2

There are already PHP, node.js and Python SDKs available:
https://github.com/cloudconvert/cloudconvert-php
https://github.com/cloudconvert/cloudconvert-node
https://github.com/cloudconvert/cloudconvert-python

So the task is basically just "translating" one of these SDKs into Java.

The SDK should have the following features:
- [DONE] Create jobs, get jobs, delete jobs, wait for job completion
- [DONE] Create tasks, get tasks, delete tasks, wait for task completion
- [DONE] Method to upload input files
- [DONE] Method to download output files
- [DONE] Method to validate webhook signatures
- [DONE] Unit tests
- [DONE] Integration tests
- [DONE] Working Travis configuration for both unit and integration tests
- [DONE] README file (similar to the existing SDKs)
- [DONE] Prepare Maven package (pom.xml)


1. [DONE] The SDK should work with Java 8+
2. [OK] There is no strict deadline, but I would be thankful if you could get it done in ~2 weeks.
3. [OK] Yes, there is a sandbox API available with unlimited requests.
4. [OK] Socket.io is not required for the Java SDK
5. [DONE] Both sync and async support
