# Steam Boiler requirements — ID prefix conventions

53 structured requirements in the schema documented in CLAUDE.md (`kind`, `name`,
`id`, `description`, `priority`, `types`), plus a `source` field citing the section
of the original specification each requirement comes from. There is no separate
system description for this study: the requirements are the whole input.

| Prefix     | Layer                   | Count |
|------------|-------------------------|-------|
| `SB-FR*`   | Functional requirements | 8     |
| `SB-DM*`   | Data types              | 10    |
| `SB-V*`    | Variables and constants | 10    |
| `SB-Beh*`  | Behavioural transitions | 25    |

## Files in this directory

- `requirement_all.json` — the requirements. **Read this** when implementing.
- `requirement_all.txt` — the same content as text.
