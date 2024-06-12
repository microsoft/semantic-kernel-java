# Semantic Kernel for Java

Semantic Kernel (SK) is a lightweight foundation that lets you easily mix conventional programming languages with the latest in
Large Language Model (LLM) AI "prompts" with templating, chaining, and planning capabilities out-of-the-box.

To learn more about Microsoft Semantic Kernel, visit
the [Microsoft Semantic Kernel documentation](https://learn.microsoft.com/en-us/semantic-kernel/whatissk).

The Microsoft Semantic Kernel for Java is a library that implements the key concepts and foundations of Microsoft Semantic Kernel. It is designed
to be used in Java applications in both client (desktop, mobile, CLIs) and server environments in an idiomatic way, and to be easily integrated with other Java libraries
and frameworks.

## Get started

To run the LLM prompts and semantic functions in this kernel, make sure you have
an [Open AI API Key](https://openai.com/product/)
or [Azure Open AI service key](https://learn.microsoft.com/azure/cognitive-services/openai/).

### Requirements

To build the Semantic Kernel for Java, you will need:

- **Required**:
  - [OpenJDK 17](https://microsoft.com/openjdk/) or newer

### Build the Semantic Kernel

1. Clone this repository

        git clone -b java-development https://github.com/microsoft/semantic-kernel/

2. Build the project with the Maven Wrapper

        cd semantic-kernel/java
        ./mvnw install

## Using the Semantic Kernel for Java

The library is organized in a set of dependencies published to Maven Central. For a list of the Maven dependencies and how to use each of them, see [PACKAGES.md](PACKAGES.md).

Alternatively, check the `samples` folder for examples of common AI-enabled scenarios implemented with Semantic Kernel for Java.

## Discord community

Join the [Microsoft Semantic Kernel Discord community](https://aka.ms/java-sk-discord) to discuss the Semantic Kernel
and get help from the community. We have a `#java` channel for Java-specific questions.

## Contributing

### Testing locally

The project may contain end-to-end tests that require an OpenAI key to run. To run these tests locally, you
will need to set the following environment variable:

- `OPENAI_API_KEY` - the OpenAI API key.

If you are using Azure OpenAI, you will also need to set the following environment variables:

- `AZURE_OPENAI_ENDPOINT` - the Azure OpenAI endpoint found in **Keys * Endpoint** section of the Azure OpenAI service.
- `AZURE_OPENAI_API_KEY` - the Azure OpenAI API key found in **Keys * Endpoint** section of the Azure OpenAI service.
- `AZURE_OPENAI_DEPLOYMENT_NAME` - the custom name you chose for your deployment when you deployed a model. It can be
  found under **Resource Management > Deployments** in the Azure Portal.

For more information, see the Azure OpenAI documentation
on [how to get your Azure OpenAI credentials](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/quickstart?pivots=rest-api&tabs=command-line#retrieve-key-and-endpoint).

To run the unit tests only, run the following command:

    ./mvnw package

To run all tests, including integration tests that require an OpenAI key, run the following command:

    ./mvnw verify

### Submitting a pull request

Before submitting a pull request, please make sure that you have run the project with the command:

```shell
./mvnw clean package -Pbug-check
```

The bug-check profile will detect some static analysis issues that will prevent merging as well as apply formatting
requirements to the code base.

Also ensure that:

- All new code is covered by unit tests
- All new code is covered by integration tests

Once your proposal is ready, submit a pull request to the `java-development` branch. The pull request will be reviewed by the
project maintainers.

Make sure your pull request has an objective title and a clear description explaining the problem and solution.

## License

This project is licensed under the [MIT License](../LICENSE).

## Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](../CODE_OF_CONDUCT.md).

