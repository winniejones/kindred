# Intent-Based Free-Text Dialogue

Kindred's dialogue should be built around intent-based free text: the player types what they want to say, and the game maps the wording to controlled dialogue intentions and dialogue nodes. The vertical slice will implement this in a tightly limited scope to validate the model, not to imply that NPCs can discuss anything.

This is a deliberate trade-off against simpler fixed dialogue choices or keyword-only parsing. It supports a more expressive RPG dialogue feel, but requires dialogue logic, testing, localization, and authored content to depend on stable internal intentions rather than visible text strings.
