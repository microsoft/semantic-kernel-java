# Presidio Sample

This sample demonstrates how to use Presidio with Semantic Kernel to redact sensitive data from a prompt. A sample
output from this example is shown below:

```
==============================
Input text is: 
The users name is: Steven.
Steven has account number 012345612.
Steven was born in New York and their mother is Sally.
==============================
Anonymised text is: 
The users name is: PERSON4.
PERSON4 has account number AU_ACN3.
PERSON4 was born in LOCATION1 and their mother is PERSON2.

==============================
User Question: 
Question: Where was the user born?
==============================
Anonymised response: 
The user was born in LOCATION1.
==============================
Deanonymised response: 
The user was born in New York.

==============================
User Question: 
Question: Who is the users mother?
==============================
Anonymised response: 
The user's mother is PERSON2.
==============================
Deanonymised response: 
The user's mother is Sally.
```

# App structure

The [semantickernel-presidio-plugin](..%2F..%2Fsemantickernel-sample-plugins%2Fsemantickernel-presidio-plugin) plugin
takes user text and runs it through Presidio to redact sensitive data. The plugin then returns the redacted text. The
redacted information is then sent to an LLM for processing. The response is then de-anonymised.

# Build and Run

As this example depends on running Presidio, it run within docker containers using docker compose.

- Before building and running ensure that you have run `./mvnw install` on the semantic kernel you wish to use.
- Copy [env.example](env.example) to `.env` and update the values as needed.
- Run [buildAndRun.sh](buildAndRun.sh)