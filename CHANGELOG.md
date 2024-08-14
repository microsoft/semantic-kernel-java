# 1.2.2

- Fix bug in `FunctionInvocation` not using per-invocation type conversion when calling `withResultType`.
- Fix bug in Global Hooks not being invoked under certain circumstances.
- Add fluent returns to `ChatHistory` `addXMessage` methods.
- Add user agent opt-out for OpenAI requests by setting the property `semantic-kernel.useragent-disable` to `true`.
- Add several convenience `invokePromptAsync` methods to `Kernel`.
- Allow Handlebars templates to call Javabean getters to extract data from invocation arguments.
- Improve thread safety of `ChatHistory`.

#### Experimental Changes

- Add JDBC vector store

#### Non-API Changes

- Add custom type Conversion example, `CustomTypes_Example`
- Dependency updates and pom cleanup
- Documentation updates

# 1.2.0

- Add ability to use image_url as content for a OpenAi chat completion
    - As part of this `ChatMessageTextContent` and `ChatMessageImageContent` was added that extends the
      existing `ChatMessageContent` class. `ChatMessageContent` for now defaults to a text content type for backwards
      compatibility. However, users are encouraged to migrate to using the builders on `ChatMessageTextContent` to
      create text based chat messages.
    - Constructors of `ChatMessageContent` were also modified to support this change.
- Added preliminary hugging face implementation that is still in development/beta.
- Added Gemini support
- Added OpenTelemetry spans for OpenAI requests
- Update the user agent for OpenAI requests
- Move XML parsing classes to implementation package as they are not expected to be used by users.

#### Non-API Changes

- Reorganized the repository when moving to the new Github location
- Removed non-Java files
- Update readmes
- Update build scripts
- Bring back Spring example project

# 1.1.5

- Fix bug with removing new lines on function parameters on Windows
- Fix bug forming serializing arguments to tool calls

# 1.1.3

- Fix bug appending plugin name to tool calls
- Improve exception handling in OpenAIChatCompletion

# 1.1.2

- Upgrade azure-identity to 1.12.1
- Remove fixed netty version in bom

# 1.1.1

- Upgrade azure-ai-openai to 1.0.0-beta.8

# 1.1.0

### Breaking Changes

- `ChatHistory` no longer has a default message, see below for more details.

### Api Changes

- Allow setting deployment name in addition to modelId on AI services.
- Remove default message of "Assistant is a large language model" from ChatHistory
    - **This is a breaking change if you were relying on the default message in your code**
- Add InvocationReturnMode and rework OpenAi chat completion to allow configuring what data is returned from Chat
  requests

### Other

- Reorganize example projects and documentation structure.
- Number of sample updates and bug fixes.