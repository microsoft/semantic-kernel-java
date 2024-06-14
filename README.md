# Semantic Kernel for Java

The [Semantic Kernel for Java](https://learn.microsoft.com/en-us/semantic-kernel/overview/?tabs=Java). For .NET, Python and other language support, see [https://github.com/semantic-kernel.](https://github.com/semantic-kernel). The remainder of this README is a summary focused on Java, please see the main repo for the full text for the Semantic Kernel.

[Maven Package](https://repo1.maven.org/maven2/com/microsoft/semantic-kernel/semantickernel-api/)
[![License: MIT](https://img.shields.io/github/license/microsoft/semantic-kernel)](https://github.com/microsoft/semantic-kernel/blob/main/LICENSE)
[![Discord](https://img.shields.io/discord/1063152441819942922?label=Discord&logo=discord&logoColor=white&color=d82679)](https://aka.ms/SKDiscord)

[Semantic Kernel](https://learn.microsoft.com/en-us/semantic-kernel/overview/)
is an SDK that integrates Large Language Models (LLMs) like
[OpenAI](https://platform.openai.com/docs/introduction),
[Azure OpenAI](https://azure.microsoft.com/en-us/products/ai-services/openai-service),
and [Hugging Face](https://huggingface.co/)
with conventional programming languages like C#, Python, and Java. Semantic Kernel achieves this
by allowing you to define [plugins](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/plugins)
that can be chained together
in just a [few lines of code](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/chaining-functions?tabs=Csharp#using-the-runasync-method-to-simplify-your-code).

What makes Semantic Kernel _special_, however, is its ability to _automatically_ orchestrate
plugins with AI. With Semantic Kernel
[planners](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/planner), you
can ask an LLM to generate a plan that achieves a user's unique goal. Afterwards,
Semantic Kernel will execute the plan for the user.

#### Please star the repo to show your support for this project!

![Orchestrating plugins with planner](https://learn.microsoft.com/en-us/semantic-kernel/media/kernel-infographic.png)

## Getting started with Semantic Kernel for Java

<img align="left" width=52px height=52px src="https://hg.openjdk.org/duke/duke/raw-file/e71b60779736/vector/ThumbsUp.svg" alt="Java logo"/><a href="https://github.com/microsoft/semantic-kernel/blob/main/java/README.md">Using Semantic Kernel in Java</a>

The quickest way to get started with the basics is to get an API key
from either OpenAI or Azure OpenAI and to run one of the C#, Python, and Java console applications/scripts below.

### For Java:

1. Clone the repository: `git clone https://github.com/microsoft/semantic-kernel-java.git`
    1. To access the latest Java code, clone and checkout the Java development branch: `git clone https://github.com/microsoft/semantic-kernel-java.git`
2. Follow the instructions [Start learning how to use Semantic Kernel](https://learn.microsoft.com/en-us/semantic-kernel/get-started/quick-start-guide?tabs=Java).

## Learning how to use Semantic Kernel

The fastest way to learn how to use Semantic Kernel is with our C# and Python Jupyter notebooks. These notebooks
demonstrate how to use Semantic Kernel with code snippets that you can run with a push of a button.

- [Getting Started with C# notebook](dotnet/notebooks/00-getting-started.ipynb)
- [Getting Started with Python notebook](python/samples/getting_started/00-getting-started.ipynb)

Once you've finished the getting started notebooks, you can then check out the main walkthroughs
on our Learn site. Each sample comes with a completed C# and Python project that you can run locally.

1. 📖 [Overview of the kernel](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/)
1. 🔌 [Understanding AI plugins](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/plugins)
1. 👄 [Creating semantic functions](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/semantic-functions)
1. 💽 [Creating native functions](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/native-functions)
1. ⛓️ [Chaining functions together](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/chaining-functions)
1. 🤖 [Auto create plans with planner](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/planner)
1. 💡 [Create and run a ChatGPT plugin](https://learn.microsoft.com/en-us/semantic-kernel/ai-orchestration/chatgpt-plugins)

## Join the community

We welcome your contributions and suggestions to SK community! One of the easiest
ways to participate is to engage in discussions in the GitHub repository.
Bug reports and fixes are welcome!

For new features, components, or extensions, please open an issue and discuss with
us before sending a PR. This is to avoid rejection as we might be taking the core
in a different direction, but also to consider the impact on the larger ecosystem.

To learn more and get started:

- Read the [documentation](https://aka.ms/sk/learn)
- Learn how to [contribute](https://learn.microsoft.com/en-us/semantic-kernel/get-started/contributing) to the project
- Join the [Discord community](https://aka.ms/SKDiscord)
- Attend [regular office hours and SK community events](COMMUNITY.md)
- Follow the team on our [blog](https://aka.ms/sk/blog)

## Contributor Wall of Fame

[![semantic-kernel contributors](https://contrib.rocks/image?repo=microsoft/semantic-kernel)](https://github.com/microsoft/semantic-kernel/graphs/contributors)

## Code of Conduct

This project has adopted the
[Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the
[Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/)
or contact [opencode@microsoft.com](mailto:opencode@microsoft.com)
with any additional questions or comments.

## License

Copyright (c) Microsoft Corporation. All rights reserved.

Licensed under the [MIT](LICENSE) license.
