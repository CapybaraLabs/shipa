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
