# Localizable Player-Facing Text

All playable dialogue and other player-facing text that may need localization must live in separate language resources and be retrieved through stable text keys. Quest logic, dialogue flow, ECS components, and world-state logic must reference internal IDs rather than using visible text as identifiers.

This adds upfront structure compared with hardcoded prototype text, but avoids making authored content, dialogue conditions, and future localization costly to untangle once the vertical slice grows.
