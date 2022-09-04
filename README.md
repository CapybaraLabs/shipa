# Shipa [![Release](https://jitpack.io/v/dev.capybaralabs/shipa.svg?style=flat-square)](https://jitpack.io/#dev.capybaralabs/shipa) [![Coverage](https://img.shields.io/sonar/coverage/dev.capybaralabs.shipa?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/summary/overall?id=dev.capybaralabs.shipa)

This is a discord interaction lib that fits CapybaraLabs' needs. It is named
after [a beer](https://www.kehrwieder.beer/12/speaking-bottle/detail/15/shipa.html) which the original author was drinking when
creating the repository.

## Usage

This lib is in early development, and not ready for production. Only a subset of features has been implemented and
tested.

### Gradle

```groovy
repositories {
	maven { url "https://jitpack.io" }
}

dependencies {
	implementation "dev.capybaralabs:shipa:x.y.z"
}
 ```

### Spring Boot Component Scan

Include the project in your Spring Boot component scan, e.g. by using the provided module in your application launcher
class:

```kotlin
@SpringBootApplication
@Import(ShipaModule::class)
class Launcher {
	// ...
}
```

### Spring Boot Config Properties

Discord properties are not optional.

If not otherwise configured, the interaction-receiving controller will be deployed to `/api/interaction`.

```yaml
shipa:
    discord:
        application-id: 123
        public-key: "abc"
        bot-token: "xyz"
    interaction-controller-path: "/webhook/interaction"
```

### Implementations

You need to provide an implementation for `CommandLookupService`,
see [InMemoryCommandLookupService](example/src/main/kotlin/dev/capybaralabs/shipa/InMemoryCommandLookupService.kt) for
example.

You need to provide an implementation for `InteractionRepository`,
see [InMemoryInteractionRepository](example/src/main/kotlin/dev/capybaralabs/shipa/InMemoryInteractionRepository.kt) for
example. If you don't want to use the functionality, a noop implementation will do.

Commands should
implement [InteractionCommand](src/main/kotlin/dev/capybaralabs/shipa/discord/interaction/command/InteractionCommand.kt)
. The passed in `InteractionStateHolder` offers an API for responding.

[CommandRegisterService](src/main/kotlin/dev/capybaralabs/shipa/discord/interaction/command/CommandRegisterService.kt)
can be used to register your commands with Discord.

## Nullability

Extended table from the
related [Discord Docs](https://discord.com/developers/docs/reference#nullable-and-optional-resource-fields)

| FIELD                        | TYPE    | Kotlin Type         |
|------------------------------|---------|---------------------|
| optional_field?              | string  | `String?`           |
| nullable_field               | ?string | `Optional<String>`  |
| optional_and_nullable_field? | ?string | `Optional<String>?` |

The Kotlin types are somewhat unintuitively mapped, and
require [Jackson adjustments](src/main/kotlin/dev/capybaralabs/shipa/jackson/JacksonConfig.kt): We use

- **Kotlin nullability** to signal the **optionality** of a field
- **Java's Optional** to signal the **nullability** of a type

The main reason do to it this way is that there are fewer nullable types than optional fields in the Discord API, so we
get to type less awkward Java Optional code.

## Interaction State Machine

TODO none of these are really verified to be correct / the only choices

### SlashCommand

```plantuml
hide empty description

[*] --> Received : Webhook APPLICATION_COMMAND

Received --> MessageSent : Http.SendMessage
Received --> Thinking : Http.Ack

Thinking --> MessageSent : Rest.editOriginalResponse

MessageSent --> MessageSent : Rest.editOriginalResponse
MessageSent --> MessageSent : Rest.createFollowupMessage
MessageSent --> MessageSent : Rest.editFollowupMessage

Received --> TimedOut : 3 Seconds
Thinking --> TimedOut : 15 Minutes
MessageSent --> Done : 15 Minutes
TimedOut --> [*]
Done --> [*]
```

### MessageComponent invocation

```plantuml
hide empty description

[*] --> Received : Webhook MESSAGE_COMPONENT

Received --> MessageUpdated : Http.SendMessage
Received --> Thinking : Http.AckUpdate

Thinking --> MessageUpdated : Rest.editOriginalResponse

' Not sure about these.
' MessageUpdated --> MessageUpdated : Rest.createFollowupMessage
' MessageUpdated --> MessageUpdated : Rest.editFollowupMessage

' Also actually not sure about this one
' MessageUpdated --> MessageUpdated : Rest.editOriginalResponse

Received --> TimedOut : 3 Seconds
Thinking --> TimedOut : 15 Minutes
MessageUpdated --> Done : 15 Minutes
TimedOut --> [*]
Done --> [*]
```

### Autocomplete

```plantuml
hide empty description

[*] --> Received : Webhook APPLICATION_COMMAND_AUTOCOMPLETE
Received --> Done : Http APPLICATION_COMMAND_AUTOCOMPLETE_RESULT
Received --> TimedOut : 3 Seconds
TimedOut --> [*]
Done --> [*]

```

### Modal

TODO

```plantuml
hide empty description
```

TODO diagram with multiple interactions going back and forth.
