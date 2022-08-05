# Shipa

This is a discord interaction lib that fits CapybaraLabs' needs. It is named
after [a beer](https://www.kehrwieder.shop/shipa-eclipse-single-hop-ipa) which the original author was drinking when
creating the repository.

### Using

This lib is still in development, so no versions have been released yet. You can try it out
via [Gradle Composite builds](https://docs.gradle.org/current/userguide/composite_builds.html).

### Nullability

Extended table from the
related [Discord Docs](https://discord.com/developers/docs/reference#nullable-and-optional-resource-fields)

| FIELD                        | TYPE    | Kotlin Type         |
|------------------------------|---------|---------------------|
| optional_field?              | string  | `String?`           |
| nullable_field               | ?string | `Optional<String>`  |
| optional_and_nullable_field? | ?string | `Optional<String>?` |

The Kotlin types are somewhat unintuitively mapped, and
require [Jackson adjustments](src/main/kotlin/dev/capybaralabs/shipa/JacksonConfig.kt): We use

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
