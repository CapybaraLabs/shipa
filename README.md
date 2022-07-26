# Shipa

named after a beer I liked

this is an interaction lib for discord that fit my needs

### Nullability

Extended table from the
related [Discord Docs](https://discord.com/developers/docs/reference#nullable-and-optional-resource-fields)

| FIELD                        | TYPE    | Kotlin Type         |
|------------------------------|---------|---------------------|
| optional_field?              | string  | `String?`           |
| nullable_field               | ?string | `Optional<String>`  |
| optional_and_nullable_field? | ?string | `Optional<String>?` |

The Kotlin types are somewhat unintuitively mapped, and require minor Jackson adjustments:
We use the Kotlin nullability to signal the optionality of a field, and Java's optional to signal the nullability of a
type.

The main reason for that is that there are fewer nullable fields than optional ones, so I get to type less Java code.
